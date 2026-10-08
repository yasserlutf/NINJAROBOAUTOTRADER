"""Signal-only research backtest for a simple MT5 scalping hypothesis.

This script reads historical bars from the already-open MT5 terminal. It never
submits, modifies, or closes orders. Results are exploratory and do not predict
future performance.
"""

from __future__ import annotations

import argparse
import math
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone

import MetaTrader5 as mt5


@dataclass
class Trade:
    side: int  # +1 buy, -1 sell
    entry_index: int
    entry: float
    stop: float
    target: float
    risk: float


@dataclass
class ClosedTrade:
    side: int
    entry_time: datetime
    exit_time: datetime
    entry: float
    exit: float
    net_points: float
    r_multiple: float
    reason: str


def ema(values: list[float], period: int) -> list[float | None]:
    result: list[float | None] = [None] * len(values)
    if period < 1 or len(values) < period:
        return result
    alpha = 2.0 / (period + 1.0)
    current = sum(values[:period]) / period
    result[period - 1] = current
    for index in range(period, len(values)):
        value = values[index]
        current = alpha * value + (1.0 - alpha) * current
        result[index] = current
    return result


def atr(rates, period: int) -> list[float | None]:
    result: list[float | None] = [None] * len(rates)
    ranges: list[float] = []
    previous_close: float | None = None
    for index, bar in enumerate(rates):
        high, low, close = float(bar["high"]), float(bar["low"]), float(bar["close"])
        true_range = high - low if previous_close is None else max(
            high - low, abs(high - previous_close), abs(low - previous_close)
        )
        ranges.append(true_range)
        if len(ranges) >= period:
            result[index] = sum(ranges[-period:]) / period
        previous_close = close
    return result


def make_m5_trend(m5_rates, fast_period: int, slow_period: int) -> dict[int, int]:
    closes = [float(bar["close"]) for bar in m5_rates]
    fast, slow = ema(closes, fast_period), ema(closes, slow_period)
    # A five-minute candle is usable only after it has closed.
    warmup = slow_period * 3
    return {
        int(bar["time"]) + 300: (1 if fast[i] > slow[i] else -1)
        for i, bar in enumerate(m5_rates)
        if i >= warmup and fast[i] is not None and slow[i] is not None
    }


def close_price_for_bar(position: Trade, bar, point: float, slippage_points: float):
    spread = float(bar["spread"]) * point
    bid_open, bid_high, bid_low = (float(bar[key]) for key in ("open", "high", "low"))
    slip = slippage_points * point
    if position.side == 1:
        # Long positions exit against Bid.
        if bid_open <= position.stop:
            return bid_open - slip, "stop (gap)"
        if bid_open >= position.target:
            return bid_open - slip, "target (gap)"
        if bid_low <= position.stop:
            return position.stop - slip, "stop"
        if bid_high >= position.target:
            return position.target - slip, "target"
    else:
        # Short positions exit against Ask, approximated from each bar's spread.
        ask_open = bid_open + spread
        ask_high, ask_low = bid_high + spread, bid_low + spread
        if ask_open >= position.stop:
            return ask_open + slip, "stop (gap)"
        if ask_open <= position.target:
            return ask_open + slip, "target (gap)"
        if ask_high >= position.stop:
            return position.stop + slip, "stop"
        if ask_low <= position.target:
            return position.target + slip, "target"
    return None


