from django.urls import path

from authenticate.apis import CheckUserAPIView, MakeEEGModelAPIView, GetModelAPIView

urlpatterns = [
    path('make_model/', MakeEEGModelAPIView.as_view()),
    path('check_user/', CheckUserAPIView.as_view()),
    path('get_model/', GetModelAPIView.as_view()),
]