from flask import Flask, Response, request
from google.appengine.api import mail
import json, phrije_email

app = Flask(__name__)
app.config.update(dict(
    DEBUG=True,
    JSONIFY_PRETTYPRINT_REGULAR=True
))

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

@app.before_first_request
def configure():
    jsonData = open('modules/secret.json')
    data = json.load(jsonData)
    jsonData.close()

    app.config.update(dict(
        EMAIL_PASSWORD=data['email_password'],
        API_KEY=data['API_KEY']
    ))

def myResponse(body):
    return Response(json.dumps(body), mimetype='application/json')

@app.route('/')
def hello():
    '''Return a friendly HTTP greeting.'''
    return 'Hello World!'

@app.errorhandler(404)
def page_not_found(e):
    '''Return a custom 404 error.'''
    return 'Are you lost, bro?', 404

@app.route('/api/email', methods=['POST'])
def email():
    if request.headers.get('API_KEY') != app.config['API_KEY']:
        return myResponse({'error': 'You are not authorized.'})

    #sendEmail = phrije_email.sendEmail('returnjump@gmail.com', 'Phrije', 'Your food has gone bad.', app.config['EMAIL_PASSWORD'])
    #return myResponse(sendEmail)
    message = mail.EmailMessage(sender='Return Jump <returnjump@gmail.com>', subject='Phrije')

    message.to = 'returnjump@gmail.com'
    message.body = '''
Your food has gone bad.
'''
    message.html = '''
<html><head></head><body>
<b>Your food has gone bad.</b>
</body></html>
'''

    message.send()
    return myResponse({'success': 'Email sent.'})    
