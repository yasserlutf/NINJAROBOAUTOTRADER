"""Read-only local MT5 to Firebase bridge for Ninja Robo Forex.

Run on the Windows computer where MetaTrader 5 is already logged in. This
program never sends orders and never asks for the MT5 trading password.
"""

from __future__ import annotations

import getpass
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

import MetaTrader5 as mt5
import numpy as np


ROOT = Path(getattr(sys, "_MEIPASS", Path(__file__).resolve().parents[1]))
GOOGLE_SERVICES = ROOT / "app" / "google-services.json"
ACCOUNT_WRITE_SECONDS = 2
POSITION_WRITE_SECONDS = 2
HEARTBEAT_SECONDS = 15
POLL_SECONDS = 1
REQUEST_POLL_SECONDS = 5
NETWORK_RETRY_SECONDS = 10
SESSION_PATH = Path(os.environ.get("LOCALAPPDATA", Path.home())) / "NinjaRoboForex" / "firebase-session.bin"


def protect_windows_data(data: bytes, *, decrypt: bool = False) -> bytes:
    """Protect a small session record with DPAPI for this Windows user."""
    if os.name != "nt":
        raise OSError("Secure session storage is available only on Windows.")

    import ctypes
    from ctypes import wintypes

    class DataBlob(ctypes.Structure):
        _fields_ = [("cbData", wintypes.DWORD), ("pbData", ctypes.POINTER(ctypes.c_ubyte))]

    source_buffer = ctypes.create_string_buffer(data)
    source = DataBlob(len(data), ctypes.cast(source_buffer, ctypes.POINTER(ctypes.c_ubyte)))
    destination = DataBlob()
    crypt32 = ctypes.WinDLL("Crypt32", use_last_error=True)
    kernel32 = ctypes.WinDLL("Kernel32", use_last_error=True)
    flags = 0x1  # CRYPTPROTECT_UI_FORBIDDEN
    if decrypt:
        operation = crypt32.CryptUnprotectData
        operation.argtypes = [ctypes.POINTER(DataBlob), ctypes.POINTER(wintypes.LPWSTR),
                              ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p,
                              wintypes.DWORD, ctypes.POINTER(DataBlob)]
        operation.restype = wintypes.BOOL
        succeeded = operation(ctypes.byref(source), None, None, None, None, flags,
                              ctypes.byref(destination))
    else:
        operation = crypt32.CryptProtectData
        operation.argtypes = [ctypes.POINTER(DataBlob), wintypes.LPCWSTR, ctypes.c_void_p,
                              ctypes.c_void_p, ctypes.c_void_p, wintypes.DWORD,
                              ctypes.POINTER(DataBlob)]
        operation.restype = wintypes.BOOL
        succeeded = operation(ctypes.byref(source), "Ninja Robo Firebase session", None,
                              None, None, flags, ctypes.byref(destination))
    if not succeeded:
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        return ctypes.string_at(destination.pbData, destination.cbData)
    finally:
        kernel32.LocalFree.argtypes = [ctypes.c_void_p]
        kernel32.LocalFree.restype = ctypes.c_void_p
        kernel32.LocalFree(destination.pbData)


def load_saved_session() -> dict | None:
    if os.name != "nt" or not SESSION_PATH.exists():
        return None
    try:
        return json.loads(protect_windows_data(SESSION_PATH.read_bytes(), decrypt=True))
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError):
        # A corrupt file or a different Windows user cannot decrypt this session.
        SESSION_PATH.unlink(missing_ok=True)
        return None


def save_session(uid: str, email: str, refresh_token: str) -> None:
    if os.name != "nt":
        return
    SESSION_PATH.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps({"uid": uid, "email": email, "refreshToken": refresh_token}).encode("utf-8")
    SESSION_PATH.write_bytes(protect_windows_data(payload))


def load_firebase_config() -> tuple[str, str]:
    with GOOGLE_SERVICES.open("r", encoding="utf-8") as source:
        config = json.load(source)
    project_id = config["project_info"]["project_id"]
    api_key = config["client"][0]["api_key"][0]["current_key"]
    return project_id, api_key


