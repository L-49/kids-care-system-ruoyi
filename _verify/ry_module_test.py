# -*- coding: utf-8 -*-
"""若依版：幼儿档案 / 通知公告 两模块功能实测（只针对隔离库 kids_care_ry）"""
import json
import sys

from ry_api import http, login

results = []


def rec(module, name, ok, detail=""):
    results.append({"module": module, "name": name, "ok": ok, "detail": detail})
    print(("[OK]  " if ok else "[FAIL]") + " %s | %s | %s" % (module, name, detail))


def body(resp):
    st, b = resp
    return st, b


admin, r = login("admin", "123456")
teacher, _ = login("teacher", "123456")
parent, _ = login("parent", "123456")
print("tokens:", bool(admin), bool(teacher), bool(parent))
assert admin and teacher and parent

A = lambda p, m="GET", body=None, raw=False: http(p, m, token=admin, body=body, raw=raw)
T = lambda p, m="GET", body=None: http(p, m, token=teacher, body=body)
P = lambda p, m="GET", body=None: http(p, m, token=parent, body=body)

# ---------------- 幼儿档案 ----------------
st, b = A("/kids/child/list?pageNum=1&pageSize=10")
rec("幼儿档案", "分页列表", st == 200 and b.get("total", 0) > 0, "total=%s rows=%s" % (b.get("total"), len(b.get("rows") or [])))

st, b = A("/kids/child/list?pageNum=1&pageSize=10&name=王")
rec("幼儿档案", "按姓名模糊查询", st == 200 and b.get("total", 0) >= 1, "王 → total=%s" % b.get("total"))

st, b = A("/kids/child/list?pageNum=1&pageSize=10&clazzId=1")
rec("幼儿档案", "按班级筛选", st == 200, "clazzId=1 → total=%s" % b.get("total"))

st, b = A("/kids/child/list?pageNum=1&pageSize=50&incompleteOnly=true")
rec("幼儿档案", "仅看待补全", st == 200, "total=%s" % b.get("total"))

st, b = A("/kids/child/1")
row = b.get("data") or {}
rec("幼儿档案", "详情", st == 200 and row.get("id") == 1,
    "name=%s clazzName=%s completeness=%s missing=%s" % (row.get("name"), row.get("clazzName"), row.get("completeness"), row.get("missingItems")))

st, b = A("/kids/child/allergy")
rec("幼儿档案", "过敏幼儿清单", st == 200 and isinstance(b.get("data"), list), "count=%s" % len(b.get("data") or []))

st, b = A("/kids/child/all")
rec("幼儿档案", "在园幼儿下拉(all)", st == 200 and isinstance(b.get("data"), list), "count=%s" % len(b.get("data") or []))

# 新增（正常）
st, b = A("/kids/child", "POST", {"name": "__验证幼儿", "gender": "男", "clazzId": 2,
                                  "guardianName": "__验证家长", "guardianPhone": "13800001111",
                                  "birthday": "2021-05-01", "enrollDate": "2024-09-01", "status": 1})
new_id = b.get("data") if isinstance(b.get("data"), int) else None
if not new_id:
    st2, b2 = A("/kids/child/list?pageNum=1&pageSize=1&name=__验证幼儿")
    new_id = ((b2.get("rows") or [{}])[0] or {}).get("id")
rec("幼儿档案", "新增", st == 200 and b.get("code") == 200, "msg=%s id=%s" % (b.get("msg"), new_id))

