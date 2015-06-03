from challenges.app import db
from sqlalchemy import Column, Integer, String, ForeignKey

class GameUserLink(db.Model):
    __tablename__ = 'game_user_link'

    id = Column(Integer, primary_key=True)
    game_id = Column(Integer, ForeignKey('games.id'), primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'), primary_key=True)