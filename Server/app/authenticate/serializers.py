from rest_framework import serializers
from rest_framework.authtoken.models import Token

from members.models import User


class ECGSerializer(serializers.Serializer):
    # 유저판별
    ECG = serializers.FileField()
