package com.barma.udk.core;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Vitalii Misiura on 11/7/14.
 */
public class Record {

    public class Item
    {
        public Item(String k, String v) {
            key = k;
            value = v;
        }
        public String key;
        public String value;
    }

    public UUID getId() {
        if (m_id == null)
        {
            m_id = UUID.randomUUID();
        }
        return m_id;
    }

    public void setId(final UUID id) {m_id = id;}

    public String getPublicName() { return m_publicName; }

    public void setPublicName(String publicName) { m_publicName = publicName; }

    public int getItemsCount() { return m_items.size(); }

    public void addItem(String key, String value) {
        m_items.add(new Item(key, value));
    }

    public Item getItem(int index){
        return m_items.get(index);
    }

    public void setItem(int index, String key, String value) {
        Item item = new Item(key, value);
        m_items.remove(index);
        m_items.add(index, item);
    }

    public void removeItem(int index) {
        m_items.remove(index);
    }

    public void clearItems() { m_items.clear(); }

    //************************************* private members ***********************************************************
    private ArrayList<Item> m_items = new ArrayList<Item>();

    private String  m_publicName = null;

    private UUID    m_id = null;
}
