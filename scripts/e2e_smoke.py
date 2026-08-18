"""수령자 플로우 E2E 스모크 테스트.

실행 중인 앱에 실제 HTTP 요청을 보내 전체 흐름을 확인한다.
선물 생성 → 초대 → (비회원) 취향 제출 → 추천 → 확정 → 배송 → 공개,
그리고 다른 회원이 남의 선물 세션에 손대지 못하는지까지.

MockMvc 슬라이스 테스트가 못 잡는 것을 잡는다. 시큐리티 필터 체인이 실제로
어떤 순서로 도는지, 비회원 경로가 정말 열려 있는지, 상태 전이가 실제 DB에서
이어지는지는 앱을 띄워봐야 안다.

사용:
    python scripts/e2e_smoke.py                                  # 기본 localhost:8080
    E2E_BASE=http://localhost:8081 python scripts/e2e_smoke.py

표준 라이브러리만 쓴다. 매 실행마다 새 계정·새 선물 세션을 만들어서
반복 실행해도 안전하다.
"""
import json
import os
import time
import urllib.request
import urllib.error

BASE = os.environ.get("E2E_BASE", "http://localhost:8080")


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            return e.code, json.loads(raw)
        except json.JSONDecodeError:
            return e.code, raw


def show(step, status, body, keys=None):
    ok = isinstance(body, dict) and body.get("success")
    mark = "OK " if ok else "FAIL"
    detail = ""
    if not ok:
        detail = json.dumps(body, ensure_ascii=False)[:200]
    elif keys:
        d = body.get("data")
        if isinstance(d, dict):
            detail = json.dumps({k: d.get(k) for k in keys}, ensure_ascii=False)
        elif isinstance(d, list):
            detail = f"{len(d)}건 " + json.dumps(
                [{k: x.get(k) for k in keys} for x in d], ensure_ascii=False)[:400]
    print(f"[{mark}] {step} ({status}) {detail}")
    return ok


stamp = int(time.time())
email = f"e2e{stamp}@test.com"

# 1) 선물자 가입
s, b = call("POST", "/api/auth/signup", {
    "email": email, "password": "password123", "nickname": "e2e-gifter",
    "phone": "01011112222",
    "terms": [{"termsType": "SERVICE", "version": "1.0", "agreed": True},
              {"termsType": "PRIVACY", "version": "1.0", "agreed": True}],
})
assert show("1. 선물자 가입", s, b, ["accessToken"]), "가입 실패"
token = b["data"]["accessToken"]

# 2) 선물 세션 생성
s, b = call("POST", "/api/gift-sessions", {
    "relationship": "여자친구", "occasion": "취업 축하",
    "budgetMin": 100000, "budgetMax": 900000,
    "category": "가방", "brand": "MCM",
    "meaning": "오래 사용할 수 있는 선물이면 좋겠어요",
    "moods": ["PRACTICAL"],
    "giverKnownTaste": {"colors": "블랙,다크브라운", "style": "심플",
                        "avoid": "큰 로고", "wearStyle": "숄더백"},
}, token)
assert show("2. 선물 세션 생성", s, b, ["id", "status"]), "세션 생성 실패"
gid = b["data"]["id"]

# 3) 초대 생성
s, b = call("POST", f"/api/gift-sessions/{gid}/invite", None, token)
assert show("3. 초대 생성", s, b, ["inviteToken", "inviteCode"]), "초대 실패"
invite_token = b["data"]["inviteToken"]
invite_code = b["data"]["inviteCode"]

# 4) 수령자: 초대 확인 (토큰 없이 — 여기부터 비회원)
s, b = call("GET", f"/api/invite/{invite_token}")
assert show("4. [비회원] 초대 확인", s, b, ["brand", "category"]), "초대 확인 실패"
assert "productName" not in json.dumps(b), "블라인드 위반: 상품명 노출"

# 5) 수령자: 코드 검증
s, b = call("POST", "/api/invite/verify", {"code": invite_code})
assert show("5. [비회원] 초대 코드 검증", s, b, ["giftSessionId"]), "코드 검증 실패"

# 6) 수령자: 취향 폼
s, b = call("GET", f"/api/invite/{invite_token}/taste-form")
assert show("6. [비회원] 취향 폼 조회", s, b, ["category"]), "폼 조회 실패"
steps = {st["key"]: st for st in b["data"]["steps"]}
print("     문항:", list(steps))


def first(key):
    return steps[key]["options"][0]["code"] if key in steps else None


payload = {
    "colors": [first("colors")],
    "mood": first("mood"),
    "material": first("material"),
    "logoVisibility": first("logoVisibility"),
    "size": first("size"),
    "avoid": [],
}
if "wearStyle" in steps:
    payload["wearStyle"] = first("wearStyle")

