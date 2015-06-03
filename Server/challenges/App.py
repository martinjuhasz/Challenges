from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
import flask.ext.restless


app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from models.game import Game
from models.user import User

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

    user1 = User(username="Michael")
    user2 = User(username="Martin")
    user3 = User(username="Philipp")

    game1 = Game(title="Game 1", game_rounds=10)
    game1.users.append(user1)
    game1.users.append(user2)

    game2 = Game(title="Game 2", game_rounds=2)
    game2.users.append(user1)
    game2.users.append(user2)
    game2.users.append(user3)

    db.session.add_all([game1, game2])
    db.session.commit()

    return "db filled"