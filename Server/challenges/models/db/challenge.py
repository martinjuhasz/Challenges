from sqlalchemy import Column, Integer, String, ForeignKeyConstraint, ForeignKey
from sqlalchemy.orm import relationship

from challenges.app import db


class Challenge(db.Model):

    __tablename__ = 'challenges'

    id = Column(Integer, primary_key=True)
    challenge_task_id = Column(db.Integer, db.ForeignKey('challenge_tasks.id'))
    game_id = Column(Integer, ForeignKey('games.id', name="fk_game_id_challenge", use_alter=True))


    def to_dict(self):
        return {
            'id': self.id,
            'type': self.task.type.title,
            'text_hint': self.task.hint_text,
            'text_task': self.task.task_text
        }

