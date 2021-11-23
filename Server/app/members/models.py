from django.contrib.auth.models import AbstractUser
from django.db import models


# Create your models here.

# username, password 기본
class User(AbstractUser):
    name = models.CharField(max_length=20)
    GENDER_CHOICES = (
        ('male', 'male'),
        ('female', 'female'),
    )
    # 학습된 모델 저장
    model = models.FileField(upload_to='model/', null=True)
