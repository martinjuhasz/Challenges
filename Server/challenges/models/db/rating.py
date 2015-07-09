from challenges.app import db


class Rating(db.Model):

    user_id = db.Column(db.INTEGER, db.ForeignKey('users.id'), primary_key=True)
    challenge_id = db.Column(db.INTEGER, db.ForeignKey('challenges.id'), primary_key=True)
    oid = db.Column(db.INTEGER, db.ForeignKey('media.oid'), primary_key=True)
    rating = db.Column(db.INTEGER, nullable=False)

    def to_dict(self):
        return {
            'user_id': self.user_id,
            'challenge_id': self.challenge_id,
            'oid': self.media_id,
            'rating': self.rating
        }
