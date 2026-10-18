# -*- coding: utf-8 -*-
"""用 Chrome DevTools 协议截图若依前端页面（先注入登录 Cookie）"""
import base64
import json
import os
import subprocess
import sys
import time
import urllib.request

import websocket

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from ry_api import login  # noqa: E402

CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
PORT = 9222
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "shots")
os.makedirs(OUT, exist_ok=True)


def start_chrome():
    profile = os.path.join(os.environ.get("TEMP", "."), "ry_cdp_profile")
    subprocess.Popen([CHROME, "--headless=new", "--disable-gpu", "--no-first-run",
                      "--remote-debugging-port=%d" % PORT, "--remote-allow-origins=*",
                      "--user-data-dir=" + profile, "--window-size=1600,1000", "about:blank"],
                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    for _ in range(40):
        try:
            with urllib.request.urlopen("http://127.0.0.1:%d/json/version" % PORT, timeout=1) as r:
                return json.loads(r.read())
        except Exception:
            time.sleep(0.5)
    raise RuntimeError("chrome 未启动")


def new_tab(url="about:blank"):
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    req = urllib.request.Request("http://127.0.0.1:%d/json/new?%s" % (PORT, url), method="PUT")
    with opener.open(req, timeout=5) as r:
        return json.loads(r.read())


class Cdp:
    def __init__(self, ws_url):
        self.ws = websocket.create_connection(ws_url, timeout=30)
        self.i = 0

    def send(self, method, params=None, wait=True):
        self.i += 1
        mid = self.i
        self.ws.send(json.dumps({"id": mid, "method": method, "params": params or {}}))
        if not wait:
            return None
        while True:
            msg = json.loads(self.ws.recv())
            if msg.get("id") == mid:
                if "error" in msg:
                    raise RuntimeError(msg["error"])
                return msg.get("result", {})

    def eval(self, expr):
        r = self.send("Runtime.evaluate", {"expression": expr, "returnByValue": True, "awaitPromise": True})
        return (r.get("result") or {}).get("value")


def main():
    token, _ = login("admin", "123456")
    start_chrome()
    tab = new_tab()
    cdp = Cdp(tab["webSocketDebuggerUrl"])
    cdp.send("Page.enable")
    cdp.send("Runtime.enable")
    cdp.send("Network.enable")
    cdp.send("Network.setCookie", {"name": "Admin-Token", "value": token,
                                   "domain": "localhost", "path": "/", "httpOnly": False})
    # 收集控制台错误
    errors = []

    pages = [
        ("child", "/child/list"),
        ("notice", "/finance/notice"),
        ("my_child", "/mychild/index"),
        ("clazz", "/child/clazz"),
    ]
    for name, path in pages:
        cdp.send("Page.navigate", {"url": "http://localhost:81" + path})
        time.sleep(6)
        # 取控制台错误
        try:
            while True:
                cdp.ws.settimeout(0.2)
                msg = json.loads(cdp.ws.recv())
                if msg.get("method") == "Runtime.consoleAPICalled" and msg["params"].get("type") == "error":
                    errors.append((name, str(msg["params"].get("args"))[:200]))
                if msg.get("method") == "Runtime.exceptionThrown":
                    errors.append((name, msg["params"]["exceptionDetails"].get("text")))
        except Exception:
            cdp.ws.settimeout(30)
        text = cdp.eval("document.body.innerText.slice(0,600)")
        shot = cdp.send("Page.captureScreenshot", {"format": "png"})
        with open(os.path.join(OUT, name + ".png"), "wb") as f:
            f.write(base64.b64decode(shot["data"]))
        print("==", name, "==")
        print((text or "").replace("\n", " | ")[:420])
    print("console errors:", errors[:5])


if __name__ == "__main__":
    main()
