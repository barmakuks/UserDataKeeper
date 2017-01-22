package com.barma.udk.gui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.barma.udk.R;
import com.barma.udk.core.Engine;
import com.barma.udk.core.Record;
import com.barma.udk.core.interfaces.ITemplatesKeeper;
import com.barma.udk.gui.adapters.RecordItemsListAdapter;

/**
 * Record view activity
 * Created by Vitalii Misiura on 11/10/14.
 */
public class RecordViewActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_details_view);

        final ListView listView = (ListView) findViewById(R.id.items_view);

        try {
            m_engine = Engine.GetInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_items_adapter = new RecordItemsListAdapter(this, m_engine.getRecord(m_engine.getSelectedIndex()), m_removeItemClickListener);
        listView.setAdapter(m_items_adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editItemUI(position);
            }
        });
        
        final TextView textPublicName = (TextView) findViewById(R.id.public_name);

        textPublicName.setClickable(true);
        textPublicName.setFocusable(true);

        textPublicName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPublicNameUI();
            }
        });


        Button btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setVisibility(View.GONE);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRemoveMode(false);
            }
        });

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        invalidatePublicNameView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.record_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.add_item: {
                setRemoveMode(false);
                addItemUI();
                return true;
            }
            case R.id.remove_items: {
                setRemoveMode(true);
                return true;
            }
            case R.id.save_as_template: {
                setRemoveMode(false);
                saveAsTemplateUI();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void editItemUI(final int position) {
        final Record current_record = m_engine.getRecord(m_engine.getSelectedIndex());

        Record.Item item = current_record.getItem(position);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_item);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.record_item_edit, null);

        final EditText txtKey = (EditText) view.findViewById(R.id.editKey);
        final EditText txtValue = (EditText) view.findViewById(R.id.editValue);

        txtKey.setText(item.key);
        txtValue.setText(item.value);

        alert.setView(view);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                current_record.setItem(position, txtKey.getText().toString(), txtValue.getText().toString());
                m_engine.replaceRecord(m_engine.getSelectedIndex(), current_record);
                invalidateItemsListView();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();

    }

    private void saveAsTemplateUI() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_template_name);

        final Record current_record = m_engine.getRecord(m_engine.getSelectedIndex());

        final EditText editor = new EditText(this);
        editor.setText(current_record.getPublicName());
        alert.setView(editor);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ITemplatesKeeper templates = m_engine.getTemplatesKeeper();
                current_record.setPublicName(editor.getText().toString());
                templates.addTemplate(current_record);
                invalidatePublicNameView();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void setRemoveMode(boolean switch_on) {
        final ListView listView = (ListView) findViewById(R.id.items_view);
        if (listView != null) {
            RecordItemsListAdapter adapter = (RecordItemsListAdapter) listView.getAdapter();
            adapter.setMode(switch_on ? RecordItemsListAdapter.Mode.Remove : RecordItemsListAdapter.Mode.View);
            listView.invalidateViews();

            Button btnDone = (Button) findViewById(R.id.btnDone);
            btnDone.setVisibility(switch_on ? View.VISIBLE : View.GONE);
        }
    }

    private void editPublicNameUI() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_public_name);

        final EditText editor = new EditText(this);
        editor.setText(m_engine.getRecordName(m_engine.getSelectedIndex()));
        alert.setView(editor);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final int index = m_engine.getSelectedIndex();
                Record current_record = m_engine.getRecord(index);
                current_record.setPublicName(editor.getText().toString());
                m_engine.replaceRecord(index, current_record);
                invalidatePublicNameView();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void promptRemoveItemUI(final int position) {

        final Record current_record = m_engine.getRecord(m_engine.getSelectedIndex());
        Record.Item item = current_record.getItem(position);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String format = getResources().getString(R.string.remove_confirm_format);
        builder.setTitle(String.format(format, item.key));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                current_record.removeItem(position);
                m_engine.replaceRecord(m_engine.getSelectedIndex(), current_record);

                invalidateItemsListView();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void invalidatePublicNameView() {
        final TextView textPublicName = (TextView) findViewById(R.id.public_name);
        final Record record = m_engine.getRecord(m_engine.getSelectedIndex());
        m_items_adapter.setRecord(record);
        textPublicName.setText(record.getPublicName());
    }

    private void invalidateItemsListView() {
        final ListView listView = (ListView) findViewById(R.id.items_view);
        if (listView != null) {
            final Record record = m_engine.getRecord(m_engine.getSelectedIndex());
            m_items_adapter.setRecord(record);
            listView.invalidateViews();
        }
    }

    private void addItemUI() {
        final Record current_record = m_engine.getRecord(m_engine.getSelectedIndex());

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_item);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.record_item_edit, null);

        final EditText txtKey = (EditText) view.findViewById(R.id.editKey);
        final EditText txtValue = (EditText) view.findViewById(R.id.editValue);

        txtKey.setText("");
        txtValue.setText("");

        alert.setView(view);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                current_record.addItem(txtKey.getText().toString(), txtValue.getText().toString());
                m_engine.replaceRecord(m_engine.getSelectedIndex(), current_record);
                invalidateItemsListView();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private Engine m_engine = null;

    private View.OnClickListener    m_removeItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = findItemByKey((String) v.getTag());
            if (index >= 0) {
                promptRemoveItemUI(index);
            }
        }
    };

    private int findItemByKey(final String key) {
        Record record = m_engine.getRecord(m_engine.getSelectedIndex());

        for (int i = 0 ; i < record.getItemsCount(); ++i) {
            if (record.getItem(i).key.equals(key)) {
                return i;
            }
        }

        return -1;
    }

    private RecordItemsListAdapter m_items_adapter = null;
}