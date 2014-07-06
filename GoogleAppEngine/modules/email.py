import smtplib
import json

# As tempting as it may be, DO NOT tab this function
def createMessage(fromName, fromEmail, toName, toEmail, subject, body):
    return """From: %s <%s>
To: %s <%s>
MIME-Version: 1.0
Content-type: text/html
Subject: %s

%s
""" % (fromName, fromEmail, toName, toEmail, subject, body)

def sendEmail(fromName='Return Jump', fromEmail='returnjump@gmail.com', toName='', toEmail, subject, body, password):
    sender = fromEmail
    receivers = [toEmail]

    message = createMessage(fromName, fromEmail, toName, toEmail, subject, body)

    try:
        smtpObj = smtplib.SMTP('smtp.gmail.com', 587)
        smtpObj.starttls()
        smtpObj.login(fromEmail, password)
        smtpObj.sendmail(sender, receivers, message)         
        
        return "Successfully sent email"
    except SMTPException:
        return "Error: unable to send email"
