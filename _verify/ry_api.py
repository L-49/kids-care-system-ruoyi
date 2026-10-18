# -*- coding: utf-8 -*-
"""若依版接口验证脚本：从 Redis 取验证码答案 -> 登录 -> 调业务接口。
只读为主，新增数据统一加 __验证 前缀，跑完由本脚本删除。
"""
import json
import socket
import sys
import urllib.parse
import urllib.request

import os

BASE = os.environ.get("KIDS_API", "http://127.0.0.1:8081")


def resp_cmd(sock, *args):
    """发送 RESP 命令"""
    out = b"*%d\r\n" % len(args)
    for a in args:
        b = a.encode()
        out += b"$%d\r\n%s\r\n" % (len(b), b)
    sock.sendall(out)
    return read_reply(sock, b"")


def read_reply(sock, buf):
    def readline():
        nonlocal buf
        while b"\r\n" not in buf:
            buf += sock.recv(4096)
        line, buf = buf.split(b"\r\n", 1)
        return line

    line = readline()
    t, rest = line[:1], line[1:]
    if t == b"+":
        return rest.decode()
    if t == b"-":
        return Exception(rest.decode())
    if t == b":":
        return int(rest)
    if t == b"$":
        n = int(rest)
        if n == -1:
            return None
        while len(buf) < n + 2:
            buf += sock.recv(4096)
        data, buf = buf[:n], buf[n + 2:]
        return data.decode("utf-8", "replace")
    if t == b"*":
        n = int(rest)
        return [read_reply(sock, buf) for _ in range(n)]
    return None


def redis_get(key):
    s = socket.create_connection(("127.0.0.1", 6379), timeout=3)
    try:
        resp_cmd(s, "SELECT", "1")
        return resp_cmd(s, "GET", key)
    finally:
        s.close()


def http(path, method="GET", token=None, body=None, raw=False):
    path = urllib.parse.quote(path, safe="/?&=%:,")
    url = BASE + path
    data = None
    headers = {"Content-Type": "application/json"}
    if body is not None:
        data = json.dumps(body).encode()
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    try:
        with opener.open(req, timeout=20) as r:
            content = r.read()
            if raw:
                return r.status, content
            return r.status, json.loads(content.decode("utf-8"))
    except urllib.error.HTTPError as e:
        content = e.read()
        try:
            return e.code, json.loads(content.decode("utf-8"))
        except Exception:
            return e.code, content[:200]


def login(username, password):
    st, cap = http("/captchaImage")
    uuid = cap.get("uuid")
    code = None
    if cap.get("captchaEnabled"):
        v = redis_get("captcha_codes:" + uuid)
        if v:
            v = v.strip()
            if v.startswith('"') and v.endswith('"'):
                v = v[1:-1]           # Redis 里是 FastJson 序列化的字符串，带引号
            ans = v.split("@")[0]
            if "=" in ans:
                expr = ans.split("=")[0]
                code = str(eval(expr))
            else:
                code = ans
    st, r = http("/login", "POST", body={"username": username, "password": password, "code": code, "uuid": uuid})
    return r.get("token") if isinstance(r, dict) else None, r


if __name__ == "__main__":
    user = sys.argv[1] if len(sys.argv) > 1 else "admin"
    token, r = login(user, "123456")
    print(json.dumps({"user": user, "login": r.get("code") if isinstance(r, dict) else r, "token": (token or "")[:12]}, ensure_ascii=False))
    if token:
        for p in ["/getInfo", "/kids/child/list?pageNum=1&pageSize=5", "/kids/notice/list?pageNum=1&pageSize=5"]:
            st, body = http(p, token=token)
            if isinstance(body, dict):
                print(p, st, "total=", body.get("total"), "rows=", len(body.get("rows") or body.get("data") or []))
            else:
                print(p, st, str(body)[:120])
