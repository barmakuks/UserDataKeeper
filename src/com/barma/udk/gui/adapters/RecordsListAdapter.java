package com.barma.udk.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.barma.udk.R;
import com.barma.udk.core.interfaces.IDataKeeper;

/**
 * Created by Vitalii Misiura on 11/7/14.
 */
public class RecordsListAdapter extends BaseAdapter {

    public RecordsListAdapter(Context context, IDataKeeper dataKeeper, View.OnClickListener removeButtonListener){
        super();
        m_context = context;
        m_dataKeeper = dataKeeper;
        m_removeButtonListener = removeButtonListener;
    }

    public enum Mode{View, Remove}

    public void setMode(Mode mode) {
        m_mode = mode;
    }

    @Override
    public int getCount() {
        return m_dataKeeper.getRecordCount();
    }

    @Override
    public Object getItem(int position) {
        return m_dataKeeper.getRecord(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if (m_mode == Mode.View) {
            rowView = inflater.inflate(R.layout.record_view_mode, parent, false);
        }
        else {
            rowView = inflater.inflate(R.layout.record_remove_mode, parent, false);
            ImageButton btnRemove = (ImageButton) rowView.findViewById(R.id.btnRemoveRecord);
            btnRemove.setTag(m_dataKeeper.getRecord(position).getId());
            btnRemove.setOnClickListener(m_removeButtonListener);
        }

        TextView textView = (TextView) rowView.findViewById(R.id.public_name);
        textView.setText(m_dataKeeper.getRecordName(position));

        return rowView;
    }

    private final IDataKeeper     m_dataKeeper;
    private final Context   m_context;
    private Mode            m_mode = Mode.View;
    private final View.OnClickListener m_removeButtonListener;
}
