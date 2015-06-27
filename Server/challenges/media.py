# -*- coding: utf-8 -*-
"""simple media management"""
import imghdr

from app import db

def guess_mimetype(blob):
    "guesses the mimetype"
    mimetype = imghdr.what("ignore filename", blob)
    if not mimetype:
        return "application/octet-stream"
    return "image/%s" % mimetype

def add_media(blob, filename, mimetype=None):
    "adds media based on blob"
    oid = db.write_blob(blob)
    if mimetype == None:
        mimetype = guess_mimetype(blob)
    binsize = len(blob)
    _add_media_meta(oid, filename, mimetype, binsize)
    return oid

_INSERT_META = """INSERT INTO media(oid, filename, mimetype, binsize)
  VALUES (%s, %s, %s, %s)"""
def _add_media_meta(oid, filename, mimetype, binsize):
    "adds only the meta information"
    if filename == None:
        filename = str(oid)
    db.transact_one(_INSERT_META, (oid, str(filename), mimetype, binsize))

_SELECT_META = "SELECT filename, mimetype, binsize FROM media WHERE oid=%s"
_SELECT_META_ALL = "SELECT oid, filename, mimetype, binsize FROM media"
def get_media_meta(oid=None):
    "access meta information of one or all stored binary media"
    if oid:
        return db.query_one_row(_SELECT_META, (oid,))
    else:
        return db.query(_SELECT_META_ALL)

def get_blob(oid):
    "returns the blob given its id"
    return db.read_blob(oid)

_DELETE_META = "DELETE FROM media WHERE oid=%s"
def del_media(oid):
    "delete one binary media and its meta data"
    db.transact_one(_DELETE_META, (oid, ))
    db.del_blob(oid)