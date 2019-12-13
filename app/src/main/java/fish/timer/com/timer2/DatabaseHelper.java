package fish.timer.com.timer2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fish 11/30/2019.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Timer7.db";
    public static final String TABLE_NAME = "Timer7_table";
    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String B_NAMES = "B_NAMES";
    public static final String B_TIMES = "B_TIMES";
    public static final String B_COLORS = "B_COLORS";
    public static final String B_DESCRIPTION = "B_DESCRIPTION";
    public static final String RATIO = "RATIO";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,B_NAMES TEXT,B_TIMES TEXT,B_COLORS TEXT,B_DESCRIPTION TEXT,RATIO INTEGER)");
    }
    public Cursor getIdAndNames(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID,Name from "+TABLE_NAME,null);
        return res;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name,String b_names,String b_times,String b_colors,String b_description,int ratio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME,name);
        contentValues.put(B_NAMES,b_names);
        contentValues.put(B_TIMES,b_times);
        contentValues.put(B_COLORS,b_colors);
        contentValues.put(B_DESCRIPTION,b_description);
        contentValues.put(RATIO,ratio);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME +" WHERE " +ID + " = ?"  ,new String[] {id});
        return res;
    }

    public boolean updateData(String id,String name,String b_names,String b_times,String b_colors,String b_description,int ratio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID,id);
        contentValues.put(NAME,name);
        contentValues.put(B_NAMES,b_names);
        contentValues.put(B_TIMES,b_times);
        contentValues.put(B_COLORS,b_colors);
        contentValues.put(B_DESCRIPTION,b_description);
        contentValues.put(RATIO,ratio);
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }
}