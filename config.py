import os
basedir = os.path.abspath(os.path.dirname(__file__))

class Config(object):

    SQLALCHEMY_DATABASE_URI = 'sqlite:////' #insert the path that you want the sql server to be in
    SQLALCHEMY_TRACK_MODIFICATIONS = False