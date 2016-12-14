package com.tanpn.applocker.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by phamt_000 on 11/29/16.
 */
public class SQLAppUsages extends SQLiteOpenHelper {

    // Phiên bản
    private static final int DATABASE_VERSION = 1;

    // Tên cơ sở dữ liệu.
    private static final String DATABASE_NAME = "APP_USAGE";

    private static final String TABLE_NAME = "APP";

    private static final String COLUMN_APP_ID ="APP_IS";
    private static final String COLUMN_APP_TITLE ="APP_TITLE";
    private static final String COLUMN_APP_PACKAGE ="APP_PACKAGE";
    private static final String COLUMN_APP_USAGE = "APP_USAGE";

    public SQLAppUsages(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String script = "CREATE TABLE " + TABLE_NAME +
                "(" +
                COLUMN_APP_ID + "INTEGER PRIMARY KEY, " +
                COLUMN_APP_TITLE + " TEXT," +
                COLUMN_APP_PACKAGE + " TEXT, " +
                COLUMN_APP_USAGE + " INTEGER " +
                ")";
        // Chạy lệnh tạo bảng.
        sqLiteDatabase.execSQL(script);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Hủy (drop) bảng cũ nếu nó đã tồn tại.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);


        // Và tạo lại.
        onCreate(sqLiteDatabase);
    }

    public int updateValue(String apppackage){

        // kiem tra co app nay trong danh sach hay k, neu co thi update, neu k thi them moi
        if(checkAlreadyExist(apppackage, "")){
            // co
            int newValue = getValue(apppackage) + 1;

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_APP_USAGE, newValue);

            Log.i(TAG, apppackage + "  " + newValue);

            // updating row
            return db.update(TABLE_NAME, values, COLUMN_APP_PACKAGE + " = ?",
                    new String[]{String.valueOf(apppackage)});
        }
        else{
            // them moi
            String[] appname = apppackage.split("\\.");
            insert(apppackage,appname[appname.length - 1] , 1);
            return 1;
        }

    }

    public int getValue(String _package){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                COLUMN_APP_TITLE,
                COLUMN_APP_PACKAGE,
                COLUMN_APP_USAGE }, COLUMN_APP_PACKAGE + "=?",
                new String[] { String.valueOf(_package) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return 0;
        return Integer.parseInt(cursor.getString(2));
    }


    public Map<String, Integer> getAll(){
        Map<String, Integer> map = new HashMap<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.
        if (cursor.moveToFirst()) {
            do {

                String apppackage = cursor.getString(2);
                int usage = Integer.parseInt(cursor.getString(3));

                map.put(apppackage, usage);

            } while (cursor.moveToNext());
        }


        return map;
    }

    public boolean checkAlreadyExist(String apppackage, String appname){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_APP_TITLE,
                        COLUMN_APP_PACKAGE,
                        COLUMN_APP_USAGE }, COLUMN_APP_PACKAGE + "=?",
                new String[] { String.valueOf(apppackage) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return false;
        else
            return true;
    }
    private final String TAG = "SQL_TAG";
    public void insert(String apppackage, String appname, int defValue){

        Log.i(TAG, "insert " + apppackage);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_TITLE, appname);
        values.put(COLUMN_APP_PACKAGE, apppackage);
        values.put(COLUMN_APP_USAGE, defValue);

        // Trèn một dòng dữ liệu vào bảng.
        db.insert(TABLE_NAME, null, values);


        // Đóng kết nối database.
        db.close();
    }

    public void reset(){
        Map<String, Integer> map = getAll();
        for(Map.Entry<String, Integer> m : map.entrySet()){
            updateValue(m.getKey());
        }
    }


}
