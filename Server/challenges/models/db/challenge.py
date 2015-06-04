from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship

from challenges.app import db
from challenges.models.db.game import game_user_link


class Challenge(db.Model):

    __tablename__ = 'challenges'

    id = Column(Integer, primary_key=True)
    challenge_task_id = db.Column(db.Integer, db.ForeignKey('challenge_tasks.id'))
    game_id = db.Column(db.Integer, db.ForeignKey('games.id'))