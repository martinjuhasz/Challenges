from sqlalchemy import Column, Integer, String, Text
from sqlalchemy.orm import relationship

from challenges.app import db
from challenges.models.db.game import game_user_link


class ChallengeTask(db.Model):

    __tablename__ = 'challenge_tasks'

    id = Column(Integer, primary_key=True)
    hint_text = Column(Text())
    task_text = Column(Text())
    challenges = db.relationship('Challenge', backref='challenge_tasks', lazy='dynamic')
    challenge_type_id = db.Column(db.Integer, db.ForeignKey('challenge_types.id'))