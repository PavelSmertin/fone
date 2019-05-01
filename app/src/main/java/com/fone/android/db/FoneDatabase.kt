package com.fone.android.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fone.android.Constants.DataBase.DB_NAME
import com.fone.android.vo.*


@Database(entities = [
    (User::class),
    (Conversation::class),
    (Message::class),
    (Participant::class),
    (Offset::class),
    (Asset::class),
    (Snapshot::class),
    (MessageHistory::class),
    (SentSenderKey::class),
    (Sticker::class),
    (StickerAlbum::class),
    (App::class),
    (Hyperlink::class),
    (FloodMessage::class),
    (Address::class),
    (ResendMessage::class),
    (StickerRelationship::class),
    (TopAsset::class),
    (Job::class)], version = 1)
abstract class FoneDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun participantDao(): ParticipantDao
    abstract fun offsetDao(): OffsetDao
    abstract fun assetDao(): AssetDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun messageHistoryDao(): MessageHistoryDao
    abstract fun sentSenderKeyDao(): SentSenderKeyDao
    abstract fun stickerDao(): StickerDao
    abstract fun stickerAlbumDao(): StickerAlbumDao
    abstract fun appDao(): AppDao
    abstract fun hyperlinkDao(): HyperlinkDao
    abstract fun floodMessageDao(): FloodMessageDao
    abstract fun jobDao(): JobDao
    abstract fun addressDao(): AddressDao
    abstract fun resendMessageDao(): ResendMessageDao
    abstract fun stickerRelationshipDao(): StickerRelationshipDao
    abstract fun topAssetDao(): TopAssetDao

    companion object {
        private var INSTANCE: FoneDatabase? = null
        private var READINSTANCE: FoneDatabase? = null

        private val lock = Any()
        private val readlock = Any()
        private var supportSQLiteDatabase: SupportSQLiteDatabase? = null

        fun getDatabase(context: Context): FoneDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, FoneDatabase::class.java, DB_NAME)
                        .enableMultiInstanceInvalidation()
                        .addCallback(CALLBACK)
                        .build()
                }
                return INSTANCE as FoneDatabase
            }
        }

        @Transaction
        fun checkPoint() {
            supportSQLiteDatabase?.query("PRAGMA wal_checkpoint(FULL)")?.close()
        }

        fun getReadDatabase(context: Context): FoneDatabase {
            synchronized(readlock) {
                if (READINSTANCE == null) {
                    READINSTANCE = Room.databaseBuilder(context, FoneDatabase::class.java, DB_NAME)
                        .enableMultiInstanceInvalidation()
                        .build()
                }
                return READINSTANCE as FoneDatabase
            }
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("CREATE TRIGGER conversation_last_message_update AFTER INSERT ON messages BEGIN UPDATE conversations SET last_message_id = new.id WHERE conversation_id = new.conversation_id; END")
                db.execSQL("CREATE TRIGGER conversation_last_message_delete AFTER DELETE ON messages BEGIN UPDATE conversations SET last_message_id = (select id from messages where conversation_id = old.conversation_id order by created_at DESC limit 1) WHERE conversation_id = old.conversation_id; END")
                db.execSQL("CREATE TRIGGER conversation_unseen_message_count_insert AFTER INSERT ON messages BEGIN UPDATE conversations SET unseen_message_count = (SELECT count(m.id) FROM messages m, users u WHERE m.user_id = u.user_id AND u.relationship != 'ME' AND m.status = 'DELIVERED' AND conversation_id = new.conversation_id) where conversation_id = new.conversation_id; END")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                supportSQLiteDatabase = db
                db.execSQL("CREATE TRIGGER IF NOT EXISTS conversation_last_message_update AFTER INSERT ON messages BEGIN UPDATE conversations SET " +
                        "last_message_id = new.id WHERE conversation_id = new.conversation_id; END")
                db.execSQL("CREATE TRIGGER IF NOT EXISTS conversation_last_message_delete AFTER DELETE ON messages BEGIN UPDATE conversations SET last_message_id = (select id from messages where conversation_id = old.conversation_id order by created_at DESC limit 1) WHERE conversation_id = old.conversation_id; END")
                db.execSQL("CREATE TRIGGER IF NOT EXISTS conversation_unseen_message_count_insert AFTER INSERT ON messages BEGIN UPDATE conversations SET unseen_message_count = (SELECT count(m.id) FROM messages m, users u WHERE m.user_id = u.user_id AND u.relationship != 'ME' AND m.status = 'DELIVERED' AND conversation_id = new.conversation_id) where conversation_id = new.conversation_id; END")
            }
        }
    }
}