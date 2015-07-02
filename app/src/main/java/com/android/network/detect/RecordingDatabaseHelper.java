package com.android.network.detect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class RecordingDatabaseHelper {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());
    private SQLiteDatabase mDbHelper;
    private SqliteHelpers mSqliteHelper;
    private Cursor mCursor;

    public RecordingDatabaseHelper(Context context) {
        mSqliteHelper = new SqliteHelpers(context);
    }

    void createNewEntry(String column, String value) {
        mDbHelper = mSqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column, value);
        mDbHelper.insert(SqliteHelpers.TABLE_NAME, null, values);
        Log.i(LOG_TAG, "open database");
    }

    void deleteItem(String column, String value) {
        mDbHelper = mSqliteHelper.getWritableDatabase();
        mDbHelper.delete(SqliteHelpers.TABLE_NAME, column + "=?", new String[]{value});
    }
    
    ArrayList<String> retrieveDate(String column) {
        mDbHelper = mSqliteHelper.getReadableDatabase();
        String query = "SELECT * FROM " + SqliteHelpers.TABLE_NAME;
        mCursor = mDbHelper.rawQuery(query, null);
        ArrayList<String> arrayList = new ArrayList<>();
        while (mCursor.moveToNext()) {
            String itemName = mCursor.getString(mCursor.getColumnIndex(column));
            if (itemName != null) {
                arrayList.add(itemName);
            }
        }
        return arrayList;
    }

    boolean CheckIfItemAlreadyExist(String item) {
        mDbHelper = mSqliteHelper.getReadableDatabase();
        String query = "SELECT * FROM " + SqliteHelpers.TABLE_NAME;
        mCursor = mDbHelper.rawQuery("SELECT * FROM " + SqliteHelpers.TABLE_NAME +
                " WHERE " +SqliteHelpers.COULMN_UPLOAD+ "  =? ", new String[]{item});
        if (mCursor == null) {
            return false;
        } else {
            return true;
        }

    }
}