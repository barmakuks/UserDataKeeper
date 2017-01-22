package com.barma.udk.gui.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Provides data for list of system directories
 * Created by vitalii on 11/17/14.
 */
public class DirectoriesListAdapter extends BaseAdapter {

    public DirectoriesListAdapter(Context context){
        m_context = context;
    }

    @Override
    public int getCount() {
        return m_directories.length;
    }

    @Override
    public Object getItem(int position) {
        return Environment.getExternalStoragePublicDirectory(m_directories[position]).getPath();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LinearLayout view = new LinearLayout(m_context);
        view.setOrientation(LinearLayout.VERTICAL);

        final TextView txtKey = new TextView(m_context);
        txtKey.setText(m_directories[position]);
        txtKey.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Medium);

        final TextView txtValue = new TextView(m_context);
        txtValue.setText(Environment.getExternalStoragePublicDirectory(m_directories[position]).getPath());
        txtValue.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Small);

        view.addView(txtKey);
        view.addView(txtValue);

        return view;
    }

    private final Context m_context;
    private final String m_directories[] = {
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_PICTURES
    };

}
