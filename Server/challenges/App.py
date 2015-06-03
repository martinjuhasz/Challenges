from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
import flask.ext.restless


app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)
from models.game import Game

manager = flask.ext.restless.APIManager(app, flask_sqlalchemy_db=db)
manager.create_api(Game, methods=['GET', 'POST', 'DELETE'])


@app.route('/games')
def games():

    games = Game.query.all()

    # create new game
    gameTitle = "Game " + str(len(games))
    game = Game(gameTitle)
    db.session.add(game)
    db.session.commit()

    games = Game.query.all()
    print games
    return str(games)