def request_json(url: str, method: str = "GET", body: dict | None = None,
                 token: str | None = None) -> dict:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    payload = json.dumps(body).encode("utf-8") if body is not None else None
    request = urllib.request.Request(url, data=payload, headers=headers, method=method)
    while True:
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                raw = response.read()
                return json.loads(raw) if raw else {}
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"Firebase HTTP {error.code}: {detail}") from None
        except urllib.error.URLError as error:
            reason = error.reason if isinstance(error, urllib.error.URLError) else error
            print(f"Temporary Firebase network/DNS error: {reason}. Retrying in {NETWORK_RETRY_SECONDS} seconds.")
            time.sleep(NETWORK_RETRY_SECONDS)


class FirebaseSession:
    def __init__(self, project_id: str, api_key: str) -> None:
        self.project_id = project_id
        self.api_key = api_key
        saved = load_saved_session()
        if saved:
            self.uid = saved["uid"]
            self.email = saved.get("email", "")
            self.refresh_token = saved["refreshToken"]
            self.expires_at = 0.0
            try:
                self._refresh()
                print("Restored the securely saved Ninja Robo sign-in for this Windows user.")
                return
            except RuntimeError as error:
                if "HTTP 400" not in str(error) and "HTTP 401" not in str(error):
                    raise
                SESSION_PATH.unlink(missing_ok=True)
                print("The saved Ninja Robo sign-in expired. Please sign in again.")

        self.email = input("Ninja Robo app email: ").strip()
        password = getpass.getpass("Ninja Robo app password (not your MT5 password): ")
        result = request_json(
            f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={api_key}",
            "POST",
            {"email": self.email, "password": password, "returnSecureToken": True},
        )
        self.uid = result["localId"]
        self.id_token = result["idToken"]
        self.refresh_token = result["refreshToken"]
        self.expires_at = time.time() + int(result.get("expiresIn", "3600")) - 60
        save_session(self.uid, self.email, self.refresh_token)
        # Discard the password reference as soon as Firebase has issued the session.
        password = ""
        print(f"Signed in to Ninja Robo as {self.email}; this session is for Firebase only.")

    def token(self) -> str:
        if time.time() >= self.expires_at:
            self._refresh()
        return self.id_token

    def _refresh(self) -> None:
        form = urllib.parse.urlencode({
            "grant_type": "refresh_token",
            "refresh_token": self.refresh_token,
        }).encode("ascii")
        request = urllib.request.Request(
            f"https://securetoken.googleapis.com/v1/token?key={self.api_key}",
            data=form,
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                result = json.loads(response.read())
        except urllib.error.HTTPError as error:
            details = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"Firebase token refresh HTTP {error.code}: {details}") from None
        self.id_token = result["id_token"]
        self.refresh_token = result["refresh_token"]
        self.expires_at = time.time() + int(result.get("expires_in", "3600")) - 60
        save_session(self.uid, self.email, self.refresh_token)

    def firestore(self, path: str, method: str = "GET", body: dict | None = None,
                  fields: list[str] | None = None) -> dict:
        base = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/{path}"
        if fields:
            query = urllib.parse.urlencode([("updateMask.fieldPaths", field) for field in fields])
            base = f"{base}?{query}"
        try:
            return request_json(base, method, body, self.token())
        except RuntimeError as error:
            if "HTTP 401" not in str(error):
                raise
            self.expires_at = 0
            return request_json(base, method, body, self.token())


def string_value(document: dict, name: str, default: str = "") -> str:
    return document.get("fields", {}).get(name, {}).get("stringValue", default)


def request_time(document: dict) -> str:
    return document.get("fields", {}).get("requestedAt", {}).get("timestampValue", "")


def latest_request(session: FirebaseSession) -> dict | None:
    path = f"users/{session.uid}/mt5_connection_requests"
    result = session.firestore(path)
    documents = result.get("documents", [])
    if not documents:
        return None
    documents.sort(key=request_time, reverse=True)
    return documents[0]


