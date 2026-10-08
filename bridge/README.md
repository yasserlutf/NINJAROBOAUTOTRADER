# Ninja Robo MT5 bridge for Windows

This local bridge is for the Windows computer where MetaTrader 5 is already running. It reads the logged-in terminal account and open positions, then writes them to the matching signed-in user's own Firebase records. It does not place orders and does not ask for the MT5 trading password.

## One-time Windows setup

1. Download `NinjaRoboConnectorSetup.exe` from the **Ninja Robo Forex Connector** GitHub Actions artifact and copy it to the Windows computer or VMware Fusion Windows VM.
2. Open MetaTrader 5 and log in to the account you want to link. Leave MT5 open.
3. Double-click **NinjaRoboConnectorSetup.exe** and complete setup. It installs for the current Windows user and does not require administrator access. You can select automatic startup when Windows signs in.
4. On first launch, enter the **same Ninja Robo app email and password** used in the Android app. This is the Firebase app password, not the broker/MT5 password. The refresh token is encrypted with Windows DPAPI for this Windows user; the app password is not saved.
5. In the Android app, open **Settings → MT5 connection**, enter the account number and exact server displayed in MT5, then tap **Request secure connection**.
6. Keep the Windows computer/VM awake, connected to the internet, and MT5 open for continuous sync. The connector starts automatically at Windows sign-in if you selected that option. To uninstall it, use **Settings → Apps → Installed apps**; uninstalling also removes the saved app session.

For maintainers: see `BUILD_WINDOWS_CONNECTOR.md` for the Windows build workflow. Until its first successful GitHub Actions run, the installer executable is not yet available; the repository contains the scripts that build it.

The bridge checks for new requests every 5 seconds and syncs account/position values every 15 seconds. If the logged-in MT5 account doesn't match the latest app request, it reports the mismatch in the app. Fix the terminal login, then send a fresh request from Settings.

## Research-only scalping prototype

`scalping_research.py` evaluates a deliberately simple hypothesis: an M5 EMA trend filter plus an M1 EMA pullback reclaim, with an ATR-based stop and fixed reward/risk target. It reads historical M1/M5 bars from the open terminal and prints a report in R multiples. It is a screening tool, not an AI model or proof of an edge. It never places or modifies orders.

Run it from the installed connector folder in the Windows VM, with MT5 open and history loaded for the exact broker symbol:

```powershell
py bridge\scalping_research.py --symbol EURUSD.pro --days 180 --max-spread-points 10
```

Replace `EURUSD.pro` with the exact symbol shown by this broker. The spread ceiling is an example only: use the symbol's actual MT5 point size and observed spread. Add the broker's round-trip commission in price points with `--commission-points-round-trip` and a conservative per-side slippage assumption with `--slippage-points`. The bar-based simulation assumes the stop is hit first if both stop and target occur inside the same M1 candle; validate any candidate again in MT5 Strategy Tester using real ticks before considering demo execution.

The initial hypothesis is transparent and deterministic. An AI scoring/filter layer should only be considered after collecting enough clean historical and demo data to evaluate it out of sample. It must not bypass spread checks, stop-loss requirements, exposure limits, or the pause control.

## Important limits

- This is a per-user local bridge: every app user runs it on their own Windows machine. That avoids a paid always-on Windows VPS, but syncing stops when the PC or MT5 closes.
- Firebase Email/Password sign-in must be enabled for the Ninja Robo account. Google-only sign-in is not supported by this first bridge version.
- Deploy `firestore.rules` before relying on the bridge. The rules let a user write only their own bridge summary, open positions, and connection request status; they cannot access another user's data.
- This is account telemetry sync only. It does not execute trades or send commands to MT5.
- A reconnect request must match the exact MT5 login and server string.
