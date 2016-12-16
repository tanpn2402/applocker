package com.tanpn.applocker.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.tanpn.applocker.utils.AppDetail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamt_000 on 12/2/16.
 */
public class SQLAppPassword extends SQLiteOpenHelper {

    // Phiên bản
    private static final int DATABASE_VERSION = 1;

    // Tên cơ sở dữ liệu.
    private static final String DATABASE_NAME = "APP_PASSWORD";

    private static final String TABLE_NAME = "APPPASSWORD";

    private static final String COLUMN_APP_ID ="APP_ID";
    private static final String COLUMN_APP_PACKAGE ="APP_PACKAGE";
    private static final String COLUMN_APP_PASSWORD = "APP_PASSWORD";

    private static final String FILE_DIR = "TAN";

    public SQLAppPassword(Context context) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String script = "CREATE TABLE " + TABLE_NAME +
                "(" +
                COLUMN_APP_ID + "INTEGER PRIMARY KEY, " +
                COLUMN_APP_PACKAGE + " TEXT, " +
                COLUMN_APP_PASSWORD + " TEXT " +
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


    /**
     * lay password
     * cung dung ham nay de kiem tra xem app nay co trong danh sach hay chua
     * */

    public String getPassword(String app){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_APP_PASSWORD },
                COLUMN_APP_PACKAGE + "=?",
                new String[] { String.valueOf(app) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return null;
        else
            return cursor.getString(0);
    }

    public int insertApp(String app){

        // check if already exist
        if(getPassword(app) == null){

            /// chua co

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_APP_PACKAGE, app);
            values.put(COLUMN_APP_PASSWORD, "");
            //values.put(COLUMN_GROUP_NEMBER_COUNT, "");

            // Trèn một dòng dữ liệu vào bảng.
            db.insert(TABLE_NAME, null, values);

            // Đóng kết nối database.
            db.close();

            return 1;
        }
        else
            return -1;
    }

    public int insertApp(List<AppDetail> app){

        SQLiteDatabase db = this.getWritableDatabase();

        for(AppDetail a : app){

            if(getPassword(a.getAppPackage()) == null){
                ContentValues values = new ContentValues();
                values.put(COLUMN_APP_PACKAGE, a.getAppPackage());
                values.put(COLUMN_APP_PASSWORD, "");
                //values.put(COLUMN_GROUP_NEMBER_COUNT, "");

                // Trèn một dòng dữ liệu vào bảng.
                db.insert(TABLE_NAME, null, values);
            }

        }

        return 1;
    }

    public int updatePassword(String app, String pass){
        if(getPassword(app) == null){
            return -1; // k ton tai app nay
        }
        else{
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_APP_PASSWORD, pass);

            // updating row
            return db.update(TABLE_NAME, values, COLUMN_APP_PACKAGE + " = ?",
                    new String[]{String.valueOf(app)});
        }
    }

    public List<String> getAllAppsLocked(){

        List<String> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.
        if (cursor.moveToFirst()) {
            do {

                if(!cursor.getString(2).equals("")){
                    list.add(cursor.getString(1));
                }

            } while (cursor.moveToNext());
        }

        return list;
    }
}
