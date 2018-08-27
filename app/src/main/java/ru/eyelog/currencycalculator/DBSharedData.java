package ru.eyelog.currencycalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;

public class DBSharedData extends SQLiteOpenHelper implements BaseColumns {

    SQLiteDatabase sqldb;
    ContentValues cv;
    Cursor cursor, subcursor;

    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> hm;
    String subId;

    private final static String DB_FILE = "shareddata.db";
    private final static int DB_VERSION = 1;

    private final static String TABLE_NAME_DATA = "shareddatalist";

    private final String TAG_NUMCODE = "NumCode";
    private final String TAG_CHARCODE = "CharCode";
    private final String TAG_NOMINAL = "Nominal";
    private final String TAG_NAME = "Name";
    private final String TAG_VALUE = "Value";

    private final static String COLUMN_NUMCODE = "numcode";
    private final static String COLUMN_CHARCODE = "charcode";
    private final static String COLUMN_NOMINAL = "nominal";
    private final static String COLUMN_NAME = "name";
    private final static String COLUMN_VALUE = "value";

    private final static String DB_CREATE_SCRIPT = "create table " + TABLE_NAME_DATA + " ("
            + BaseColumns._ID + " integer primary key autoincrement, " + COLUMN_NUMCODE + " text, "
            +  COLUMN_CHARCODE + " text, " +  COLUMN_NOMINAL + " text, " +  COLUMN_NAME + " text, "
            +  COLUMN_VALUE + " text);";

    DBSharedData(Context context) {
        super(context, DB_FILE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME_DATA);
    }

    public void createData(ArrayList<HashMap<String, String>> dataList){

        sqldb = this.getWritableDatabase();
        for(int i=0; i<dataList.size(); i++){
            cv = new ContentValues();
            cv.put(COLUMN_NUMCODE, dataList.get(i).get(TAG_NUMCODE));
            cv.put(COLUMN_CHARCODE, dataList.get(i).get(TAG_CHARCODE));
            cv.put(COLUMN_NOMINAL, dataList.get(i).get(TAG_NOMINAL));
            cv.put(COLUMN_NAME, dataList.get(i).get(TAG_NAME));
            cv.put(COLUMN_VALUE, dataList.get(i).get(TAG_VALUE));

            sqldb.insert(TABLE_NAME_DATA, null, cv);
        }

        sqldb.close();
    }

    public ArrayList<HashMap<String, String>> readData(){
        data = new ArrayList<>();
        sqldb = this.getReadableDatabase();

        cursor = sqldb.rawQuery("select * from " + TABLE_NAME_DATA, null);

        if(cursor.moveToFirst()){
            do{
                hm = new HashMap<>();
                //hm.put("MAIN_ID", String.valueOf(cursor.getInt(0)));
                hm.put(TAG_NUMCODE, cursor.getString(1));
                hm.put(TAG_CHARCODE, cursor.getString(2));
                hm.put(TAG_NOMINAL, cursor.getString(3));
                hm.put(TAG_NAME, cursor.getString(4));
                hm.put(TAG_VALUE, cursor.getString(5));
                data.add(hm);
            }while (cursor.moveToNext());
        }
        sqldb.close();

        return data;
    }

    public void updateData(ArrayList<HashMap<String, String>> dataList){
        sqldb = this.getWritableDatabase();
        cursor = sqldb.rawQuery("select * from " + TABLE_NAME_DATA, null);

        for(int i=0; i<cursor.getCount(); i++){
            cursor.moveToPosition(i);
            sqldb.delete(TABLE_NAME_DATA, _ID + " =?", new String[]{cursor.getString(0)});
        }

        for(int i=0; i<dataList.size(); i++){
            cv = new ContentValues();
            cv.put(COLUMN_NUMCODE, dataList.get(i).get(TAG_NUMCODE));
            cv.put(COLUMN_CHARCODE, dataList.get(i).get(TAG_CHARCODE));
            cv.put(COLUMN_NOMINAL, dataList.get(i).get(TAG_NOMINAL));
            cv.put(COLUMN_NAME, dataList.get(i).get(TAG_NAME));
            cv.put(COLUMN_VALUE, dataList.get(i).get(TAG_VALUE));

            sqldb.insert(TABLE_NAME_DATA, null, cv);
        }
        sqldb.close();
    }
}
