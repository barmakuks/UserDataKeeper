package com.barma.udk.gui.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.barma.udk.core.interfaces.ITemplatesKeeper;

/**
 * Created by vitalii on 11/12/14.
 */
public class TemplatesListAdapter extends BaseAdapter {

    public TemplatesListAdapter(Context context, ITemplatesKeeper templatesKeeper) {
        super();
        m_context = context;
        m_templatesKeeper = templatesKeeper;
    }

    @Override
    public int getCount() {
        return m_templatesKeeper.getRecordCount();
    }

    @Override
    public Object getItem(int position) {
        return m_templatesKeeper.getTemplate(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView (m_context);//TextView) rowView.findViewById(R.id.public_name);
        textView.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Widget_Button);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(0xffffffff);
        textView.setPadding(20,20,20,20);
        textView.setText(m_templatesKeeper.getTemplateName(position));

        return textView;
    }

    private Context m_context = null;
    private ITemplatesKeeper m_templatesKeeper = null;

}
