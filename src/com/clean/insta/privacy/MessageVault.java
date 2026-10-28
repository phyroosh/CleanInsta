package com.clean.insta.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageVault: Local SQLite store that preserves Direct Messages against un-sending.
 */
public class MessageVault extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "clean_message_vault.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "vault_messages";
    public static final String COL_ID = "_id";
    public static final String COL_MSG_ID = "server_message_id";
    public static final String COL_THREAD_ID = "thread_id";
    public static final String COL_SENDER_ID = "sender_id";
    public static final String COL_TEXT = "message_text";
    public static final String COL_RAW_JSON = "raw_json";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_IS_DELETED = "is_deleted";

    private static volatile MessageVault instance;

    public static MessageVault get(Context context) {
        if (instance == null) {
            synchronized (MessageVault.class) {
                if (instance == null) {
                    instance = new MessageVault(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private MessageVault(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MSG_ID + " TEXT UNIQUE, "
                + COL_THREAD_ID + " TEXT, "
                + COL_SENDER_ID + " TEXT, "
                + COL_TEXT + " TEXT, "
                + COL_RAW_JSON + " TEXT, "
                + COL_TIMESTAMP + " INTEGER, "
                + COL_IS_DELETED + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_thread ON " + TABLE_MESSAGES + " (" + COL_THREAD_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration logic when version upgrades
    }

    public synchronized void recordMessage(String msgId, String threadId, String senderId, String text, String rawJson, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_ID, msgId);
        cv.put(COL_THREAD_ID, threadId);
        cv.put(COL_SENDER_ID, senderId);
        cv.put(COL_TEXT, text);
        cv.put(COL_RAW_JSON, rawJson);
        cv.put(COL_TIMESTAMP, timestamp);
        cv.put(COL_IS_DELETED, 0);

        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public synchronized void markDeleted(String msgId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_DELETED, 1);
        db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{msgId});
    }

    public static class SavedMessage {
        public long id;
        public String msgId;
        public String threadId;
        public String senderId;
        public String text;
        public long timestamp;
        public boolean isDeleted;
    }

    public synchronized List<SavedMessage> getDeletedMessages(String threadId) {
        List<SavedMessage> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MESSAGES, null,
                COL_THREAD_ID + " = ? AND " + COL_IS_DELETED + " = 1",
                new String[]{threadId}, null, null, COL_TIMESTAMP + " DESC");

        if (c != null) {
            try {
                int idIdx = c.getColumnIndex(COL_ID);
                int msgIdIdx = c.getColumnIndex(COL_MSG_ID);
                int threadIdx = c.getColumnIndex(COL_THREAD_ID);
                int senderIdx = c.getColumnIndex(COL_SENDER_ID);
                int textIdx = c.getColumnIndex(COL_TEXT);
                int timeIdx = c.getColumnIndex(COL_TIMESTAMP);
                int delIdx = c.getColumnIndex(COL_IS_DELETED);

                while (c.moveToNext()) {
                    SavedMessage m = new SavedMessage();
                    m.id = c.getLong(idIdx);
                    m.msgId = c.getString(msgIdIdx);
                    m.threadId = c.getString(threadIdx);
                    m.senderId = c.getString(senderIdx);
                    m.text = c.getString(textIdx);
                    m.timestamp = c.getLong(timeIdx);
                    m.isDeleted = c.getInt(delIdx) == 1;
                    list.add(m);
                }
            } finally {
                c.close();
            }
        }
        return list;
    }
}