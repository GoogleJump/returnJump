from flask import Flask, Response, request, render_template, url_for
from google.appengine.api import urlfetch
from google.appengine.api import mail
import json

app = Flask(__name__)
app.config.update(dict(
    DEBUG=True,
    JSONIFY_PRETTYPRINT_REGULAR=True
))

@app.before_first_request
def configure():
    jsonData = open('secret.json')
    data = json.load(jsonData)
    jsonData.close()

    app.config.update(dict(
        EMAIL_PASSWORD=data['email_password'],
        API_KEY=data['API_KEY'],
        PARSE_APPLICATION_ID=data['parse_application_id'],
        PARSE_REST_API_KEY=data['parse_rest_api_key']
    ))

def myResponse(body):
    return Response(json.dumps(body), mimetype='application/json')

@app.route('/')
@app.route('/fridge')
def main():
    return render_template('index.html')

@app.route('/old/')
def old():
    return render_template('old.html')

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
    # Authenticate by API Key
    if request.headers.get('API_KEY') != app.config['API_KEY']:
        return myResponse({'error': 'You are not authorized.'})
  
    return sendEmail(request.json['email'], request.json['expiredFridgeItems'])

def requestGet(url, query):
    return urlfetch.fetch(url=(url + query), headers={'X-Parse-Application-Id': app.config['PARSE_APPLICATION_ID'], 'X-Parse-REST-API-Key': app.config['PARSE_REST_API_KEY']})

def getUsersInstallationObjectid(email):
    url = 'https://api.parse.com/1/classes/Users'
    query = '?where={"email":"%s"}' % email
    result = requestGet(url, query)

    if result.status_code == 200:
        body = json.loads(result.content)['results']

        # We don't have their email in the database
        if len(body) == 0:
            return False, None, myResponse({'error': 'Go get an account!'})

        # Return the installationObjectId of the first user (there shouldn't be duplicates) that returns a match
        return True, body[0]['installationObjectId'], None
    else:
        return False, None, myResponse({'error': result.content})

def getUsersUnexpiredFridgeItems(installationObjectId):
    url = 'https://api.parse.com/1/classes/Fridge'
    query = '?where={"installationObjectId":"%s","dismissed":false}' % installationObjectId
    result = requestGet(url, query)

    if result.status_code == 200:
        body = json.loads(result.content)['results']

        # Strip out the data we need
        body = map(lambda x: {'name': x['foodItem'], 'expiryDate': x['expiryDate']}, body)

        return True, body, None
    else:
        return False, None, myResponse({'error': result.content})

@app.route('/api/fridge', methods=['POST'])
def fridge():
    # Need authentication here (https://developers.google.com/+/web/signin/server-side-flow)

    if not request.json.get('email'):
        return myResponse({'error': 'No email given.'})

    success, installationObjectId, error = getUsersInstallationObjectid(request.json['email'])
    if not success:
        return error

    success, fridgeItems, error = getUsersUnexpiredFridgeItems(installationObjectId)
    if not success:
        return error

    return myResponse({'data': fridgeItems})
