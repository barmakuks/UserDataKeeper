package com.barma.udk.core.interfaces;

import com.barma.udk.core.Record;

/**
 * Created by Vitalii Misiura on 11/7/14.
 */
public interface IStorage {
    void saveBegin();
    void saveRecord(Record record);
    void removeRecord(Record record);
    void saveTemplate(Record template);
    void removeTemplate(Record template);
    void saveEnd();

    boolean load();

    boolean checkIntegrity(ICipher cipher);
    boolean isPasswordSet();
    void setupPassword(String passwordHash);

    int getRecordCount();
    Record getRecord(int i);

    int getTemplatesCount();
    Record getTemplate(int i);

    void clear();
}
