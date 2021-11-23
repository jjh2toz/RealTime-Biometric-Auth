import requests
from django.http import HttpResponse

from config.settings import AUTH_USER_MODEL

User = AUTH_USER_MODEL


def naver_login(request):
    code = request.GET.get('code')
    state = request.GET.get('state')
    if not code or not state:
        return HttpResponse('code또는 state가 전달되지 않았습니다.')

    token_base_url = 'http://nid.naver.com/oauth2.0/token'
    token_params = {
        'grant_type': 'authorization_code',
        'client_id': '',  # NAVER_ID,
        'client_secret': '',  # NAVER_SERECT,
        'code': code,
        'state': state,
        'redirectURI': 'http://localhost:8000/members/naver-login/',
    }

    token_url = '{base}?{params}'.format(
        base=token_base_url,
        params='&'.join([f'{key}={value}' for key, value in token_params.items()])
    )
    # request를 보내서 access_token 얻어옴
    response = requests.get(token_url)
    access_token = response.json()['access_token']

    me_url = 'https://openapi.naver.com/v1/nid/me'
    me_headers = {
        'Authorization': f'Bearer {access_token}',
    }
    me_response = requests.get(me_url, headers=me_headers)
    me_response_data = me_response.json()

    unique_id = me_response_data['response']['id']

    naver_username = f'n_{unique_id}'
    if not User.objects.filter(username=naver_username):
        user = User.objects.create_user(username=naver_username)
    else:
        user = User.objects.get(username=naver_username)
