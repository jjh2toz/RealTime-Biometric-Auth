from django.contrib.auth import authenticate
from rest_framework import status
from rest_framework.authentication import TokenAuthentication
from rest_framework.authtoken.models import Token
from rest_framework.exceptions import AuthenticationFailed
from rest_framework.response import Response
from rest_framework.views import APIView

from members.serializers import UserSerializer


# 회원가입
class CreateUserAPIView(APIView):
    def post(self, request):
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            username = request.data['username']
            password = request.data['password']

            user = authenticate(username=username, password=password)
            token = Token.objects.create(user=user)

            data = {
                'user': serializer.data,
                'token': token.key,
                'code': 200,
            }

            return Response(data)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# 로그인 (토큰 발급)
class AuthTokenAPIView(APIView):
    def post(self, request):
        username = request.data['username']
        password = request.data['password']

        user = authenticate(username=username, password=password)

        if user:
            token, _ = Token.objects.get_or_create(user=user)
        else:
            raise AuthenticationFailed()

        serializer = UserSerializer(user)
        data = {
            'token': token.key,
            'user': serializer.data,
            'code': "login_success",
        }
        return Response(data)


# 로그아웃 (토큰 삭제)
class LogoutAPIView(APIView):
    def get(self, request):
        user_token = Token.objects.filter(key=request.auth)
        if user_token:
            user_token.delete()
            return Response(data={"detail": "로그아웃 되었습니다."}, status=status.HTTP_200_OK)
        else:
            return Response(data={"detail": "토큰이 유효하지 않습니다."}, status=status.HTTP_200_OK)


# 토큰
class GettokenAPIView(APIView):  # 토큰 전송
    def post(self, request):

        username = request.data['username']
        password = request.data['password']

        user = authenticate(username=username, password=password)
        token = Token.objects.get(user=user)

        if request.method == 'POST':
            return Response({'code': token.key, 'msg': "토큰값"}, status=200)
        else:
            return Response(data={"detail": "오류"}, status=status.HTTP_400_BAD_REQUEST)
