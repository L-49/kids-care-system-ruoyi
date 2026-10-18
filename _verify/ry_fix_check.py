# -*- coding: utf-8 -*-
"""若依版修复回归：幼儿档案（调班/导入/校验/权限）+ 通知公告（发布时间/家长可见/字典）。"""
import datetime
import io
import json
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid

sys.path.insert(0, r"D:\project\work2\kids-care-交付包-20261018\_verify")
from ry_api import BASE, http, login  # noqa: E402

import openpyxl  # noqa: E402

MYSQL = r"D:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
DB = "kids_care_ry"
P = "__修复验证"
HEADERS = ["姓名", "性别", "出生日期", "所在班级", "家长姓名", "家长电话", "家庭住址", "过敏史/禁忌", "入园日期", "状态"]

results = []


def rec(tag, ok, detail=""):
    results.append({"case": tag, "pass": bool(ok), "detail": str(detail)[:220]})
    print(("PASS " if ok else "FAIL ") + tag + ("  |  " + str(detail)[:160] if detail else ""))


def sql(q):
    r = subprocess.run([MYSQL, "-uroot", "-p123456", "--default-character-set=utf8mb4", "-N", "-e", q],
                       capture_output=True, text=True, encoding="utf-8", errors="replace")
    return r.stdout.strip()


def cleanup():
    sql("update {0}.child set deleted=1 where name like '{1}%';"
        "update {0}.notice set deleted=1 where title like '{1}%';".format(DB, P))


def cm(b):
    if isinstance(b, dict):
        return b.get("code"), b.get("msg")
    return None, str(b)[:160]


def upload(path, token, filename, data):
    boundary = "----wb" + uuid.uuid4().hex
    body = (("--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n"
             "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n")
            % (boundary, filename)).encode() + data + ("\r\n--%s--\r\n" % boundary).encode()
    req = urllib.request.Request(BASE + path, data=body, method="POST",
                                 headers={"Content-Type": "multipart/form-data; boundary=" + boundary,
                                          "Authorization": "Bearer " + token})
    op = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    try:
        with op.open(req, timeout=30) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")[:200]


def make_xlsx(rows):
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.append(HEADERS)
    for r in rows:
        ws.append(r)
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


def child_by_name(token, name):
    st, b = http("/kids/child/list?pageNum=1&pageSize=5&name=" + urllib.parse.quote(name), token=token)
    rows = (b or {}).get("rows") or []
    return rows[0] if rows else None


def notice_by_title(token, title):
    st, b = http("/kids/notice/list?pageNum=1&pageSize=5&title=" + urllib.parse.quote(title), token=token)
    rows = (b or {}).get("rows") or []
    return rows[0] if rows else None


def routers_menus(token):
    st, b = http("/getRouters", token=token)
    return (b or {}).get("data") or []


def router_paths(menus):
    """展开成 (父路径, 子路径) 便于断言"""
    out = []
    for m in menus:
        out.append(("", m.get("path")))
        for c in (m.get("children") or []):
            out.append((m.get("path"), c.get("path")))
    return out


