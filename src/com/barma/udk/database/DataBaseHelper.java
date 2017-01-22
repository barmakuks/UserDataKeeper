package com.barma.udk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database creator
 * Created by vitalii on 11/18/14.
 */
class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RECORD_TABLE_CREATE);
        db.execSQL(ITEMS_TABLE_CREATE);
        db.execSQL(PARAMS_TABLE_CREATE);
        db.execSQL(TEMPLATE_TABLE_CREATE);
        db.execSQL(TEMPLATE_ITEMS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "user_data_keeper";

    private static final String RECORD_TABLE_CREATE = "CREATE TABLE records (id CHARACTER(36) PRIMARY KEY, public_name TEXT);";
    private static final String ITEMS_TABLE_CREATE = "CREATE TABLE items (record_id CHARACTER(36) REFERENCES records (id) ON DELETE CASCADE, key_name TEXT, value TEXT);";
    private static final String PARAMS_TABLE_CREATE = "CREATE TABLE params (key CHARACTER(20) PRIMARY KEY, value TEXT);";
    private static final String TEMPLATE_TABLE_CREATE = "CREATE TABLE templates (id CHARACTER(36) PRIMARY KEY, public_name TEXT);";
    private static final String TEMPLATE_ITEMS_TABLE_CREATE = "CREATE TABLE template_items (record_id CHARACTER(36) REFERENCES templates (id) ON DELETE CASCADE, key_name TEXT);";
}
