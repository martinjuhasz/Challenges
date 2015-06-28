#!/usr/bin/python
# -*- coding: utf-8 -*-

from flask import Flask, Response, stream_with_context
from flask import request, jsonify
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy import exc
from sqlalchemy.exc import OperationalError
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from challenges.models.db.user import User
from challenges.models.db.challenge_task import ChallengeTask
from challenges.models.db.challenge_type import ChallengeType
from controller.game_controller import GameController
from models.db.media import Media

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

    user_games = game_controller.get_games(current_user)
    json = jsonify({'data': [game.to_dict() for game in user_games]})
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
    print user
    return jsonify({'id': user.id, 'username': user.username, 'image': None})


@app.route("/binary/<int:oid>/meta")
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
    """store a binary in the database returning its meta-data as json"""
    uploaded_file = request.files['file']
    # if not uploaded_file or not util.allowed_file(uploaded_file.filename):
    #     return jsonify({'error': 'no file or filename not allowed'}), 415
    filename = secure_filename(uploaded_file.filename)
    mimetype = uploaded_file.mimetype
    con = db.engine.raw_connection()
    lob = con.lobject(0, "wb")
    oid = lob.oid

    binsize = 0
    for chunk in read_in_chunks(uploaded_file):
        binsize += len(chunk)
        lob.write(chunk)

    uploaded_file.close()
    lob.close()
    con.commit()
    con.close()

    media = Media(oid=oid, filename=filename, mimetype=mimetype, binsize=binsize)
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


def read_in_chunks(stream, chunk_size=8129):
    while True:
        chunk = stream.read(chunk_size)
        if not chunk:
            break
        yield chunk
