JWT 기본 과제
========

**목표**

- **사용자 인증 시스템**을 구축합니다. (회원가입, 로그인)
- **JWT(Json Web Token) 기반 인증 메커니즘**을 구현하여 보안성을 강화합니다.
- **역할(Role) 기반 접근 제어**를 적용하여 관리자(Admin) 권한이 필요한 API를 보호합니다.

--------------------------------------
**API명세**



|기능|	method|	url	|request	|response	|status|
|-------------|----|---------------|-----------|---------|--------------|
|회원 가입|POST|	/auth/signup|	요청 body	|등록 정보	|200: 정상 등록|
|로그인|POST|/auth/signin|	요청 body	|단건 응답 정보	|200: 정상 조회|
|관리자 권한부여|PUT|/admin/users/{userId}/roles|	요청 param|	단건 응답 정보|	200: 정상 조회|



프로젝트 실행 후 http://localhost:8080/swagger-ui/index.html#/ 에서도 확인할 수 있다.

