from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config.from_object('config.Config')
db = SQLAlchemy(app)

from models.game import Game

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