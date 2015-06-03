from challenges.app import db
from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship

game_user_link = db.Table('game_user_link',
    db.Column('game_id', db.Integer, db.ForeignKey('games.id')),
    db.Column('user_id', db.Integer, db.ForeignKey('users.id'))
)

class Game(db.Model):
    __tablename__ = 'games'

    id = Column(Integer, primary_key=True)
    title = Column(String(255), unique=True)
    game_rounds = Column(Integer)
    users = relationship('User', secondary=game_user_link, backref='Game')
        
    def __repr__(self):
        return 'Game(title=%s, game_rounds=%d users=%s)' % (self.title, self.game_rounds, str(len(self.users)))