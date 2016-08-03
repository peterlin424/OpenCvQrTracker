package helloopencv.peter.com.opencvqrtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

/**
 * Created by linweijie on 7/27/16.
 */
public class ThresholdDBA {

    public final static String TABLE_NAME = "treshholdTable";    //<-- table name

    public final static String COL_ID = "_ID";
    public final static String COL_THRESHOLD = "_THRESHOLD";
    public final static String COL_WHITEBALANCE = "_WHITEBALANCE";

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
                            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COL_THRESHOLD + " TEXT NOT NULL, " +
                            COL_WHITEBALANCE + " TEXT NOT NULL" +
                        ");";
    public static String DROP_TABLE = "DROP TABLE " + TABLE_NAME;

    private SQLiteDatabase db;

    public ThresholdDBA(Context c){
        db = DbHelper.getDatabase(c);
    }
    public void closeDB(){
        db.close();
    }

    public long insert(String thv, String wbv){
        ContentValues values = new ContentValues();
        values.put(COL_THRESHOLD, thv);
        values.put(COL_WHITEBALANCE, wbv);
        return db.insert(TABLE_NAME, null, values);
    }

    public int update(String thv, String wbv){
        String where = COL_ID + "=" + 1;
        ContentValues values = new ContentValues();
        values.put(COL_THRESHOLD, thv);
        values.put(COL_WHITEBALANCE, wbv);
        return db.update(TABLE_NAME, values, where, null);
    }

    public HashMap<String, String> query(){
        String where = COL_ID + "=" + 1;
        Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null);

        HashMap<String, String> out = new HashMap<>();
        if (cursor.moveToFirst()){
            String thv = cursor.getString(cursor.getColumnIndex(COL_THRESHOLD));
            String wbv = cursor.getString(cursor.getColumnIndex(COL_WHITEBALANCE));
            out.put(COL_THRESHOLD, thv);
            out.put(COL_WHITEBALANCE, wbv);
            return out;
        }

        return null;
    }

    public long delete(){
        String where = COL_ID + "=" + 1;
        return db.delete(TABLE_NAME, where, null);
    }

    public int getCount(){
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        return cursor.getCount();
    }

    public Cursor getAll(){
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }
}
