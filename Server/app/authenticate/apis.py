from django.contrib.auth import authenticate
from django.http import JsonResponse
from rest_framework import status
from rest_framework.authtoken.models import Token
from rest_framework.parsers import MultiPartParser
from rest_framework.response import Response
from rest_framework.views import APIView
import pandas as pd
import numpy as np

from members.models import User
from .ECG import getFeature, preprocess
from .EEG_CNN import *
from tensorflow import keras

from authenticate.serializers import ECGSerializer


# 처음 가입했을 때 사용
class MakeEEGModelAPIView(APIView):
    parser_classes = (MultiPartParser,)  # 파일을 받기 위해 사용

    def post(self, request):
        serializer = ECGSerializer(data=request.data)
        if serializer.is_valid():
            token = request.data['token']
            user = Token.objects.get(key=token).user
            eeg = request.data['EEG']

            train_data, train_label = make_train_dataset(eeg)
            input_shape = train_data.shape[-2:]
            model = make_model(input_shape)
            model.fit(train_data, train_label, batch_size=20, epochs=10, verbose=0)

            save_file = f'../media/{user.username}.h5'
            model.save(save_file)
            user.model = save_file
            user.save()

            return Response({
                'detail': '저장완료.'
            })
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# 가입 후 인증할 때 사용
class CheckUserAPIView(APIView):
    parser_classes = (MultiPartParser,)

    def post(self, request):
        serializer = ECGSerializer(data=request.data)
        if serializer.is_valid():
            token = Token.objects.get(key=request.auth)
            user = Token.objects.get(key=token).user

            # 저장된 model load
            saved_model = user.model

            model = keras.models.load_model(str(saved_model))

            ecg = request.data['ECG']  # 받아온 eeg

            # Feature 추출 및 전처리
            ecg = pd.read_csv(ecg)
            ecg = getFeature(ecg)
            ecg = preprocess(ecg)

            predict = model.predict(ecg)

            if predict >= 0.5:
                return JsonResponse({"result": "true", "code": 200}, status=status.HTTP_200_OK)
            else:
                return Response({"result": "false", "code": 400}, status=status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# 토큰 받아서 모델 있는지 확인
class GetModelAPIView(APIView):
    def get(self, request):
        user = Token.objects.get(key=request.auth).user

        if user.model:  # 모델이 있으면
            return Response(data={"code": 200}, status=status.HTTP_200_OK)
        else:
            return Response(data={"code": 400}, status=status.HTTP_200_OK)
