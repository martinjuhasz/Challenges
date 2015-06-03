from challenges.app import db
from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship


class User(db.Model):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    username = Column(String(255), unique=True)
    games = relationship('Game', secondary="game_user_link", backref='User')

    def __init__(self, username=None):
        self.username = username