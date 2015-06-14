from sqlalchemy import Column, Integer, String, Text
from sqlalchemy.orm import relationship

from challenges.app import db
from challenges.models.db.game import game_user_link


class ChallengeType(db.Model):

    __tablename__ = 'challenge_types'

    id = Column(Integer, primary_key=True)
    title = Column(String(255), unique=True)
    tasks = db.relationship('ChallengeTask', backref='type', lazy='dynamic')