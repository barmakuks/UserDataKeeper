package com.barma.udk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.barma.udk.core.Record;
import com.barma.udk.core.interfaces.ICipher;
import com.barma.udk.core.interfaces.IStorage;

import java.util.ArrayList;
import java.util.UUID;

/**
 * SQLite data storage Created by vitalii on 11/18/14.
 */
public class DbStorage implements IStorage {

    public DbStorage(Context context) {
        SQLiteOpenHelper helper = new DataBaseHelper(context);
        m_database = helper.getWritableDatabase();
    }

    @Override
    protected void finalize() throws Throwable {
        m_database.close();
        super.finalize();
    }

    @Override
    public void saveBegin() {
        // Do nothing
    }

    @Override
    public void saveRecord(Record record) {
        // remove all items
        m_database.delete(ITEMS_TABLE, "record_id='" + record.getId().toString() + "'", null);

        // update or insert record
        final ContentValues values = new ContentValues();
        values.put("public_name", record.getPublicName());

        if (checkIfExists(RECORDS_TABLE, "id", record.getId().toString())) {
            m_database.update(RECORDS_TABLE, values, "id = '" + record.getId().toString() + "'", null);
        }
        else {
            values.put("id", record.getId().toString());
            m_database.insert(RECORDS_TABLE, null, values);
        }

        // insert all items
        for (int i = 0; i < record.getItemsCount(); ++i)
        {
            final ContentValues item_values = new ContentValues();
            item_values.put("record_id", record.getId().toString());
            item_values.put("key_name", record.getItem(i).key);
            item_values.put("value", record.getItem(i).value);
            m_database.insert(ITEMS_TABLE, null, item_values);
        }
    }

    @Override
    public void removeRecord(Record record) {
        // remove all items
        m_database.delete(ITEMS_TABLE, "record_id='" + record.getId().toString() + "'", null);
        // remove record
        m_database.delete(RECORDS_TABLE, "id='" + record.getId().toString() + "'", null);
    }

    @Override
    public void saveTemplate(Record template) {
        // remove all items
        m_database.delete(TEMPLATE_ITEMS_TABLE, "record_id='" + template.getId().toString() + "'", null);

        // update or insert record
        final ContentValues values = new ContentValues();
        values.put("public_name", template.getPublicName());

        if (checkIfExists(TEMPLATES_TABLE, "id", template.getId().toString())) {
            m_database.update(TEMPLATES_TABLE, values, "id = '" + template.getId().toString() + "'", null);
        }
        else {
            values.put("id", template.getId().toString());
            m_database.insert(TEMPLATES_TABLE, null, values);
        }

        // insert all items
        for (int i = 0; i < template.getItemsCount(); ++i)
        {
            final ContentValues item_values = new ContentValues();
            item_values.put("record_id", template.getId().toString());
            item_values.put("key_name", template.getItem(i).key);
            m_database.insert(TEMPLATE_ITEMS_TABLE,  null, item_values);
        }
    }

    @Override
    public void removeTemplate(Record template) {
        // remove all items
        m_database.delete(TEMPLATE_ITEMS_TABLE, "record_id='" + template.getId().toString() + "'", null);
        // remove record
        m_database.delete(TEMPLATES_TABLE, "id='" + template.getId().toString() + "'", null);
    }

    @Override
    public void saveEnd() {
        // Do nothing
    }

    @Override
    public boolean load() {
        // load ID list of all records
        {
            m_record_keys.clear();
            Cursor res = m_database.query(RECORDS_TABLE, new String[]{"id", "public_name"}, null, null, null, null, "public_name COLLATE NOCASE");

            if (res.moveToFirst()) {
                do {
                    m_record_keys.add(res.getString(0));
                } while (res.moveToNext());
            }
            res.close();
        }

        // load ID list of all templates
        {
            m_template_keys.clear();
            Cursor res = m_database.query(TEMPLATES_TABLE, new String[]{"id"}, null, null, null, null, null);

            if (res.moveToFirst()) {
                do {
                    m_template_keys.add(res.getString(0));
                } while (res.moveToNext());
            }
            res.close();
        }

        return true;
    }

    @Override
    public boolean checkIntegrity(ICipher cipher) {
        final String passHash = getParamValue(PARAM_PASSWORD_HASH);

        return passHash.equals(cipher.getPasswordHash());
    }

