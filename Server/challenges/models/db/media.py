from sqlalchemy import Column, TEXT, INTEGER
from sqlalchemy.dialects.postgresql import OID
from challenges.app import db

class Media(db.Model):

    __tablename__ = 'media'

    oid = Column(OID, primary_key=True)
    filename = Column(TEXT, nullable=False)
    mimetype = Column(TEXT)
    binsize = Column(INTEGER, nullable=False)

    def to_dict(self):
        return {
            'oid': self.oid,
            'filename': self.filename,
            'mimetype': self.mimetype,
            'binsize': self.binsize
        }