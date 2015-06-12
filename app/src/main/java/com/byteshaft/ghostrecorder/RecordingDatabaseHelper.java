package com.byteshaft.ghostrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class RecordingDatabaseHelper {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());
    SQLiteDatabase mDbHelper;
    SqliteHelpers mSqliteHelper;
    Cursor mCursor;

    public RecordingDatabaseHelper(Context context) {
        mSqliteHelper = new SqliteHelpers(context);
        mDbHelper = mSqliteHelper.getWritableDatabase();
        mDbHelper = mSqliteHelper.getReadableDatabase();

    }

    void createNewEntry(String column, String value) {
        ContentValues values = new ContentValues();
        values.put(column, value);
        mDbHelper.insert(SqliteHelpers.TABLE_NAME, null, values);
        Log.i(LOG_TAG, "open database");
    }

    void deleteItem(String column, String value) {
        mDbHelper.delete(SqliteHelpers.TABLE_NAME, column + " = ?", new String[]{value});
        Log.i(LOG_TAG, "Entry deleted");
    }

    void closeDatabase() {
        mSqliteHelper.close();
        mCursor.close();
        Log.i(LOG_TAG, "close database");
    }

    ArrayList<String> retrieveDate(String column) {
        String query = "SELECT * FROM " + SqliteHelpers.TABLE_NAME;
        mCursor = mDbHelper.rawQuery(query, null);
        mCursor.moveToFirst();
        ArrayList<String> arrayList = new ArrayList<>();
        while (mCursor.moveToNext()) {
            String itemname = mCursor.getString(mCursor.getColumnIndex(column));
            if (itemname != null) {
                System.out.println(itemname);
                arrayList.add(itemname);
            }
        }
        return arrayList;
    }
}