# 7) 수령자: 취향 제출
s, b = call("POST", f"/api/invite/{invite_token}/preferences", payload)
assert show("7. [비회원] 취향 제출", s, b), "취향 제출 실패"

# 8) 중복 제출은 막혀야 한다
s, b = call("POST", f"/api/invite/{invite_token}/preferences", payload)
print(f"[{'OK ' if not b.get('success') else 'FAIL'}] 8. 취향 재제출 차단 ({s}) "
      f"{json.dumps(b.get('error'), ensure_ascii=False)}")

# 9) 선물자: 추천 생성
s, b = call("POST", f"/api/gift-sessions/{gid}/recommendations", None, token)
assert show("9. 추천 생성", s, b, ["recommendationId", "rank", "matchRate", "productName"]), "추천 실패"
recs = b["data"]
rec_id = recs[0]["recommendationId"]

# 10) 추천 상세 — AI 코멘트 확인
s, b = call("GET", f"/api/recommendations/{rec_id}", None, token)
assert show("10. 추천 상세", s, b, ["matchRate", "aiComment"]), "상세 실패"

# 11) 최종 확정
s, b = call("POST", f"/api/gift-sessions/{gid}/finalize", {"recommendationId": rec_id}, token)
assert show("11. 최종 확정", s, b), "확정 실패"

# 12) 완료 전 공개 시도 — 숨겨져 있어야 한다
s, b = call("GET", f"/api/invite/{invite_token}/reveal")
revealed = b.get("data", {}).get("revealed")
print(f"[{'OK ' if revealed is False else 'FAIL'}] 12. [비회원] 완료 전 공개 차단 ({s}) revealed={revealed}")

# 13) 전달 정보 입력
s, b = call("POST", f"/api/gift-sessions/{gid}/delivery", {
    "method": "SHIP", "message": "취업 축하해!", "recipientName": "받는사람",
    "address": "서울시 어딘가 123", "phone": "01033334444",
}, token)
assert show("13. 전달 정보 입력", s, b, ["method", "giftStatus"]), "전달 실패"

# 14) 선물 완료 처리
s, b = call("POST", f"/api/gift-sessions/{gid}/complete", None, token)
assert show("14. 선물 완료 처리", s, b), "완료 실패"

# 15) 공개
s, b = call("GET", f"/api/invite/{invite_token}/reveal")
ok = show("15. [비회원] 선물 공개", s, b, ["revealed", "productName", "brand", "price", "message"])
assert ok and b["data"]["revealed"] is True, "공개 실패"

# 16) 남의 선물 세션에 접근 — 전부 막혀야 한다
s, b = call("POST", "/api/auth/signup", {
    "email": f"other{stamp}@test.com", "password": "password123", "nickname": "other",
    "phone": "01099998888",
    "terms": [{"termsType": "SERVICE", "version": "1.0", "agreed": True},
              {"termsType": "PRIVACY", "version": "1.0", "agreed": True}],
})
assert b.get("success"), "2번 사용자 가입 실패"
other = b["data"]["accessToken"]

blocked = [
    ("GET  세션 단건", "GET", f"/api/gift-sessions/{gid}", None),
    ("POST 초대 생성", "POST", f"/api/gift-sessions/{gid}/invite", None),
    ("POST 추천 생성", "POST", f"/api/gift-sessions/{gid}/recommendations", None),
    ("GET  추천 목록", "GET", f"/api/gift-sessions/{gid}/recommendations", None),
    ("GET  추천 상세", "GET", f"/api/recommendations/{rec_id}", None),
    ("POST 최종 확정", "POST", f"/api/gift-sessions/{gid}/finalize", {"recommendationId": rec_id}),
    ("GET  전달 정보", "GET", f"/api/gift-sessions/{gid}/delivery", None),
    ("POST 전달 정보", "POST", f"/api/gift-sessions/{gid}/delivery",
     {"method": "SHIP", "recipientName": "탈취", "address": "여기로 보내", "phone": "01000000000"}),
    ("POST 수령 완료", "POST", f"/api/gift-sessions/{gid}/complete", None),
]
all_blocked = True
for label, method, path, body in blocked:
    s, b = call(method, path, body, other)
    code = (b.get("error") or {}).get("code") if isinstance(b, dict) else None
    ok = s == 403 and code == "G002"
    all_blocked &= ok
    print(f"[{'OK ' if ok else 'FAIL'}] 16. 타인 접근 차단 — {label} ({s} {code})")

s, b = call("GET", "/api/gift-sessions", None, other)
empty = b.get("data") == []
print(f"[{'OK ' if empty else 'FAIL'}] 17. 타인의 목록에 내 세션이 안 보인다 (건수={len(b.get('data') or [])})")

assert all_blocked and empty, "소유자 격리 실패"

print("\n=== E2E 전체 통과 ===")
