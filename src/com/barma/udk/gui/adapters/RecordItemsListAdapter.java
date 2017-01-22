package com.barma.udk.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.barma.udk.R;
import com.barma.udk.core.Record;

/** Provides views for progress records items list
 * Created by vitalii on 11/10/14.
 */
public class RecordItemsListAdapter extends BaseAdapter {

    public RecordItemsListAdapter(Context context, Record record, View.OnClickListener removeButtonListener) {
        m_context = context;
        m_record = record;
        m_removeButtonListener = removeButtonListener;
    }

    public void setRecord(Record record) {
        m_record = record;
    }

    @Override
    public int getCount() {
        if (m_record != null)
        {
            return  m_record.getItemsCount();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.record_item_view, parent, false);

        TextView textKey = (TextView) rowView.findViewById(R.id.txtKey);
        TextView textValue  = (TextView) rowView.findViewById(R.id.txtValue);

//        final Record current = m_dataKeeper.getRecord(m_dataKeeper.getSelectedIndex());

        textKey.setText(m_record.getItem(position).key);
        textValue.setText(m_record.getItem(position).value);

        ImageButton btnRemove = (ImageButton) rowView.findViewById(R.id.btnRemoveItem);
        btnRemove.setVisibility(m_mode == Mode.View? View.GONE : View.VISIBLE);
        btnRemove.setTag(m_record.getItem(position).key);
        btnRemove.setOnClickListener(m_removeButtonListener);

        return rowView;
    }

    public enum Mode{View, Remove}

    public void setMode(Mode mode) {
        m_mode = mode;
    }

    private Context     m_context = null;
    private Mode m_mode = Mode.View;
    private Record m_record;
    private final View.OnClickListener m_removeButtonListener;

}
