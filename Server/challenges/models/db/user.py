from sqlalchemy import Column, Integer, String, LargeBinary
from sqlalchemy.orm import relationship
import uuid

from challenges.app import db
from challenges.models.db.game import game_user_link


class User(db.Model):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    username = Column(String(255), unique=True)
    token = Column(String(255), unique=True)
    image = Column(LargeBinary)
    games = relationship('Game', secondary=game_user_link, backref='User')

    def __init__(self, username):
        self.token = self.generate_token()
        self.username = username

    def __repr__(self):
        return 'User(username=%s, games=%d)' % (self.username, len(self.games))

    def generate_token(self):
        return uuid.uuid4().hex

    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'image': self.image
        }
