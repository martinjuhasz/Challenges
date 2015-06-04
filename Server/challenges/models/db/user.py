from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship

from challenges.app import db
from challenges.models.db.game import game_user_link


class User(db.Model):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    username = Column(String(255), unique=True)
    games = relationship('Game', secondary=game_user_link, backref='User')

    def __repr__(self):
        return 'User(username=%s, games=%d)' % (self.username, len(self.games))