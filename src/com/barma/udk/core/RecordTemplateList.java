package com.barma.udk.core;

import com.barma.udk.core.interfaces.IStorage;
import com.barma.udk.core.interfaces.ITemplatesKeeper;

import java.util.ArrayList;

/**
 * Created by Vitalii Misiura on 11/12/14.
 * Implementation of ITemplatesKeeper interface
 */
public class RecordTemplateList implements ITemplatesKeeper {

    @Override
    public int getRecordCount() { return m_templates.size(); }

    @Override
    public String getTemplateName(int index)
    {
        return m_templates.get(index).getPublicName();
    }

    @Override
    public Record getTemplate(int index)
    {
        return m_templates.get(index);
    }

    @Override
    public boolean addTemplate(final Record record) {
        for (Record template : m_templates)
        {
            if (Equals(template, record))
            {
                return false;
            }
        }

        Record template = new Record();
        template.setPublicName(record.getPublicName());

        for (int i = 0; i < record.getItemsCount(); ++i)
        {
            template.addItem(record.getItem(i).key, "");
        }

        m_templates.add(template);

        if (m_storage != null) {
            m_storage.saveTemplate(template);
        }

        return true;
    }

    @Override
    public void clear() {
        m_templates.clear();
    }

    @Override
    public void loadFromStorage(IStorage storage) {
        m_templates.clear();
        m_storage = storage;

        if (m_storage != null) {
            final int templatesCount = m_storage.getTemplatesCount();

            for (int i = 0; i < templatesCount; ++i) {
                Record record = m_storage.getTemplate(i);
                m_templates.add(record);
            }
        }
    }

    private boolean Equals(Record first, Record second) {

        final int items_count = first.getItemsCount();

        if (items_count != second.getItemsCount())
        {
            return false;
        }

        int same_count = 0;

        for (int i = 0; i < items_count; ++i)
        {
            for (int j = 0; j < items_count; ++j)
            {
                if (first.getItem(i).key.equals(second.getItem(j).key))
                {
                    same_count++;
                }
            }
        }

        return same_count == items_count;
    }

    private ArrayList<Record>   m_templates = new ArrayList<Record>();
    private IStorage            m_storage = null;
}
