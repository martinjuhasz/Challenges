#!/usr/bin/python
# -*- coding: utf-8 -*-

from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
import flask.ext.restless


app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from challenges.models.db.game import Game
from challenges.models.db.user import User
from challenges.models.db.challenge import Challenge
from challenges.models.db.challenge_task import ChallengeTask
from challenges.models.db.challenge_type import ChallengeType

manager = flask.ext.restless.APIManager(app, flask_sqlalchemy_db=db)
manager.create_api(Game, methods=['GET', 'POST', 'DELETE'])

@app.route('/games')
def games():
    games = Game.query.all()
    print games
    return str(games)


@app.route('/fill_db')
def fill():
    db.drop_all()
    db.create_all()

    # setup some challenges
    challengeTypeImage = ChallengeType(title="Bilder-Challenge")
    challengeTask1 = ChallengeTask(hint_text="Bereite dich darauf vor, ein Bild zu machen!",
                                   task_text="Mache ein Bild von einer Kuh!")
    challengeTask2 = ChallengeTask(hint_text="Bereite dich darauf vor, ein Bild zu machen!",
                                   task_text="Zeige deine dümmste Grimasse")
    challengeTypeImage.tasks.append(challengeTask1)
    challengeTypeImage.tasks.append(challengeTask2)
    db.session.add(challengeTypeImage)

    # add some users
    user1 = User(username="Michael")
    user2 = User(username="Martin")
    user3 = User(username="Philipp")


    # add some games
    game1 = Game(title="Game 1", game_rounds=10)
    game1.users.append(user1)
    game1.users.append(user2)

    challenge1 = Challenge()
    challengeTask2.challenges.append(challenge1)
    game1.challenges.append(challenge1)
    db.session.add(challenge1)


    game2 = Game(title="Game 2", game_rounds=2)
    game2.users.append(user1)
    game2.users.append(user2)
    game2.users.append(user3)

    challenge2 = Challenge()
    challengeTask1.challenges.append(challenge2)
    game2.challenges.append(challenge2)
    db.session.add(challenge2)

    db.session.add_all([game1, game2])
    db.session.commit()

    return "db filled"