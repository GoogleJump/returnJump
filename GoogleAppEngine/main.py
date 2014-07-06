from flask import Flask, Response, request
import json, email

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

@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    return 'Hello World!'

@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404

@app.route('/api/email', methods=['POST'])
def email():
    if request.headers.get('API_KEY') != app.config['API_KEY']:
        return Response(json.dumps({'error': 'You are not authorized.'}), mimetype=None, content_type="application/json", direct_passthrough=False)

    return Response(json.dumps({'success': 'You are authorized'}), mimetype=None, content_type="application/json", direct_passthrough=False)