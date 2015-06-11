package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SqliteHelpers extends SQLiteOpenHelper {

    private final String LOGTAG = AppGlobals.LOG_TAG + "/" + getClass().getName();
    public static final String DATABASE_NAME = "Recordings.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "Recordings_table";
    public static final String ID = "ID";
    public static final String COULMN_UPLOAD = "DATA_UP";
    public static final String COULMN_DELETE = "DATA_DEL";

    public static final String TABLE_CREATE =
            "CREATE TABLE " +
            TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COULMN_DELETE + " TEXT , " +
                    COULMN_UPLOAD + " TEXT  " + " ) ";

    public SqliteHelpers(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        Log.i(LOGTAG , "tabel created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);

    }
}
