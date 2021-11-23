daemon = False
chdir = '/srv/realtime_authentication/app'
bind = 'unix:/run/authenticate.sock'
accesslog = '/var/log/gunicorn/authenticate-access.log'
errorlog = '/var/log/gunicorn/authenticate-error.log'
capture_output = True