# 新增（空姓名）
st, b = A("/kids/child", "POST", {"name": "", "clazzId": 2})
rec("幼儿档案", "空姓名应被拒绝", b.get("code") != 200, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# 新增（缺班级）
st, b = A("/kids/child", "POST", {"name": "__验证无班级"})
no_clazz_id = b.get("data") if isinstance(b.get("data"), int) else None
rec("幼儿档案", "缺班级是否被拒绝", b.get("code") != 200, "code=%s msg=%s（期望被拒绝）" % (b.get("code"), b.get("msg")))

# 新增（非法手机号）
st, b = A("/kids/child", "POST", {"name": "__验证坏号码", "clazzId": 2, "guardianPhone": "123"})
bad_phone_id = b.get("data") if isinstance(b.get("data"), int) else None
rec("幼儿档案", "非法手机号是否被拒绝", b.get("code") != 200, "code=%s msg=%s（期望被拒绝）" % (b.get("code"), b.get("msg")))

# 修改：只传部分字段，观察是否把其他字段清空
if new_id:
    st, b = A("/kids/child", "PUT", {"id": new_id, "name": "__验证幼儿改", "clazzId": 2, "status": 1})
    st2, b2 = A("/kids/child/%s" % new_id)
    d = b2.get("data") or {}
    rec("幼儿档案", "局部修改是否覆盖其他字段", d.get("guardianPhone") == "13800001111",
        "改后 guardianName=%s guardianPhone=%s birthday=%s" % (d.get("guardianName"), d.get("guardianPhone"), d.get("birthday")))

# 调班
if new_id:
    st, b = A("/kids/child/%s/transfer" % new_id, "POST", {"targetClazzId": 3})
    st2, b2 = A("/kids/child/%s/history" % new_id)
    logs = b2.get("data") or []
    rec("幼儿档案", "调班+留痕", st == 200 and len(logs) >= 1,
        "调班code=%s 变更记录=%s 条，最新=%s" % (b.get("code"), len(logs), (logs[0] if logs else {}).get("changeType")))

# 调班容量校验：把班级容量压到 1 再调
st, b = A("/kids/child/%s/transfer" % new_id, "POST", {"targetClazzId": None})
rec("幼儿档案", "调班缺参数校验", b.get("code") != 200, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# 导出 / 模板
st, raw = A("/kids/child/export?pageNum=1&pageSize=10", "POST", raw=True)
rec("幼儿档案", "Excel 导出", st == 200 and raw[:2] == b"PK", "status=%s bytes=%s magic=%s" % (st, len(raw), raw[:2]))
st, raw = A("/kids/child/import/template", "POST", raw=True)
rec("幼儿档案", "导入模板下载", st == 200 and raw[:2] == b"PK", "status=%s bytes=%s" % (st, len(raw)))

# 教师视角
st, b = T("/kids/child/list?pageNum=1&pageSize=5")
rec("幼儿档案", "教师可查列表", st == 200 and b.get("total", 0) > 0, "teacher total=%s" % b.get("total"))
st, b = T("/kids/child", "POST", {"name": "__验证教师新增", "clazzId": 2})
t_add_ok = b.get("code") == 200
t_add_id = b.get("data") if isinstance(b.get("data"), int) else None
rec("幼儿档案", "教师可新增", t_add_ok, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# 家长视角
st, b = P("/kids/child/list?pageNum=1&pageSize=5")
rec("幼儿档案", "家长越权查列表应 403", b.get("code") == 403 or st == 403, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# ---------------- 通知公告 ----------------
st, b = A("/kids/notice/list?pageNum=1&pageSize=10")
rec("通知公告", "分页列表", st == 200 and b.get("total", 0) > 0, "total=%s rows=%s" % (b.get("total"), len(b.get("rows") or [])))

st, b = A("/kids/notice/list?pageNum=1&pageSize=10&title=春季")
rec("通知公告", "按标题筛选", st == 200, "title=春季 → total=%s" % b.get("total"))

st, b = A("/kids/notice/list?pageNum=1&pageSize=10&type=通知")
rec("通知公告", "按类型筛选", st == 200, "type=通知 → total=%s" % b.get("total"))

st, b = A("/kids/notice/list?pageNum=1&pageSize=10&status=3")
rec("通知公告", "按状态筛选(已撤回)", st == 200, "status=3 → total=%s" % b.get("total"))

# 新增草稿
st, b = A("/kids/notice", "POST", {"title": "__验证公告", "content": "自动化验证内容", "type": "通知",
                                   "status": 0, "targetType": "all"})
nid = b.get("data") if isinstance(b.get("data"), int) else None
if not nid:
    st2, b2 = A("/kids/notice/list?pageNum=1&pageSize=1&title=__验证公告")
    nid = ((b2.get("rows") or [{}])[0] or {}).get("id")
rec("通知公告", "新增草稿", st == 200 and b.get("code") == 200, "msg=%s id=%s" % (b.get("msg"), nid))

st, b = A("/kids/notice/%s" % nid)
d = b.get("data") or {}
rec("通知公告", "详情", d.get("id") == nid, "title=%s status=%s targetLabel=%s" % (d.get("title"), d.get("status"), d.get("targetLabel")))

if nid:
    st, b = A("/kids/notice", "PUT", {"id": nid, "title": "__验证公告改", "content": "改过", "type": "通知", "status": 0})
    rec("通知公告", "编辑", b.get("code") == 200, "msg=%s" % b.get("msg"))

    st, b = A("/kids/notice/%s/publish" % nid, "POST")
    st2, b2 = A("/kids/notice/%s" % nid)
    rec("通知公告", "发布", b.get("code") == 200 and (b2.get("data") or {}).get("status") == 2,
        "发布code=%s 状态=%s 时间=%s" % (b.get("code"), (b2.get("data") or {}).get("status"), (b2.get("data") or {}).get("publishTime")))

    st, b = A("/kids/notice/%s/revoke" % nid, "POST")
    st2, b2 = A("/kids/notice/%s" % nid)
    rec("通知公告", "撤回", b.get("code") == 200 and (b2.get("data") or {}).get("status") == 3,
        "撤回code=%s 状态=%s" % (b.get("code"), (b2.get("data") or {}).get("status")))

    # 已撤回能否直接再发布
    st, b = A("/kids/notice/%s/publish" % nid, "POST")
    rec("通知公告", "已撤回不可再发布", b.get("code") != 200, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# 定时公告：状态1 + 未来时间
st, b = A("/kids/notice", "POST", {"title": "__验证定时公告", "content": "定时", "type": "通知",
                                   "status": 1, "publishTime": "2026-12-31 08:00:00", "targetType": "all"})
sid = b.get("data") if isinstance(b.get("data"), int) else None
rec("通知公告", "新增定时公告", b.get("code") == 200, "code=%s msg=%s id=%s" % (b.get("code"), b.get("msg"), sid))

st, b = A("/kids/notice", "POST", {"title": "__验证定时无时间", "content": "x", "type": "通知", "status": 1})
rec("通知公告", "定时公告缺发布时间应拒绝", b.get("code") != 200, "code=%s msg=%s" % (b.get("code"), b.get("msg")))

# 已发布但发布时间填未来：是否允许（漏洞点）
st, b = A("/kids/notice", "POST", {"title": "__验证未来发布", "content": "x", "type": "通知",
                                   "status": 2, "publishTime": "2026-12-31 08:00:00"})
fid = b.get("data") if isinstance(b.get("data"), int) else None
rec("通知公告", "已发布状态可否填未来时间", b.get("code") == 200,
    "code=%s msg=%s（若允许则家长端会提前看到未到点公告）" % (b.get("code"), b.get("msg")))

# 家长可见范围
st, b = P("/kids/notice/all")
plist = b.get("data") or []
labels = sorted({(n.get("targetLabel") or "") for n in plist})
rec("通知公告", "家长端可见列表", st == 200 and len(plist) > 0,
    "家长可见=%s 条；可见范围=%s；是否含未来公告=%s" % (
        len(plist), labels, any(n.get("id") == fid for n in plist)))

st, b = P("/kids/notice/list?pageNum=1&pageSize=50")
plist2 = b.get("rows") or []
rec("通知公告", "家长端列表只含已发布", st == 200 and all(n.get("status") == 2 for n in plist2),
    "家长列表=%s 条 状态集合=%s" % (len(plist2), sorted({n.get("status") for n in plist2})))

st, b = T("/kids/notice/list?pageNum=1&pageSize=5")
rec("通知公告", "教师可查公告", st == 200 and b.get("total", 0) > 0, "teacher total=%s" % b.get("total"))

# 清理：删掉本次创建的幼儿与公告
ids = [i for i in [new_id, no_clazz_id, bad_phone_id, t_add_id] if isinstance(i, int)]
for i in ids:
    A("/kids/child/%s" % i, "DELETE")
nids = [i for i in [nid, sid, fid] if isinstance(i, int)]
if nids:
    A("/kids/notice/" + ",".join(str(i) for i in nids), "DELETE")
print("cleaned child ids:", ids, "notice ids:", nids)

with open("_verify/ry_module_result.json", "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)
print("saved", len(results), "checks")
