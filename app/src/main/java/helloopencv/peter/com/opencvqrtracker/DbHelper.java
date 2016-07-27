package helloopencv.peter.com.opencvqrtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by linweijie on 7/27/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    public final static int VERSION = 1;                //<-- 版本
    public final static String DB_NAME = "Threshold.db";  //<-- db name

    private static SQLiteDatabase database;

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context c){
        if (database == null || !database.isOpen()){
            database = new DbHelper(c, DB_NAME, null, VERSION).getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ThresholdDBA.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ThresholdDBA.DROP_TABLE);
        onCreate(db);
    }
}
