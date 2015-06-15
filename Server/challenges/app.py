#!/usr/bin/python
# -*- coding: utf-8 -*-

from flask import Flask
from flask import request, jsonify
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy import exc

app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from challenges.models.db.game import Game
from challenges.models.db.user import User
from challenges.models.db.challenge import Challenge
from challenges.models.db.challenge_task import ChallengeTask
from challenges.models.db.challenge_type import ChallengeType
from controller.game_controller import GameController

game_controller = GameController(db)

def user_authenticated():
    token = request.headers.get('challenge_user_token')
    if token:
        user = User.query.filter_by(token=token).first()
        if user:
            return user
    return None

@app.route('/games')
def games():
    current_user = user_authenticated()
    if not current_user:
        return "", 403

    games = game_controller.get_games(current_user)
    json = jsonify({'data': [game.to_dict() for game in games]})
    return json


@app.route('/users', methods=['POST'])
def create_user():
    request_json = request.get_json(force=True, silent=True)
    if not request_json or not request_json['username']:
        return "", 400

    try:
        user = User(username=request_json['username'])
        db.session.add(user)
        db.session.commit()
    except exc.IntegrityError:
        # TODO: REMOVE!! only temp login
        db.session.rollback()
        user = User.query.filter_by(username=request_json['username']).first()
        return jsonify({'token': user.token})

        return jsonify({'error_code': "USER_TAKEN"})

    return jsonify({'token': user.token})


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
    game1 = game_controller.create_game("Game 1")
    game1.users.append(user1)
    game1.users.append(user2)


    game2 = game_controller.create_game("Game 2")
    game2.users.append(user1)
    game2.users.append(user2)
    game2.users.append(user3)

    db.session.add_all([game1, game2])
    db.session.commit()

    return "db filled"


@app.route('/add_game')
def add_game():

    user1 = User.query.filter_by(username="Martin").first()
    user2 = User.query.filter_by(username="Michael").first()

    # add some games
    game1 = game_controller.create_game("Game 6")
    game1.users.append(user1)
    game1.users.append(user2)

    db.session.add_all([game1])
    db.session.commit()

    return "db filled"