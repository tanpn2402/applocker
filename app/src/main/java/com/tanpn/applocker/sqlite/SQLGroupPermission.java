package com.tanpn.applocker.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.tanpn.applocker.user.GroupPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class SQLGroupPermission  extends SQLiteOpenHelper {

    // Phiên bản
    private static final int DATABASE_VERSION = 1;

    // Tên cơ sở dữ liệu.
    private static final String DATABASE_NAME = "PERMISSION";

    private static final String TABLE_NAME = "GROUP_PERMISSION";

    private static final String COLUMN_GROUP_ID ="GROUP_ID";
    private static final String COLUMN_GROUP_TITLE ="GROUP_TITLE";
    private static final String COLUMN_GROUP_PASSWORD ="GROUP_PASSWORD";
    private static final String COLUMN_GROUP_NEMBER = "GROUP_NEMBER";
    private static final String COLUMN_GROUP_NEMBER_COUNT = "GROUP_NEMBER_COUNT";

    private static final String FILE_DIR = "TAN";

    public SQLGroupPermission(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String script = "CREATE TABLE " + TABLE_NAME +
                "(" +
                COLUMN_GROUP_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_GROUP_TITLE + " TEXT," +
                COLUMN_GROUP_PASSWORD + " TEXT, " +
                COLUMN_GROUP_NEMBER + " TEXT " +
                COLUMN_GROUP_NEMBER_COUNT + " INTEGER " +
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
     * Lấy nhóm ( tra ve ten package )

     * */

    public GroupPermission getGroups(String groupname){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_GROUP_PASSWORD,
                        COLUMN_GROUP_NEMBER}, COLUMN_GROUP_TITLE + "=?",
                new String[] { String.valueOf(groupname) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return null;


        return new GroupPermission(groupname, cursor.getString(0), 0, cursor.getString(1));
    }


    /**
     * thêm 1 app nào đó vào nhóm
     * */
    public int addAppNember(String groupname, String apppackage){

        GroupPermission group = getGroups(groupname);
        if(group == null){
            // chua co nhom ten nay
            return 1;
        }

       if(!group.getApps().contains(apppackage))
           group.addApp(apppackage);


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NEMBER, group.getApps().toString());

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_GROUP_TITLE + " = ?",
                new String[]{String.valueOf(groupname)});

    }

    /**
     * xoa 1 app trong nhom
     * */
    public int removeAppNember(String groupname, String apppackage){

        GroupPermission group = getGroups(groupname);
        if(group == null){
            // chua co nhom ten nay
            return 1;
        }

        group.removeApp(apppackage);


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NEMBER, group.getApps().toString());

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_GROUP_TITLE + " = ?",
                new String[]{String.valueOf(groupname)});

    }

    /**
     * kiem tra xem co group nay chua
     * */
    public boolean checkAlreadyExist(String groupname){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_GROUP_PASSWORD,
                        COLUMN_GROUP_NEMBER }, COLUMN_GROUP_TITLE + "=?",
                new String[] { String.valueOf(groupname) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return false;
        else
            return true;
    }

    /**
     * tao moi 1 group
     * */
    public int insertGroup(String groupName, String password){
        // kiem tra co group nay trong danh sach hay k, neu co thi update, neu k thi them moi
        if(!checkAlreadyExist(groupName)){
            // chua co group nay thi tao

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_GROUP_TITLE, groupName);
            values.put(COLUMN_GROUP_PASSWORD, password);
            values.put(COLUMN_GROUP_NEMBER, "");
            //values.put(COLUMN_GROUP_NEMBER_COUNT, "");

            // Trèn một dòng dữ liệu vào bảng.
            db.insert(TABLE_NAME, null, values);


            // Đóng kết nối database.
            db.close();

            return 1;
        }
        else{
            return -1; // group nay da trung ten
        }
    }

    public int removeGroup(){
        return 1;
    }

    public List<GroupPermission> getAllGroups(){

        List<GroupPermission> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.

        if (cursor.moveToFirst()) {
            do {



                /*String listApp = "";
                List<String> s = Arrays.asList(cursor.getString(3).split("\\|"));
                for(String f : s){
                    if(!f.equals("")){
                        listApp += "|" + f;
                    }
                }
                if(listApp.length() > 0 && listApp.charAt(0) == '|')
                    listApp = listApp.replaceFirst("\\|", "");
*/
                //int count = Integer.parseInt(cursor.getString(4));

                list.add(new GroupPermission(
                        cursor.getString(1),
                        cursor.getString(2),
                        0,
                        cursor.getString(3)));


            } while (cursor.moveToNext());
        }

        return list;
    }


    public String getPassword(String groupname){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_GROUP_PASSWORD
                        }, COLUMN_GROUP_TITLE + "=?",
                new String[] { String.valueOf(groupname) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return null;

        return cursor.getString(0);
    }


}
