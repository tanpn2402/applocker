package com.tanpn.applocker.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.tanpn.applocker.utils.AppDetail;
import com.tanpn.applocker.utils.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * lưu lại những app bị cố gắng mở khóa quá xxx lần
 */
public class SQLAppUnLock extends SQLiteOpenHelper {
    // Phiên bản
    private static final int DATABASE_VERSION = 1;

    // Tên cơ sở dữ liệu.
    private static final String DATABASE_NAME = "APP_UNLOCK";

    private static final String TABLE_NAME = "APPUNLOCK";

    private static final String COLUMN_APP_ID ="APP_ID";
    private static final String COLUMN_APP_PACKAGE ="APP_PACKAGE";
    private static final String COLUMN_DATETIME = "DATETIME";
    private static final String COLUMN_LOCATION = "LOCATION";
    private static final String COLUMN_PHOTO = "PHOTO";


    private static final String FILE_DIR = "TAN";

    public SQLAppUnLock(Context context) {
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
                COLUMN_DATETIME + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_PHOTO + " TEXT " +
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
     * Khi inssert 1 app mở không thành công thì kiểm tra app_package này đã có trong bảng hay chưa
     *  - nếu có thì add thêm datatime, photo và location  -> đó là lí do tại sao 3 thuộc tính này dùng list
     *  - nếu không thì tạo hàng mới như bình thường
     *
     * */
    public void insert(String app_package){
        AppDetail app = getOne(app_package);
        if(app != null){
            update(app_package, app, "");
        }
        else{
            create(app_package, "");
        }
    }

    public void insert(String app_package, String photo_link){
        AppDetail app = getOne(app_package);
        if(app != null){
            update(app_package, app, photo_link);
        }
        else{
            create(app_package, photo_link);
        }
    }

    private String currentDateTime = "";
    private String currentLocation = "";
    private String targetPhoto = "";


    private void getImformation(){
        currentDateTime = utils.getCurrentTimeStamp();

        currentLocation = "";


        targetPhoto= "";



    }



    private void create(String app_package, String photo_link) {

        getImformation(); // cap nhat thong tin
        List<String> l1 = new ArrayList<>();
        l1.add(currentDateTime);

        List<String> l2 = new ArrayList<>();
        l2.add(currentLocation);

        List<String> l3 = new ArrayList<>();
        l3.add(photo_link);



        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_PACKAGE, app_package);
        values.put(COLUMN_DATETIME, l1.toString());
        values.put(COLUMN_LOCATION, l2.toString());
        values.put(COLUMN_PHOTO, l3.toString());


        // Trèn một dòng dữ liệu vào bảng.
        db.insert(TABLE_NAME, null, values);

        // Đóng kết nối database.
        db.close();
    }

    private int update(String app_package, AppDetail app, String photo_link) {

        getImformation();  // update thong tin

        app.getDatetime().add(currentDateTime);
        app.getLocation().add(currentLocation);
        app.getPhoto().add(photo_link);


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, app.getDatetime().toString());
        values.put(COLUMN_LOCATION, app.getLocation().toString());
        values.put(COLUMN_PHOTO, app.getPhoto().toString());

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_APP_PACKAGE + " = ?",
                new String[]{String.valueOf(app_package)});
    }

    public void removeAll(){

    }

    public AppDetail getOne(String app_package){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        COLUMN_DATETIME,
                        COLUMN_LOCATION,
                        COLUMN_PHOTO}, COLUMN_APP_PACKAGE + "=?",
                new String[] { String.valueOf(app_package) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return null;

        /**
         * mình chỉ có thông tin về package, date time, photo và loccation, còn mấy thông tin kia mình sẽ update sau
         * */
        return new AppDetail(app_package, null, null, 0, cursor.getString(0), cursor.getString(1), cursor.getString(2));
    }

    public List<AppDetail> getAll(){
        List<AppDetail> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // Duyệt trên con trỏ, và thêm vào danh sách.
        if (cursor.moveToFirst()) {
            do {

                list.add(new AppDetail(cursor.getString(1), null, null, 0, cursor.getString(2), cursor.getString(3), cursor.getString(4)));

            } while (cursor.moveToNext());
        }

        return list;
    }
}