    @Override
    public boolean isPasswordSet() {
        final String hash = getParamValue(PARAM_PASSWORD_HASH);
        return !hash.isEmpty();
    }

    @Override
    public void setupPassword(String passwordHash) {
        setParamValue(PARAM_PASSWORD_HASH, passwordHash);
    }

    @Override
    public int getRecordCount() {
        return m_record_keys.size();
    }

    @Override
    public Record getRecord(int i) {
        Record record = null;

        if (i >= 0 && i < m_record_keys.size()) {
            final String key = m_record_keys.get(i);

            Cursor cur = m_database.query(RECORDS_TABLE, new String[]{"id", "public_name"}, "id='"+ key + "'", null, null, null, null);

            if (cur.getCount() == 1) {
                cur.moveToFirst();
                record = new Record();
                record.setId(UUID.fromString(cur.getString(0)));
                record.setPublicName(cur.getString(1));

                Cursor items_cur = m_database.query(ITEMS_TABLE, new String[]{"key_name", "value"}, "record_id='"+ key + "'", null, null, null, null);

                if (items_cur.moveToFirst()) {
                    do {
                        record.addItem(items_cur.getString(0), items_cur.getString(1));
                    } while (items_cur.moveToNext());
                }
                items_cur.close();
            }
            cur.close();
        }
        return record;
    }

    @Override
    public int getTemplatesCount() {
        return m_template_keys.size();
    }

    @Override
    public Record getTemplate(int i) {
        Record template = null;

        if (i >= 0 && i < m_template_keys.size()) {
            final String key = m_template_keys.get(i);

            Cursor cur = m_database.query(TEMPLATES_TABLE, new String[]{"id", "public_name"}, "id='"+ key + "'", null, null, null, null);

            if (cur.getCount() == 1) {
                cur.moveToFirst();
                template = new Record();
                template.setId(UUID.fromString(cur.getString(0)));
                template.setPublicName(cur.getString(1));

                Cursor items_cur = m_database.query(TEMPLATE_ITEMS_TABLE, new String[]{"key_name"}, "record_id='"+ key + "'", null, null, null, null);

                if (items_cur.moveToFirst()) {
                    do {
                        template.addItem(items_cur.getString(0), "");
                    } while (items_cur.moveToNext());
                }
                items_cur.close();
            }
            cur.close();
        }
        return template;
    }

    @Override
    public void clear() {
        try {
            m_database.beginTransaction();
            m_database.delete(ITEMS_TABLE, null, null);
            m_database.delete(RECORDS_TABLE, null, null);
            m_database.delete(TEMPLATES_TABLE, null, null);
            m_database.setTransactionSuccessful();
        } finally {
            m_database.endTransaction();
        }
    }

    private void setParamValue(String key, String value) {
        final ContentValues values = new ContentValues();
        values.put(PARAMS_VALUE, value);

        if (checkIfExists(PARAMS_TABLE, PARAMS_KEY, key)) {
            m_database.update(PARAMS_TABLE, values, PARAMS_KEY + "= '" + key + "'", null);
        }
        else {
            values.put(PARAMS_KEY, key);
            m_database.insert(PARAMS_TABLE, null, values);
        }
    }

    private String getParamValue(String key) {
        Cursor res = m_database.query(PARAMS_TABLE, new String[]{PARAMS_VALUE}, PARAMS_KEY + " = '" + key + "'", null, null, null, null);

        String value = "";

        if (res.getCount() == 1)
        {
            res.moveToFirst();
            value = res.getString(0);
        }

        res.close();

        return value;
    }

    private boolean checkIfExists(String table, String field, String value) {

        Cursor res = m_database.query(table, null, field + "='" + value + "'", null, null, null, null);

        final boolean exists = res.getCount() > 0;
        res.close();

        return exists;
    }

    final private SQLiteDatabase m_database;

    final private String PARAM_PASSWORD_HASH = "password_hash";

    final private String PARAMS_TABLE   = "params";
    final private String PARAMS_KEY     = "key";
    final private String PARAMS_VALUE   = "value";

    final private String RECORDS_TABLE  = "records";
    final private String ITEMS_TABLE    = "items";

    final private String TEMPLATES_TABLE        = "templates";
    final private String TEMPLATE_ITEMS_TABLE   = "template_items";

    final private ArrayList<String> m_record_keys = new ArrayList<String>();
    final private ArrayList<String> m_template_keys = new ArrayList<String>();
}
