from challenges.models.db.challenge import Challenge
from challenges.models.db.challenge_type import ChallengeType
from challenges.models.db.challenge_task import ChallengeTask
from challenges.models.db.game import Game
from challenges.models.db.user import User
import random
__author__ = 'martinjuhasz'

class GameController:

    def __init__(self, database):
        self.database = database

    def get_games(self, user):
        return Game.query.join(Game.users).filter(User.id == user.id).all()

    def create_game(self, title):
        game = Game(title=title, game_rounds=10)
        challenge = self.generate_challenge()
        game.challenges.append(challenge)
        game.current_challenge = challenge
        return game

    def generate_challenge(self):

        # get a random type
        challenge_type_count = int(ChallengeType.query.count())
        type = ChallengeType.query.offset(int(challenge_type_count*random.random())).first()

        # get a random task
        task_query = ChallengeTask.query.filter(ChallengeTask.challenge_type_id == type.id)
        task_count = int(task_query.count())
        task = task_query.offset(int(task_count*random.random())).first()

        challenge = Challenge(challenge_task_id=task.id, status=Challenge.STATUS_PLAYING)
        return challenge



