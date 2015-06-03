from challenges.app import db
from sqlalchemy import Column, Integer, String

class Game(db.Model):
    __tablename__ = 'games'
    id = Column(Integer, primary_key=True)
    title = Column(String(255), unique=True)
    game_rounds = Column(Integer)

    def __init__(self, title=None, game_rounds=10):
        self.title = title
        self.game_rounds = game_rounds
        
    def __repr__(self):
        return 'Game(title  =%s, game_rounds=%d)' % (self.title, self.game_rounds)