from flask import Flask, request, session
from twilio.rest import Client
from twilio.twiml.messaging_response import Body, Message, Redirect, MessagingResponse
from twilio.twiml.voice_response import VoiceResponse, Dial, Number, Play, Say
from twilio.rest import Client
import sys
import os.path
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from config import Config

# Your Account SID from twilio.com/console
account_sid = ""
# Your Auth Token from twilio.com/console
auth_token  = ""
client = Client(account_sid, auth_token)

app = Flask(__name__)

app.config.from_object(Config)

db = SQLAlchemy(app)

class Contact(db.Model):
	id = db.Column(db.Integer, primary_key=True)
	number = db.Column(db.String(50), unique = True, nullable=False)
	contacts = db.Column(db.String(48),nullable = False)
	def __str__(self):
		return self.number
	def __repr__(self):
		return '<Number: '+ self.number + '>' 

def create_contact(phone_number,contact_list):
	contact = Contact(number= phone_number, contacts= contact_list)
	db.session.add(contact)
	db.session.commit()
	db.session.close()
	db.session.remove()

def modify_contact(phone_number,contact_list):
	db.session.query(Contact).filter(Contact.number==phone_number).first().contacts = contact_list
	print(db.session.query(Contact).filter(Contact.number==phone_number).first().contacts, file=sys.stderr)
	db.session.commit()
	db.session.remove()

@app.route('/voicecall', methods=['POST'])
def call_reply():
	phone_numbers_list = db.session.query(Contact).filter(Contact.number==request.form["From"]).first().contacts.split(',')
	db.session.remove()	
	print(phone_numbers_list, file=sys.stderr)
	response = VoiceResponse()
	dial = Dial()
	for number in phone_numbers_list:
		dial.number(number)
	response.append(dial)
	return str(response)

@app.route('/sms/reply', methods=['POST'])
def sms_reply():
	response = MessagingResponse()
	message = Message()
	message.body("Message Recieved. Help will be on the way")
	response.append(message)
	sms_message = request.form['Body']
	send_message,send_list = sms_message.split(":")
	sender = request.form['From']
	send_list = send_list.split(',')
	if isinstance(send_list,str):
		send_list = [send_list]
	for item in send_list:
		if item == '':
			send_list.remove('')
	if send_list != []:
		if Contact.query.filter_by(number=sender).first():
			modify_contact(sender,','.join(send_list))
		else:
			create_contact(sender,','.join(send_list))
	else:
		send_list = db.session.query(Contact).filter(Contact.number==request.form["From"]).first().contacts.split(',')
	try:
		State = request.form['FromState']
	except:
		State = False
	try:
		City = request.form['FromCity']
	except:
		City = False
	try:
		Country = request.form['FromCountry']
	except:
		Country = False
	if State and City and Country:
		full_message = send_message + ' from ' + City + ' ' + State + ', ' + Country + ' ' + sender
	else:
		full_message = send_message + ' ' + sender
	for contact_number in send_list:

		export_message = client.messages.create(
			to = '+1'+contact_number,
			from_ = "+16467620371",
			body=full_message)
	return str(response)
# for this you need to generate the secret key manually so just use a random numer generator for this
app.secret_key = "" 

if __name__ == '__main__':
	app.run(host = '0.0.0.0',debug=True)