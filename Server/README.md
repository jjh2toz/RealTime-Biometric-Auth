# realtime_authentication

1. server/members/create/ **(POST)**

   회원가입

   > - request (JSON string 형식)
   >
   > ```
   > {
   >     "name": "HyeongGeun Oh",
   >     "username": "hyeonggeun2",
   >     "password": "gudrms12"
   > }
   > ```
   >
   > 
   >
   > - response
   >
   > ```
   > {
   >     "user": {
   >         "username": "hyeonggeun2",
   >         "name": "HyeongGeun Oh",
   >     }
   >     "token": "e4c0d0daaf2cc4ee2190a033957c1aa8c9eca9a5",
   >     "code": 200
   > }
   > ```

   

2. server/members/login/ **(POST)**

   로그인 및 TOKEN 값을 얻습니다.

   > - request
   >
   > ```
   > {
   >     "username": "hyeonggeun21",
   >     "password": "gudrms12"
   > }
   > ```
   >
   > 
   >
   > - response
   >
   > ```
   > {
   >     "token": "e4c0d0daaf2cc4ee2190a033957c1aa8c9eca9a5",
   >     "user": {
   >         "pk": 1,
   >         "username": "hyeonggeun21",
   >         "name": "HyeongGeun Oh",
   >         "age": 26,
   >         "gender": "male"
   >     }
   >     "code": "login_success",
   > }
   > ```

   

3. server/members/logout/ **(GET)**

   로그아웃(토큰 삭제)합니다.

   >- Header에 값 추가
   >
   >```
   >{
   >    Authorization: Token add7188de45b08a2b53944e5f6b95a43053c2ebb
   >}
   >```
   >
   >
   >
   >- response
   >
   >```
   >{
   >    "detail": "로그아웃 되었습니다."
   >}
   >```

   

4. server/authenticate/make_model/ **(POST)**

    데이터를 받아서 모델을 만듭니다.

   >- request
   >
   >```
   >{
   >	"token": e4c0d0daaf2cc4ee2190a033957c1aa8c9eca9a5,
   >	"data": (FILE)
   >}
   >```
   >
   >
   >
   >- response
   >
   >```
   >{
   >    "detail": "저장완료."
   >}
   >```

   
   
5. server/authenticate/check_user/ **(GET)**
   해당 유저가 모델을 가지고 있는지 검사합니다.
   
   >- Header에 값 추가
   >
   >```
   >{
   >    Authorization: Token add7188de45b08a2b53944e5f6b95a43053c2ebb
   >}
   >```
   >
   > - response
   >   - 모델이 존재하는 경우
   >
   >   ```
   >   {
   >       "code": 200
   >   }
   >   ```
   >
   >   
   >
   >   - 모델이 존재하지 않는 경우
   >
   >   ```
   >   {
   >       "code": 400
   >   }
   >   ```

6. server/authenticate/check_user/ **(POST)**

   생체인증 데이터를 받아서 해당 유저가 맞는지 검사합니다.
   >- Header에 값 추가
   >
   >```
   >{
   >    Authorization: Token add7188de45b08a2b53944e5f6b95a43053c2ebb
   >}
   >```

   > - request
   >
   > ```
   > {
   > 	"ECG": (FILE)
   > }
   > ```
   >
   > 
   >
   > 
   >
   > - response 
   >
   >   - 인증 성공
   >
   >   ```
   >   {
   >       "result": "true",
   >       "code": 200
   >   }
   >   ```
   >
   >   
   >
   >   - 인증 실패
   >
   >   ```
   >   {
   >       "result": "false",
   >       "code": 400
   >   }
   >   ```
   >
   >   