def patch_request(session: FirebaseSession, document: dict, status: str, message: str = "") -> None:
    name = document["name"]
    relative_path = name.split("/documents/", 1)[1]
    fields: dict = {
        "status": {"stringValue": status},
        "processedAt": {"timestampValue": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")},
    }
    masks = ["status", "processedAt"]
    if message:
        fields["message"] = {"stringValue": message[:300]}
        masks.append("message")
    session.firestore(relative_path, "PATCH", {"fields": fields}, masks)


def firestore_value(value: object) -> dict:
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    if isinstance(value, float):
        return {"doubleValue": value}
    if isinstance(value, str):
        return {"stringValue": value}
    raise TypeError(f"Unsupported Firestore value: {type(value).__name__}")


def write_document(session: FirebaseSession, path: str, values: dict) -> None:
    firestore_fields = {key: firestore_value(value) for key, value in values.items()}
    if "lastSyncedAt" in values:
        firestore_fields["lastSyncedAt"] = {"timestampValue": values["lastSyncedAt"]}
    if "processedAt" in values:
        firestore_fields["processedAt"] = {"timestampValue": values["processedAt"]}
    session.firestore(path, "PATCH", {"fields": firestore_fields}, list(firestore_fields))


def position_values(position) -> dict:
    return {
        "symbol": position.symbol,
        "side": "BUY" if position.type == mt5.POSITION_TYPE_BUY else "SELL",
        "volume": float(position.volume),
        "openPrice": float(position.price_open),
        "currentPrice": float(position.price_current),
        "profit": float(position.profit),
        "stopLoss": float(position.sl),
        "takeProfit": float(position.tp),
    }


def sync_positions(session: FirebaseSession, positions: tuple,
                   known_ids: set[str], last_written: dict[str, tuple[dict, float]]) -> int:
    """Write new/changed positions promptly, throttle tick-only changes, and remove closed ones."""
    collection = f"users/{session.uid}/open_positions"
    current_ids: set[str] = set()
    now = time.monotonic()
    changed_count = 0
    for position in positions:
        ticket = str(position.ticket)
        current_ids.add(ticket)
        values = position_values(position)
        previous = last_written.get(ticket)
        structural_fields = ("symbol", "side", "volume", "openPrice", "stopLoss", "takeProfit")
        structural_change = previous is None or any(
            previous[0].get(field) != values[field] for field in structural_fields
        )
        snapshot_change = previous is None or any(
            previous[0].get(field) != values[field] for field in values
        )
        due_for_snapshot = previous is None or now - previous[1] >= POSITION_WRITE_SECONDS
        if snapshot_change and (structural_change or due_for_snapshot):
            encoded_ticket = urllib.parse.quote(ticket, safe="")
            write_document(session, f"{collection}/{encoded_ticket}", values)
            last_written[ticket] = (values, now)
            changed_count += 1

    for ticket in known_ids - current_ids:
        relative_path = f"{collection}/{urllib.parse.quote(ticket, safe='')}"
        session.firestore(relative_path, "DELETE")
        last_written.pop(ticket, None)
        changed_count += 1
    known_ids.clear()
    known_ids.update(current_ids)
    return changed_count


def account_values(account, server: str) -> dict:
    return {
        "brokerStatus": "connected",
        "brokerServer": server,
        "accountLogin": str(account.login),
        "currency": str(account.currency),
        "balance": float(account.balance),
        "equity": float(account.equity),
        "margin": float(account.margin),
        "freeMargin": float(account.margin_free),
        "leverage": int(account.leverage),
        "profit": float(account.profit),
    }


def sync_account(session: FirebaseSession, values: dict, last_values: dict | None,
                 last_write: float) -> tuple[dict, float, bool]:
    now = time.monotonic()
    changed = last_values is None or values != last_values
    heartbeat_due = now - last_write >= HEARTBEAT_SECONDS
    if changed and (last_values is None or now - last_write >= ACCOUNT_WRITE_SECONDS):
        should_write = True
    else:
        should_write = heartbeat_due
    if should_write:
        write_document(session, f"users/{session.uid}/bridge/account", {
            **values,
            "lastSyncedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        })
        return values, now, True
    return last_values or values, last_write, False


def main() -> None:
    project_id, api_key = load_firebase_config()
    session = FirebaseSession(project_id, api_key)
    try:
        while not mt5.initialize():
            print(f"Waiting for MetaTrader 5 to open: {mt5.last_error()}")
            time.sleep(10)

        print("MT5 terminal found. Waiting for a connection request from the Android app.")
        print("This bridge reads account/position data only; it does not place trades.")
        position_collection = f"users/{session.uid}/open_positions"
        existing = session.firestore(position_collection).get("documents", [])
        known_position_ids = {document["name"].rsplit("/", 1)[-1] for document in existing}
        last_position_values: dict[str, tuple[dict, float]] = {}
        last_account_values: dict | None = None
        last_account_write = 0.0
        last_status_log = 0.0
        next_request_poll = 0.0
        request: dict | None = None
        last_request_id = ""
        last_mismatch_id = ""
        last_no_request_notice = 0.0
        while True:
            account = mt5.account_info()
            if account is None:
                print(f"MT5 account data unavailable: {mt5.last_error()}")
                time.sleep(POLL_SECONDS)
                continue

            monotonic_now = time.monotonic()
            if monotonic_now >= next_request_poll:
                request = latest_request(session)
                next_request_poll = monotonic_now + REQUEST_POLL_SECONDS
            if request is None:
                if monotonic_now - last_no_request_notice >= 30:
                    print("No app request yet. In the app, open Settings → MT5 connection and request it.")
                    last_no_request_notice = monotonic_now
                time.sleep(POLL_SECONDS)
                continue

            request_id = request["name"].rsplit("/", 1)[-1]
            request_status = string_value(request, "status", "pending")
            expected_login = string_value(request, "accountLogin")
            expected_server = string_value(request, "brokerServer")
            actual_login = str(account.login)
            actual_server = str(account.server)

            if request_status == "error":
                if request_id != last_request_id:
                    print("Latest app request is marked error. Submit a fresh request from the app to retry.")
                    last_request_id = request_id
                time.sleep(POLL_SECONDS)
                continue

            if expected_login != actual_login or expected_server.casefold() != actual_server.casefold():
                message = (
                    f"MT5 is signed in as {actual_login} on {actual_server}; "
                    f"the app requested {expected_login} on {expected_server}."
                )
                if request_status in ("pending", "connected") and request_id != last_mismatch_id:
                    patch_request(session, request, "error", message)
                    request.setdefault("fields", {})["status"] = {"stringValue": "error"}
                    last_mismatch_id = request_id
                    write_document(session, f"users/{session.uid}/bridge/account", {
                        "brokerStatus": "error",
                        "brokerServer": actual_server,
                        "accountLogin": actual_login,
                        "lastSyncedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    })
                if request_id != last_request_id:
                    print(message)
                    print("Switch the MT5 terminal to the requested account, then submit a new app request.")
                    last_request_id = request_id
                time.sleep(POLL_SECONDS)
                continue

            if request_status == "pending":
                patch_request(session, request, "connected")
                request.setdefault("fields", {})["status"] = {"stringValue": "connected"}
                print(f"Matched MT5 account {actual_login} on {actual_server} to the signed-in app user.")
                last_account_write = 0.0

            positions = mt5.positions_get()
            if positions is None:
                print(f"MT5 positions unavailable: {mt5.last_error()}")
                time.sleep(POLL_SECONDS)
                continue

            position_changes = sync_positions(
                session, positions, known_position_ids, last_position_values
            )
            values = account_values(account, actual_server)
            last_account_values, last_account_write, account_changed = sync_account(
                session, values, last_account_values, last_account_write
            )
            if position_changes or account_changed or monotonic_now - last_status_log >= HEARTBEAT_SECONDS:
                print(
                    f"Live sync {actual_login}: equity {account.equity:.2f} {account.currency}, "
                    f"{len(positions)} open positions."
                )
                last_status_log = monotonic_now
            time.sleep(POLL_SECONDS)
    except KeyboardInterrupt:
        print("Bridge stopped by user.")
    finally:
        mt5.shutdown()


if __name__ == "__main__":
    if "--self-check" in sys.argv:
        print(f"Connector bundle OK: MetaTrader5 {mt5.__version__}, NumPy {np.__version__}")
        raise SystemExit(0)
    try:
        main()
    except Exception as error:
        print(f"Bridge stopped: {error}")
        input("Press Enter to close…")