def main():
    cleanup()
    admin, _ = login("admin", "123456")
    teacher, _ = login("teacher", "123456")
    parent, _ = login("parent", "123456")
    if not (admin and teacher and parent):
        print("登录失败", bool(admin), bool(teacher), bool(parent))
        return 1

    # ---------------- 幼儿档案：新增校验 ----------------
    cases = [
        ("空姓名被拒", {"name": "", "gender": "男", "clazzId": 1, "status": 1}, "姓名"),
        ("未选班级被拒", {"name": P + "无班级", "gender": "男", "status": 1}, "班级"),
        ("非法手机号被拒", {"name": P + "错号", "gender": "男", "clazzId": 1, "guardianPhone": "123"}, "手机号"),
        ("非法性别被拒", {"name": P + "错性别", "gender": "男性", "clazzId": 1}, "性别"),
        ("非法状态被拒", {"name": P + "错状态", "gender": "男", "clazzId": 1, "status": 5}, "状态"),
    ]
    for tag, body, kw in cases:
        st, b = http("/kids/child", "POST", token=admin, body=body)
        c, m = cm(b)
        rec(tag, c != 200 and kw in (m or ""), "code=%s msg=%s" % (c, m))
    rec("非法请求未写入脏数据", child_by_name(admin, P + "无班级") is None)

    # ---------------- 幼儿档案：局部更新不清空 ----------------
    st, b = http("/kids/child", "POST", token=admin,
                 body={"name": P + "甲", "gender": "女", "birthday": "2021-05-01", "clazzId": 1,
                       "guardianName": "验证家长", "guardianPhone": "13800001111", "status": 1})
    child = child_by_name(admin, P + "甲")
    cid = (child or {}).get("id")
    rec("新增正常档案", cid is not None, "id=%s" % cid)

    st, b = http("/kids/child", "PUT", token=admin, body={"id": cid, "guardianPhone": "13900002222"})
    rec("局部修改接口返回", cm(b)[0] == 200, cm(b))
    st, b = http("/kids/child/%s" % cid, token=admin)
    d = (b or {}).get("data") or {}
    rec("局部修改未清空其它字段",
        d.get("name") == P + "甲" and d.get("gender") == "女" and d.get("clazzId") == 1 and d.get("guardianName") == "验证家长",
        "name=%s gender=%s clazzId=%s guardian=%s" % (d.get("name"), d.get("gender"), d.get("clazzId"), d.get("guardianName")))
    rec("局部修改生效", d.get("guardianPhone") == "13900002222", d.get("guardianPhone"))

    # ---------------- 幼儿档案：调班 ----------------
    rows = sql("select c.id, c.name, c.capacity, (select count(*) from %s.child ch where ch.clazz_id=c.id "
               "and ch.deleted=0 and ch.status=1) from %s.clazz c where c.deleted=0 order by c.id" % (DB, DB))
    target = None
    for line in rows.splitlines():
        f = line.split("\t")
        if len(f) == 4 and int(f[0]) != 1 and int(f[3]) < int(f[2]):
            target = (int(f[0]), f[1])
            break
    if target:
        tid, tname = target
        st, b = http("/kids/child/%s/transfer" % cid, "POST", token=admin, body={"targetClazzId": tid})
        rec("调班成功", cm(b)[0] == 200, cm(b))
        st, b = http("/kids/child/%s" % cid, token=admin)
        d = (b or {}).get("data") or {}
        rec("调班后班级正确", d.get("clazzName") == tname, "%s -> %s" % (d.get("clazzName"), tname))
        rec("调班后其它字段保留",
            d.get("name") == P + "甲" and d.get("guardianPhone") == "13900002222" and d.get("gender") == "女",
            "name=%s phone=%s" % (d.get("name"), d.get("guardianPhone")))
        st, b = http("/kids/child/%s/history" % cid, token=admin)
        types = [(x.get("changeType"), x.get("newValue")) for x in ((b or {}).get("data") or [])]
        rec("调班写变更记录", any(t == "调班" and v == tname for t, v in types), types[:4])
        st, b = http("/kids/child/%s/transfer" % cid, "POST", token=admin, body={"targetClazzId": tid})
        rec("重复调同班被拒", "已在目标班级" in (cm(b)[1] or ""), cm(b))
    else:
        rec("调班用例跳过（无空余班级）", False, rows)

    # ---------------- 幼儿档案：教师不能删 ----------------
    st, b = http("/kids/child/%s" % cid, "DELETE", token=teacher)
    rec("教师删除幼儿被拒", cm(b)[0] == 403, cm(b))

    # ---------------- 幼儿档案：Excel 导入 ----------------
    st, raw = http("/kids/child/import/template", "POST", token=admin, raw=True)
    wb = openpyxl.load_workbook(io.BytesIO(raw))
    heads = [c.value for c in wb.active[1]]
    rec("模板可下载", "姓名" in [str(h) for h in heads], heads)

    single = make_xlsx([[P + "导入1", "男", "2021-03-02", "中班", "家长一", "13800003333", "", "", "2024-09-01", "在园"]])
    st, b = upload("/kids/child/import", admin, "one.xlsx", single)
    ok = (b or {}).get("data") or {}
    rec("单行导入成功", st == 200 and ok.get("success") == 1 and ok.get("fail") == 0, ok)

    multi = make_xlsx([
        [P + "导入2", "女", "2021-04-03", "中班", "家长二", "13800004444", "", "花生过敏", "2024-09-01", "在园"],
        [P + "导入3", "男", "2021-05-04", "不存在的班", "家长三", "13800005555", "", "", "2024-09-01", "在园"],
        [P + "导入4", "男", "2021-06-05", "", "家长四", "13800006666", "", "", "2024-09-01", "在园"],
    ])
    st, b = upload("/kids/child/import", admin, "multi.xlsx", multi)
    data = (b or {}).get("data") or {}
    errs = " ".join(data.get("errors") or [])
    rec("多行导入按行统计", data.get("success") == 1 and data.get("fail") == 2, data)
    rec("导入提示班级不存在", "不存在" in errs, errs[:160])
    rec("导入提示班级必填", "所在班级不能为空" in errs, errs[:160])

    # ---------------- 通知公告：发布时间规则 ----------------
    future = (datetime.datetime.now() + datetime.timedelta(hours=2)).strftime("%Y-%m-%d %H:%M:%S")
    past = (datetime.datetime.now() - datetime.timedelta(hours=2)).strftime("%Y-%m-%d %H:%M:%S")

    st, b = http("/kids/notice", "POST", token=admin,
                 body={"title": P + "提前发布", "content": "x", "type": "通知", "status": 2, "publishTime": future, "targetType": "all"})
    rec("已发布+未来时间被拒", "晚于当前时间" in (cm(b)[1] or ""), cm(b))

    st, b = http("/kids/notice", "POST", token=admin,
                 body={"title": P + "过期定时", "content": "x", "type": "通知", "status": 1, "publishTime": past, "targetType": "all"})
    rec("定时+过去时间被拒", "必须晚于当前时间" in (cm(b)[1] or ""), cm(b))

    st, b = http("/kids/notice", "POST", token=admin,
                 body={"title": "", "content": "x", "type": "通知", "status": 0, "targetType": "all"})
    rec("空标题被拒", "标题不能为空" in (cm(b)[1] or ""), cm(b))

    st, b = http("/kids/notice", "POST", token=admin,
                 body={"title": P + "定时公告", "content": "x", "type": "通知", "status": 1, "publishTime": future, "targetType": "all"})
    rec("定时公告创建成功", cm(b)[0] == 200, cm(b))

    st, b = http("/kids/notice", "POST", token=admin,
                 body={"title": P + "立即公告", "content": "x", "type": "通知", "status": 2, "targetType": "all"})
    rec("立即发布创建成功", cm(b)[0] == 200, cm(b))

    # ---------------- 通知公告：家长可见性 ----------------
    st, b = http("/kids/notice/all", token=parent)
    rows = (b or {}).get("data") or []
    titles = [n.get("title") for n in rows]
    rec("家长可读公告接口", cm(b)[0] == 200, cm(b)[0])
    rec("定时未到点家长看不到", (P + "定时公告") not in titles)
    rec("已发布家长能看到", (P + "立即公告") in titles,
        "count=%d ids=%s" % (len(rows), [n.get("id") for n in rows][:8]))

    st, b = http("/kids/notice/list?pageNum=1&pageSize=10", token=parent)
    rec("家长可访问公告列表", cm(b)[0] == 200, cm(b)[0])
    statuses = {r.get("status") for r in ((b or {}).get("rows") or [])}
    rec("家长列表只含已发布", statuses <= {2}, statuses)

    # 历史脏数据：直接改成「已发布 + 未来时间」，家长接口仍不应返回（Mapper 双保险）
    nid = (notice_by_title(admin, P + "定时公告") or {}).get("id")
    sql("update %s.notice set status=2 where id=%s" % (DB, nid))
    st, b = http("/kids/notice/all", token=parent)
    titles = [n.get("title") for n in ((b or {}).get("data") or [])]
    rec("脏数据也不会提前可见", (P + "定时公告") not in titles)
    sql("update %s.notice set status=1 where id=%s" % (DB, nid))

    # ---------------- 通知公告：局部更新 ----------------
    nid2 = (notice_by_title(admin, P + "立即公告") or {}).get("id")
    st, b = http("/kids/notice", "PUT", token=admin, body={"id": nid2, "title": P + "立即公告改"})
    rec("公告局部修改返回", cm(b)[0] == 200, cm(b))
    st, b = http("/kids/notice/%s" % nid2, token=admin)
    d = (b or {}).get("data") or {}
    rec("公告局部修改未清空", d.get("status") == 2 and d.get("type") == "通知" and d.get("content") == "x",
        "status=%s type=%s content=%s" % (d.get("status"), d.get("type"), d.get("content")))

    # ---------------- 菜单与字典 ----------------
    p_routes = router_paths(routers_menus(parent))
    a_routes = router_paths(routers_menus(admin))
    rec("家长菜单含公告入口", ("/mychild", "notice") in p_routes, [r for r in p_routes if r[0] == "/mychild"])
    rec("园长菜单不含家长端", all("mychild" not in (x or "") for pair in a_routes for x in pair),
        [r for r in a_routes if "mychild" in str(r)])

    st, b = http("/system/dict/data/type/notice_type", token=admin)
    dicts = [x.get("dictLabel") for x in ((b or {}).get("data") or [])]
    rec("公告类型字典 7 项", len(dicts) == 7 and "园务" in dicts, dicts)

    cleanup()
    alive = sql("select (select count(*) from {0}.child where deleted=0), (select count(*) from {0}.notice where deleted=0)".format(DB))
    print("清理后存活数据（child / notice）：", alive)

    total = len(results)
    passed = sum(1 for r in results if r["pass"])
    print("\n=== %d/%d 通过 ===" % (passed, total))
    with open(r"D:\project\work2\kids-care-交付包-20261018\_verify\ry_fix_result.json", "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
