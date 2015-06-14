from challenges.app import db
from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.orm import relationship, backref
from flask import jsonify
from challenges.models.db.challenge import Challenge

game_user_link = db.Table('game_user_link',
                          db.Column('game_id', db.Integer, db.ForeignKey('games.id')),
                          db.Column('user_id', db.Integer, db.ForeignKey('users.id'))
                          )


class Game(db.Model):
    __tablename__ = 'games'

    id = Column(Integer, primary_key=True)
    title = Column(String(255), unique=True)
    game_rounds = Column(Integer)
    users = relationship('User', secondary=game_user_link)
    challenges = relationship("Challenge", foreign_keys='Challenge.game_id', primaryjoin=id==Challenge.game_id)
    current_challenge_id = Column(Integer, ForeignKey('challenges.id', name="fk_current_challenge_id_game", use_alter=True))
    current_challenge = relationship("Challenge", foreign_keys='Game.current_challenge_id', primaryjoin=current_challenge_id==Challenge.id, post_update=True)


    def __repr__(self):
        return 'Game(title=%s, game_rounds=%d users=%s)' % (self.title, self.game_rounds, str(len(self.users)))

    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'game_rounds': self.game_rounds,
            'users': [user.to_dict() for user in self.users],
            'current_challenge': self.current_challenge.to_dict(),
            #'challenges': [challenge.to_dict() for challenge in self.challenges]
        }
