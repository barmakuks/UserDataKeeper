package com.barma.udk.core.interfaces;

import com.barma.udk.core.Record;

import java.util.UUID;

/**
 * Created by vitalii on 11/13/14.
 */
public interface IDataKeeper {

    int getRecordCount();

    String getRecordName(int index);
    Record getRecord(int index);
    Record getRecord(UUID recordId);

    void addRecord(Record record);
    void clearRecordList();
    void replaceRecord(int index, Record record);
    void removeRecord(UUID recordId);

    int getSelectedIndex();
    void setSelectedIndex(int index);

    void applyRecordListFilter(String filter);
}
