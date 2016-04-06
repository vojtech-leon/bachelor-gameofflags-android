package vojtele1.gameofflags.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

/**
 * Database pro ukládání scanů, než se pošlou na server
 *
 * Created by Leon on 18.02.2016.
 */
public class Scans {
    protected static final String DATABASE_NAME = "gameofflags";
    protected static final int DATABASE_VERSION = 2;

    protected static final String TB_NAME = "scan";

    // Speciální hodnota "_id", pro jednodušší použití SimpleCursorAdapteru
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Fingerprint = "fingerprint";
    public static final String COLUMN_Date = "date";
    public static final String COLUMN_Odeslano = "odeslano";
    public static final String COLUMN_Flag = "flag";

    private SQLiteOpenHelper openHelper;
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TB_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_Fingerprint + " TEXT NOT NULL,"
                    + COLUMN_Date + " DATETIME NOT NULL,"
                    + COLUMN_Odeslano + " BOOLEAN NOT NULL,"
                    + COLUMN_Flag + " INTEGER NOT NULL"
                    + ");");
        }

        /*
         * Ve skutečnosti je potřeba, abychom uživatelům nemazali data, vytvořit
         * pro každou změnu struktury databáze nějaký upgradovací nedestruktivní
         * SQL příkaz.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS scan");
            onCreate(db);
        }
    }

    public Scans(Context ctx) {
        openHelper = new DatabaseHelper(ctx);
    }
    public static final String[] columns = { COLUMN_ID, COLUMN_Fingerprint, COLUMN_Date,
            COLUMN_Odeslano, COLUMN_Flag };

    protected static final String ORDER_BY = COLUMN_ID + " DESC";


    public Cursor getScans() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
    }

    public Cursor getScan(long id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        return db.query(TB_NAME, columns, COLUMN_ID + "= ?", selectionArgs,
                null, null, ORDER_BY);
    }

    public void deleteScan(long id) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        String[] selectionArgs = { String.valueOf(id) };

        db.delete(TB_NAME, COLUMN_ID + "= ?", selectionArgs);
    }

    public void insertScan(String fingerprint, int flag) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Date date = new Date();
        ContentValues values = new ContentValues();
        values.put(COLUMN_Fingerprint, fingerprint);
        values.put(COLUMN_Date, date.getTime());  // je to v ms
        values.put(COLUMN_Odeslano, false);
        values.put(COLUMN_Flag, flag);

        db.insert(TB_NAME, null, values);
    }
    public void updateScan(String date) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String[] selectionArgs = { date };
        values.put(COLUMN_Odeslano, true);

        db.update(TB_NAME, values, COLUMN_Date + "= ?", selectionArgs);
    }

    public void close() {
        openHelper.close();
    }
}