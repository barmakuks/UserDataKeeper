package com.barma.udk.core.interfaces;

import com.barma.udk.core.Record;

/**
 * Created by Vitalii Misiura on 11/13/14.
 * Interface of templates list.
 */
public interface ITemplatesKeeper {

    /** Get number of templates in the list
     * */
    public int getRecordCount();

    /** Get public name of the template that specified by index
     * */
    public String getTemplateName(int index);

    /** Get template from the list by index
     * */
    public Record getTemplate(int index);

    /** Append a new template into the list
     * */
    public boolean addTemplate(final Record record);

    /** Clear templates list
     * */
    void clear();

    /** Load templates list from storage
     * */
    void loadFromStorage(IStorage storage);
}