def evaluate(args) -> tuple[list[ClosedTrade], int, int]:
    if not mt5.initialize():
        raise RuntimeError(f"Could not connect to MT5: {mt5.last_error()}")
    try:
        info = mt5.symbol_info(args.symbol)
        if info is None:
            raise RuntimeError(f"Symbol {args.symbol!r} is not available in this MT5 terminal.")
        if not info.visible and not mt5.symbol_select(args.symbol, True):
            raise RuntimeError(f"Could not select {args.symbol!r}: {mt5.last_error()}")

        end = datetime.now(timezone.utc)
        start = end - timedelta(days=args.days)
        m1 = mt5.copy_rates_range(args.symbol, mt5.TIMEFRAME_M1, start, end)
        m5 = mt5.copy_rates_range(args.symbol, mt5.TIMEFRAME_M5, start, end)
        if m1 is None or m5 is None or len(m1) < 100 or len(m5) < 60:
            raise RuntimeError(
                f"Not enough MT5 history for {args.symbol}; open its M1/M5 charts and download more history."
            )

        m1_ema = ema([float(bar["close"]) for bar in m1], args.entry_ema)
        m1_atr = atr(m1, args.atr_period)
        trend_by_close = make_m5_trend(m5, args.fast_ema, args.slow_ema)
        trend_times = sorted(trend_by_close)
        trend_index = -1
        trend = 0
        position: Trade | None = None
        pending_side = 0
        closed: list[ClosedTrade] = []
        skipped_spread = 0
        skipped_no_trend = 0
        point = float(info.point)
        commission = args.commission_points_round_trip
        slippage = args.slippage_points

        for index, bar in enumerate(m1):
            bar_time = int(bar["time"])
            close_time = bar_time + 60
            while trend_index + 1 < len(trend_times) and trend_times[trend_index + 1] <= close_time:
                trend_index += 1
                trend = trend_by_close[trend_times[trend_index]]

            if position is not None:
                exit_result = close_price_for_bar(position, bar, point, slippage)
                if exit_result:
                    exit_price, reason = exit_result
                    price_points = position.side * (exit_price - position.entry) / point
                    net_points = price_points - commission
                    closed.append(ClosedTrade(
                        position.side,
                        datetime.fromtimestamp(int(m1[position.entry_index]["time"]), timezone.utc),
                        datetime.fromtimestamp(bar_time, timezone.utc),
                        position.entry,
                        exit_price,
                        net_points,
                        net_points * point / position.risk,
                        reason,
                    ))
                    position = None
                continue

            # Signals are evaluated at a closed M1 bar and entered at the next bar's open.
            if pending_side:
                spread_points = float(bar["spread"])
                if spread_points > args.max_spread_points:
                    skipped_spread += 1
                    pending_side = 0
                    continue
                signal_atr = m1_atr[index - 1] if index > 0 else None
                if signal_atr is None or signal_atr <= 0:
                    pending_side = 0
                    continue
                bid_open = float(bar["open"])
                ask_open = bid_open + spread_points * point
                side = pending_side
                entry = ask_open + slippage * point if side == 1 else bid_open - slippage * point
                risk = signal_atr * args.stop_atr
                position = Trade(
                    side=side,
                    entry_index=index,
                    entry=entry,
                    stop=entry - side * risk,
                    target=entry + side * risk * args.reward_risk,
                    risk=risk,
                )
                pending_side = 0
                # Check whether the entry candle also hit an exit level.
                exit_result = close_price_for_bar(position, bar, point, slippage)
                if exit_result:
                    exit_price, reason = exit_result
                    price_points = side * (exit_price - entry) / point
                    net_points = price_points - commission
                    closed.append(ClosedTrade(
                        side,
                        datetime.fromtimestamp(bar_time, timezone.utc),
                        datetime.fromtimestamp(bar_time, timezone.utc),
                        entry,
                        exit_price,
                        net_points,
                        net_points * point / risk,
                        reason,
                    ))
                    position = None
                continue

            if index < 1 or trend == 0:
                skipped_no_trend += 1
                continue
            previous_ema, current_ema = m1_ema[index - 1], m1_ema[index]
            if previous_ema is None or current_ema is None:
                continue
            previous_close = float(m1[index - 1]["close"])
            current_close = float(bar["close"])
            if trend == 1 and previous_close <= previous_ema and current_close > current_ema:
                pending_side = 1
            elif trend == -1 and previous_close >= previous_ema and current_close < current_ema:
                pending_side = -1

        if position is not None:
            last_bar = m1[-1]
            bid_close = float(last_bar["close"])
            spread = float(last_bar["spread"]) * point
            exit_price = bid_close - slippage * point if position.side == 1 else bid_close + spread + slippage * point
            price_points = position.side * (exit_price - position.entry) / point
            net_points = price_points - commission
            closed.append(ClosedTrade(
                position.side,
                datetime.fromtimestamp(int(m1[position.entry_index]["time"]), timezone.utc),
                datetime.fromtimestamp(int(last_bar["time"]), timezone.utc),
                position.entry,
                exit_price,
                net_points,
                net_points * point / position.risk,
                "end of history",
            ))
        return closed, skipped_spread, skipped_no_trend
    finally:
        mt5.shutdown()


