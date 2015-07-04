from sqlalchemy import Column, TEXT, INTEGER
from sqlalchemy.dialects.postgresql import OID
from challenges.app import db

class Media(db.Model):

    __tablename__ = 'media'

    oid = Column(OID, primary_key=True)
    filename = Column(TEXT)
    mimetype = Column(TEXT)
    binsize = Column(INTEGER, nullable=False)
    user_id = db.Column(db.INTEGER, db.ForeignKey('users.id'))
    challenge_id = db.Column(db.INTEGER, db.ForeignKey('challenges.id'))

    def to_dict(self):
        return {
            "user_id": self.user_id,
            'oid': self.oid,
            'filename': self.filename,
            'mimetype': self.mimetype,
            'binsize': self.binsize
        }