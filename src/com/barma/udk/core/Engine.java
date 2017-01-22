package com.barma.udk.core;

import android.util.Log;
import com.barma.udk.core.interfaces.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Vitalii Misiura on 11/6/14.
 */
public class Engine implements IDataKeeper
{
    public class OperationProgress{
        public int minimum;
        public int maximum;
        public int progress;
    }
    static private Engine instance = null;

    static public void init(IConfigurator configurator) throws Exception {

        if (configurator == null) {
            throw new Exception("configurator should not be null");
        }
        if (instance != null) {
            instance.setConfigurator(configurator);
        }
        else {
            instance = new Engine(configurator);
        }
    }

    private void setConfigurator(IConfigurator configurator) {
        m_configurator = configurator;
    }

    static public Engine GetInstance() throws Exception {

        if (instance == null)
        {
            throw new Exception("method init() should be called before");
        }

        return instance;
    }

    public void setIdleTimerListener(OnIdleTimerListener listener) {
        if (m_idleTimerListener != listener) {
            m_idleTimerListener = listener;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("Engine", "finalize");
        instance = null;
        super.finalize();
    }

    //********************************************** IDataKeeper implementation ****************************************
    @Override
    public int getRecordCount() {

        if (m_filteredItems != null) {
            return m_filteredItems.size();
        }

        return m_items.size();
    }

    @Override
    public String getRecordName(int index) {

        resetIdleTime();

        if (m_filteredItems != null) {
            index = m_filteredItems.get(index);
        }

        return m_items.get(index).getPublicName();
    }

    @Override
    public Record getRecord(int index) {

        resetIdleTime();

        if (m_filteredItems != null) {
            index = m_filteredItems.get(index);
        }

        if (index >= 0 && index < m_items.size())
        {
            Record record = m_items.get(index);

            return applyItemOperation(record, new DecryptOperation(m_cipher));
        }
        return null;
    }

    @Override
    public Record getRecord(UUID recordId) {
        resetIdleTime();

        for (Record record: m_items) {
            if (record.getId().equals(recordId)) {
                return record;
            }
        }
        return null;
    }

    @Override
    public void addRecord(Record record) {
        resetIdleTime();

        Record encrypted_record = applyItemOperation(record, new EncryptOperation(m_cipher));
        m_items.add(encrypted_record);

        m_filteredItems = null;


        if (m_storage != null) {
            m_storage.saveRecord(encrypted_record);
        }
    }

    @Override
    public void clearRecordList() {
        m_items.clear();

        if (m_filteredItems != null) {
            m_filteredItems.clear();
        }
    }

    @Override
    public void replaceRecord(int index, Record record) {

        resetIdleTime();

        if (m_filteredItems != null) {
            index = m_filteredItems.get(index);
        }

        Record encrypted_record = applyItemOperation(record, new EncryptOperation(m_cipher));
        m_items.remove(index);
        m_items.add(index, encrypted_record);

        if (m_storage != null) {
            m_storage.saveRecord(encrypted_record);
        }
    }

    @Override
    public void removeRecord(UUID recordId) {
        resetIdleTime();

        Record record = null;

        int index = -1;
        for (int i = 0; i < m_items.size(); ++i){
            if (m_items.get(i).getId().equals(recordId)){
                record = m_items.get(i);
                m_items.remove(i);
                index = i;
                break;
            }
        }

        if (m_filteredItems != null && index >= 0) {

            for (int i = 0; i < m_filteredItems.size(); ++i) {

                if (m_filteredItems.get(i) == index) {
                    m_filteredItems.remove(i);
                    break;
                }
            }
        }

        if (m_storage != null && record != null) {
            m_storage.removeRecord(record);
        }
    }

    @Override
    public int getSelectedIndex() {
        return m_selectedIndex;
    }

    @Override
    public void setSelectedIndex(int index) {
        m_selectedIndex = index;
    }

    @Override
    public void applyRecordListFilter(String filter) {
        resetIdleTime();

        if (filter == null || filter.isEmpty()) {
            m_filteredItems = null;
        }
        else {
            if (m_filteredItems == null) {
                m_filteredItems = new ArrayList<Integer>();
            }

            m_filteredItems.clear();

            final String filter_lower = filter.toLowerCase();

            for (int i = 0; i < m_items.size(); ++i) {
                if (m_items.get(i).getPublicName().toLowerCase().contains(filter_lower)) {
                    m_filteredItems.add(i);
                }
            }
        }

        m_selectedIndex = -1;
    }

    //********************************************* ITemplatesKeeper ***************************************************
    public ITemplatesKeeper getTemplatesKeeper() {
        return m_templates;
    }

    //******************************************** Serialization *******************************************************
    public void exportTo(IStorage storage, ILongActionCallback callback) {

        final OperationProgress progress = new OperationProgress();
        progress.minimum = 0;
        progress.maximum = m_items.size() + m_templates.getRecordCount();
        progress.progress = 0;

        if (callback != null) {
            callback.onActionBegin(progress);
        }

        storage.saveBegin();

        storage.setupPassword(m_cipher.getPasswordHash());

        if (callback != null) {
            callback.onActionStep(progress);
        }

        for (Record record: m_items)
        {
            if (callback != null) {
                progress.progress++;
                callback.onActionStep(progress);
            }
            storage.saveRecord(record);
        }

        for (int i = 0; i < m_templates.getRecordCount(); ++i)
        {
            if (callback != null) {
                progress.progress++;
                callback.onActionStep(progress);
            }
            storage.saveTemplate(m_templates.getTemplate(i));
        }

        storage.saveEnd();

        if (callback != null) {
            callback.onActionDone(progress);
        }
    }

    public boolean deserializeFrom(IStorage storage, ICipher cipher) {

        if (storage == null) {
            return false;
        }

        if (storage.load() && storage.checkIntegrity(cipher))
        {
            clearRecordList();

            m_cipher = cipher;
            m_storage = storage;

            m_templates.clear();
            m_selectedIndex = 0;

            final int recordCount = m_storage.getRecordCount();

            for (int i = 0; i < recordCount; ++i) {
                Record record = m_storage.getRecord(i);
                m_items.add(record);
            }

            if (m_templates != null) {
                m_templates.loadFromStorage(storage);
            }
            return true;
        }

        return false;
    }

    public boolean importFrom(IStorage storage, ICipher cipher, boolean clearRecords, ILongActionCallback callback) {

        if (storage.load() && storage.checkIntegrity(cipher))
        {
            final OperationProgress progress = new OperationProgress();
            progress.minimum = 0;
            progress.maximum = storage.getRecordCount() + storage.getTemplatesCount() + 1;
            progress.progress = 0;

            if (callback != null) {
                callback.onActionBegin(progress);
            }

            if (clearRecords) {
                m_storage.clear();
                clearRecordList();
                m_templates.clear();
                m_selectedIndex = 0;
            }

            if (callback != null) {
                progress.progress++;
                callback.onActionStep(progress);
            }

            final int recordCount = storage.getRecordCount();

            for (int i = 0; i < recordCount; ++i) {
                Record record = storage.getRecord(i);

                if (cipher != null) {
                    record = applyItemOperation(record, new DecryptOperation(cipher));
                }

                addRecord(record);

                if (callback != null) {
                    progress.progress++;
                    callback.onActionStep(progress);
                }
            }

            for (int i = 0; i < storage.getTemplatesCount(); ++i) {
                final Record record = storage.getTemplate(i);

                m_templates.addTemplate(record);

                if (callback != null) {
                    progress.progress++;
                    callback.onActionStep(progress);
                }
            }

            if (callback != null) {
                callback.onActionDone(progress);
            }

            return true;
        }

        return false;
    }

    public boolean setCipher(ICipher cipher, ILongActionCallback callback){

        if (m_cipher == cipher || cipher == null) {
            return false;
        }

        if (m_storage != null) {
            m_storage.setupPassword(cipher.getPasswordHash());
        }

        EncryptOperation encryptOperation = new EncryptOperation(cipher);
        DecryptOperation decryptOperation = new DecryptOperation(m_cipher);

        final OperationProgress progress = new OperationProgress();
        progress.minimum = 0;
        progress.maximum = m_items.size();
        progress.progress = 0;

        if (callback != null) {
            callback.onActionBegin(progress);
        }

        for (int i = 0; i < m_items.size(); ++i)
        {
            Record record = m_items.get(i);

            if (m_cipher != null) {
                record = applyItemOperation(record, decryptOperation);
            }

            record = applyItemOperation(record, encryptOperation);

            m_items.remove(i);
            m_items.add(i, record);

            if (m_storage != null) {
                m_storage.saveRecord(record);
            }

            if (callback != null) {
                progress.progress++;
                callback.onActionStep(progress);
            }
        }

        m_cipher = cipher;

        if (callback != null) {
            callback.onActionDone(progress);
        }

        return true;
    }

    //********************************************* Password routine ***************************************************
    public boolean changePassword(final String current_password, final String new_password, ILongActionCallback callback) {
        final Engine engine = new Engine(m_configurator);

        final IStorage storage_src = m_configurator.createDefaultStorage();
        final ICipher cipher_src = m_configurator.createDefaultCipher(current_password);

        return engine.deserializeFrom(storage_src, cipher_src) && setCipher(m_configurator.createDefaultCipher(new_password), callback);
    }

    public void setupNewPassword(String password) {
        if (m_storage == null) {
            m_storage = m_configurator.createDefaultStorage();
        }

        final ICipher cipher = m_configurator.createDefaultCipher(password);

        m_storage.setupPassword(cipher.getPasswordHash());
        deserializeFrom(m_storage, cipher);
    }

    public boolean checkPasswordAndLoad(String password) {
        ICipher cipher = m_configurator.createDefaultCipher(password);
        IStorage storage = m_configurator.createDefaultStorage();

        return deserializeFrom(storage, cipher);
    }

    public boolean passwordExists(){
        IStorage storage = m_storage;

        if (storage == null){
            storage = m_configurator.createDefaultStorage();
        }

        return storage.isPasswordSet();
    }

    //******************************************** private functions ***************************************************
    private class DecryptOperation implements ItemOperation
    {
        public DecryptOperation(ICipher cipher) {
            m_decryptor = cipher;
        }
        @Override
        public String Do(String value) {
            if (m_decryptor != null) {
                return m_decryptor.decrypt(value);
            }
            return value;
        }

        private final ICipher m_decryptor;
    }

    private class EncryptOperation implements ItemOperation
    {
        public EncryptOperation(ICipher cipher) {
            m_encryptor = cipher;
        }
        @Override
        public String Do(String value) {
            if (m_encryptor != null) {
                return m_encryptor.encrypt(value);
            }

            return  value;
        }

        private final ICipher m_encryptor;
    }

    private Record applyItemOperation(Record record, ItemOperation itemOperation) {
        Record result = new Record();

        result.setId(record.getId());
        result.setPublicName(record.getPublicName());

        for (int i = 0; i < record.getItemsCount(); ++i)
        {
            result.addItem(itemOperation.Do(record.getItem(i).key), itemOperation.Do(record.getItem(i).value));
        }

        return result;

    }

    //******************************************* on Timer routine *****************************************************

    private class OnTimer extends TimerTask {
        OnTimer (Engine engine)
        {
            m_engine = engine;
        }
        @Override
        public void run() {
            m_engine.onTimer();
        }

        private Engine m_engine;
    }

    private void onTimer() {
        if (m_idleTime < MaxIdleTime) {
            m_idleTime += TimerStep;
            if (m_idleTimerListener != null) {
                m_idleTimerListener.onTimer(MaxIdleTime - m_idleTime);
            }
        }
        else {
            if (m_idleTimerListener != null) {
                m_idleTimerListener.onTimeOver();
            }
        }
    }

    private void resetIdleTime() {
        m_idleTime = 0;
        if (m_idleTimerListener != null) {
            m_idleTimerListener.onTimerStart(MaxIdleTime);
        }
    }

    public void startIdleTimer() {
        if (m_idleTimer == null) {
            m_idleTimer = new Timer("com.barma.udk.idle_timer", false);
            m_idleTimer.schedule(new OnTimer(this), TimerStep, TimerStep);
        }
        resetIdleTime();
    }

    public void stopIdleTimer() {
        if (m_idleTimer != null) {
            m_idleTimer.cancel();
            m_idleTimer = null;
            m_idleTimerListener = null;
        }
    }

    //******************************************** private members *****************************************************

    private OnIdleTimerListener m_idleTimerListener = null;
    private Timer m_idleTimer = null;
    private final int TimerStep = 5000;
    private final int MaxIdleTime = 60000;
    private int m_idleTime = 0;

    private Engine(IConfigurator configurator) {
        m_configurator = configurator;
    }

    private ICipher m_cipher = null;

    private ArrayList <Record> m_items = new ArrayList<Record>();

    private ArrayList <Integer> m_filteredItems = null;

    private int m_selectedIndex = -1;

    private RecordTemplateList m_templates = new RecordTemplateList();

    private IStorage m_storage = null;

    private IConfigurator m_configurator;

}