def report(args, trades: list[ClosedTrade], skipped_spread: int, skipped_no_trend: int) -> None:
    wins = [trade.r_multiple for trade in trades if trade.r_multiple > 0]
    losses = [trade.r_multiple for trade in trades if trade.r_multiple < 0]
    net_r = sum(trade.r_multiple for trade in trades)
    gross_wins, gross_losses = sum(wins), abs(sum(losses))
    profit_factor = gross_wins / gross_losses if gross_losses else (math.inf if gross_wins else 0.0)
    equity = peak = max_drawdown = 0.0
    for trade in trades:
        equity += trade.r_multiple
        peak = max(peak, equity)
        max_drawdown = max(max_drawdown, peak - equity)

    print(f"Signal-only scalping research: {args.symbol}")
    print(f"Hypothesis: M5 EMA trend + M1 EMA pullback reclaim; ATR stop and fixed R target")
    print(f"Trades: {len(trades)} | wins: {len(wins)} | losses: {len(losses)}")
    print(f"Net result: {net_r:.2f} R | profit factor: {profit_factor:.2f} | max drawdown: {max_drawdown:.2f} R")
    print(f"Average win: {sum(wins) / len(wins):.2f} R" if wins else "Average win: n/a")
    print(f"Average loss: {sum(losses) / len(losses):.2f} R" if losses else "Average loss: n/a")
    print(f"Skipped for spread: {skipped_spread} | bars without aligned M5 trend: {skipped_no_trend}")
    print(f"Costs: {args.commission_points_round_trip:g} commission points round trip, {args.slippage_points:g} slippage points per side")
    if trades:
        print(f"Sample: {trades[0].entry_time.isoformat()} to {trades[-1].exit_time.isoformat()}")
    print("Bars are an approximation: if stop and target are both reached in one M1 bar, stop is assumed first.")
    print("Research only. No orders were sent; results do not predict future performance.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--symbol", required=True, help="Exact broker symbol, for example EURUSD.pro")
    parser.add_argument("--days", type=int, default=180)
    parser.add_argument("--max-spread-points", type=float, required=True,
                        help="Spread ceiling based on this broker symbol's observed spread")
    parser.add_argument("--commission-points-round-trip", type=float, default=0.0,
                        help="Broker commission converted to price points for one round trip")
    parser.add_argument("--slippage-points", type=float, default=0.0,
                        help="Adverse slippage assumption per side, in MT5 points")
    parser.add_argument("--entry-ema", type=int, default=20)
    parser.add_argument("--fast-ema", type=int, default=20)
    parser.add_argument("--slow-ema", type=int, default=50)
    parser.add_argument("--atr-period", type=int, default=14)
    parser.add_argument("--stop-atr", type=float, default=1.2)
    parser.add_argument("--reward-risk", type=float, default=1.5)
    args = parser.parse_args()
    if args.days < 1 or args.max_spread_points < 0 or args.commission_points_round_trip < 0 or args.slippage_points < 0:
        parser.error("days must be positive and cost/spread values cannot be negative")
    if min(args.entry_ema, args.fast_ema, args.slow_ema, args.atr_period) < 1 or args.stop_atr <= 0 or args.reward_risk <= 0:
        parser.error("indicator periods, stop ATR, and reward/risk must be positive")
    trades, skipped_spread, skipped_no_trend = evaluate(args)
    report(args, trades, skipped_spread, skipped_no_trend)


if __name__ == "__main__":
    main()
