from flask import Flask, Response, request, render_template, url_for
from google.appengine.api import mail
import json

app = Flask(__name__)
app.config.update(dict(
    DEBUG=True,
    JSONIFY_PRETTYPRINT_REGULAR=True
))

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
    return render_template('index.html')

@app.route('/material/')
def material():
    #return app.send_static_file('material.html')
    return render_template('material.html')

@app.route("/user/")
def  user_render():
    return render_template("users.html")

@app.errorhandler(404)
def page_not_found(e):
    '''Return a custom 404 error.'''
    return 'Are you lost, bro?', 404

def generateBody(expiredFridgeItems):
    plain = ''.join(map(lambda x: x + '\n', expiredFridgeItems))
    html = ''.join(map(lambda x: '<li>' + x + '</li>\n            ', expiredFridgeItems))

    return plain, html

def sendEmail(email, expiredFridgeItems):
    message = mail.EmailMessage(sender='Return Jump <returnjump@gmail.com>', subject='Frij')

    plain, html = generateBody(expiredFridgeItems)
    header = 'The following items in your fridge have expired:' if len(expiredFridgeItems) > 1 else 'The following item in your fridge has expired:'

    message.to = email
    message.bcc = 'returnjump@gmail.com'
    message.body = '''
Hey,

%s
%s
''' % plain
    message.html = '''
<html>
    <head></head>
    <body>
        Hey,
        <br>
        <br>
        %s
        <ul>
            %s
        </ul>
    </body>
</html>
''' % html

    message.send()
    return myResponse({'success': 'Email sent.', 'email': email, 'exp': expiredFridgeItems})

@app.route('/api/email', methods=['POST'])
def email():
    if request.headers.get('API_KEY') != app.config['API_KEY']:
        return myResponse({'error': 'You are not authorized.'})
  
    return sendEmail(request.json['email'], request.json['expiredFridgeItems'])
