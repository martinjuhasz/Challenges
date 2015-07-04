#!/usr/bin/python
# -*- coding: utf-8 -*-

from flask import Flask, Response, stream_with_context
from flask import request, jsonify
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy import exc
from sqlalchemy.exc import OperationalError
from sqlalchemy.orm import query
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from challenges.models.db.game import Game
from challenges.models.db.user import User
from challenges.models.db.challenge import Challenge
from challenges.models.db.challenge_task import ChallengeTask
from challenges.models.db.challenge_type import ChallengeType
from challenges.models.db.media import Media
from controller.game_controller import GameController

game_controller = GameController(db)


def user_authenticated():
    token = request.headers.get('CHALLENGEUSERTOKEN')
    if token:
        user = User.query.filter_by(token=token).first()
        if user:
            return user
    return None


@app.route('/games', methods=['GET'])
def get_games():
    current_user = user_authenticated()
    if not current_user:
        return "", 403

    user_games = game_controller.get_games(current_user)
    json = jsonify({'data': [game.to_dict() for game in user_games]})
    return json

@app.route('/games', methods=['POST'])
def games():
    current_user = user_authenticated()
    if not current_user:
        return "", 403

    request_json = request.get_json(force=True, silent=True)
    if not request_json or not request_json['title']:
        return "", 400
    if not request_json or not request_json['users']:
        return "", 400

    title = request_json['title']
    users_ids = request_json['users']

    if len(users_ids) <= 0:
        return "", 400

    game = game_controller.create_game(title)
    for user_id in users_ids:
        user = User.query.filter_by(id=user_id).first()
        game.users.append(user)

    db.session.add(game)
    db.session.commit()

    json = jsonify(game.to_dict())
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
        return jsonify({'id': user.id, 'token': user.token})

        # return jsonify({'error_code': "USER_TAKEN"})

    return jsonify({'id': user.id, 'token': user.token})


@app.route('/users/find', methods=['POST'])
def find_user():
    request_json = request.get_json(force=True, silent=True)
    if not request_json or not request_json['username']:
        return "", 400


    user = User.query.filter_by(username=request_json['username']).first()

    if not user:
        return "", 404

    return jsonify({'id': user.id, 'username': user.username, 'image': None})

@app.route('/challenge/<int:challenge_id>/submissions', methods=["GET"])
def challenge_submissions(challenge_id):
    submissions = Media.query.filter_by(challenge_id = challenge_id).all()
    return jsonify({'data:': [submission.to_dict() for submission in submissions]})

@app.route('/challenge/<int:challenge_id>/submission', methods=["POST"])
def link_submission(challenge_id):
    current_user = user_authenticated()
    if not current_user:
        return "", 403

    request_json = request.get_json(force=True, silent=True)
    if not request_json or not request_json['oid'] or not request_json['filename'] or not request_json['mimetype']:
        return "", 400

    oid = request_json['oid']
    filename = request_json['filename']
    mimetype = request_json['mimetype']

    challenge = Challenge.query.filter_by(id = challenge_id).first()

    if not challenge:
        return "", 403

    media = Media.query.filter_by(oid=oid).first()
    media.challenge_id = challenge_id
    media.filename = filename
    media.mimetype = mimetype

    db.session.commit()

    return jsonify()


@app.route("/binary/<int:oid>/meta", methods=["GET"])
def binary_meta(oid):
    """retrieve the meta information of a binary as json"""
    media = Media.query.filter_by(oid=oid).first()
    if not media:
        return "", 404
    return jsonify(media.to_dict())


@app.route("/binary/<int:oid>", methods=["GET"])
def binary(oid):
    """retrieve a binary"""
    media = Media.query.filter_by(oid=oid).first()
    if not media:
        return "", 404

    con = db.engine.raw_connection()

    try:
        lob = con.lobject(oid, "rb")

        def stream_lob():
            for chunk in read_in_chunks(lob):
                yield chunk
            lob.close()
            con.close()

        return Response(stream_with_context(stream_lob()), mimetype=media.mimetype,
                        headers={'Content-Length': media.binsize})
    except OperationalError:
        con.close()
        raise Exception("binary data and meta data out of sync")


@app.route('/binary', methods=['POST'])
def binary_post():
    """store a binary in the database returning its oid as json"""

    current_user = user_authenticated()
    if not current_user:
        return "", 403

    content_length = request.content_length
    con = db.engine.raw_connection()
    lob = con.lobject(0, "wb")
    oid = lob.oid

    binsize = 0
    for chunk in read_in_chunks(request.stream):
        binsize += len(chunk)
        lob.write(chunk)

    lob.close()
    con.commit()
    con.close()

    if(content_length == binsize):
        media = Media(oid=oid, binsize=binsize, user_id=current_user.id)
        db.session.add(media)
        db.session.commit()

    return jsonify({'oid': oid})


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

    game3 = game_controller.create_game("Game 3")
    game3.users.append(user2)
    game3.users.append(user3)
    game3.current_challenge.status = Challenge.STATUS_RATING

    db.session.add_all([game1, game2, game3])
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


def read_in_chunks(stream, chunk_size=8129):
    while True:
        chunk = stream.read(chunk_size)
        if not chunk:
            break
        yield chunk
