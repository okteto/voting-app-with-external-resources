import os
from pymongo import MongoClient

def lambda_handler(event, context):
    username = os.getenv('MONGODB_USERNAME')
    password = os.getenv('MONGODB_PASSWORD')
    database = os.getenv('MONGODB_DATABASE')
    host = os.getenv('MONGODB_HOST', 'mongodb-serverless.tussl.mongodb.net')
    client = MongoClient('mongodb+srv://' + username + ':' + password + '@'+ host + '/' + database + '?retryWrites=true&w=majority')
    db = client[database]
    coll = db['votes']
    coll.insert_one(event)

    return {
        "statusCode": 200,
        "body": "Acepted!"
    }