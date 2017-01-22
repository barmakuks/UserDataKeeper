package com.barma.udk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.barma.udk.R;
import com.barma.udk.core.*;
import com.barma.udk.core.interfaces.*;
import com.barma.udk.export_import.XmlExportStorage;
import com.barma.udk.export_import.XmlImportStorage;
import com.barma.udk.gui.adapters.DirectoriesListAdapter;
import com.barma.udk.gui.adapters.RecordsListAdapter;
import com.barma.udk.gui.adapters.TemplatesListAdapter;

import java.util.UUID;

/**
 * Main activity
 * Created by Vitalii Misiura on 11/6/14.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        OnIdleTimerListener
{
    public MainActivity() throws Exception {
        super();
        Log.d("Activity", "c-tor");
        Engine.init(new Configurator(this));
        m_engine = Engine.GetInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Activity", "onCreate");
        super.onCreate(savedInstanceState);
        checkPasswordExists();
        m_engine.setIdleTimerListener(this);
        m_engine.startIdleTimer();
    }

    @Override
    protected void onRestart() {
        Log.d("Activity", "onRestart");
        super.onRestart();

        m_mainListView = (ListView) findViewById(R.id.recordsView);
        if (m_mainListView != null) {
            m_mainListView.invalidateViews();
        }
        invalidateOptionsMenu();
        m_engine.startIdleTimer();
    }

    @Override
    protected void onResume() {
        Log.d("Activity", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("Activity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.d("Activity", "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("Activity", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("Activity", "onDestroy");
        if (m_engine != null) {
            m_engine.stopIdleTimer();
            m_engine = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_record: {
                setRemoveMode(false);
                createRecordFromTemplate(-1);
                break;
            }
            case R.id.add_record_from_template: {
                setRemoveMode(false);
                if (m_engine.getTemplatesKeeper().getRecordCount() > 0) {
                    createRecordFromTemplateUI();
                }
                break;
            }
            case R.id.remove_record: {
                setRemoveMode(true);
                break;
            }
            case R.id.change_password: {
                setRemoveMode(false);
                changePasswordUI();
                break;
            }
            case R.id.export_to_file: {
                setRemoveMode(false);
                exportDataListUI();
                break;
            }
            case R.id.import_from_file: {
                importDataFromFileUI();
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem item_from_template = menu.findItem(R.id.add_record_from_template);
        if (item_from_template != null) {
            item_from_template.setEnabled(m_engine.getTemplatesKeeper().getRecordCount() > 0);
        }

        final MenuItem item_remove = menu.findItem(R.id.remove_record);
        if (item_remove != null) {
            item_remove.setEnabled(m_engine.getRecordCount() > 0);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startEditRecordActivity(position);
    }

    /*********************************** SearchView.OnQueryTextListener *******************/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        m_engine.applyRecordListFilter(newText);

        if (m_mainListView != null) {
            m_mainListView.invalidateViews();
        }

        return true;
    }

    // ********************** Password routine ****************************************

    /** This function checks if application password is set.
     * If password is already set prompts user to input valid application password to continue,
     * otherwise prompts user to setup new password
     * */
    private void checkPasswordExists() {
        boolean isPasswordExists = m_engine.passwordExists();

        if (isPasswordExists) {
            inputPasswordUI();
        }
        else {
            inputNewPasswordUI();
        }
    }

    private void setupNewPassword(final String password) {
        m_engine.setupNewPassword(password);
    }

    private boolean checkPassword(String password) {
        return m_engine.checkPasswordAndLoad(password);
    }

    /** Input application password to enter. It is allowed to enter wrong password not more then two times.
     * */
    private void inputPasswordUI() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.password);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText password_edit = new EditText(this);
        password_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        password_edit.setLayoutParams(layoutParams);
        password_edit.setEms(10);
        layout.setPadding(40,20,40,20);
        layout.addView(password_edit);
        alert.setView(layout);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (checkPassword(password_edit.getText().toString())) {
                    loadUI();
                }
                else {
                    if (--m_passwordAttempts > 0) {
                        dialog.cancel();
                        inputPasswordUI();
                    }
                    else {
                        finish();
                    }
                }
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        alert.setCancelable(false);

        alert.show();
    }

    /** Input new password for application.
     * */
    private void inputNewPasswordUI() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.setup_password);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 0, 40, 0);

        final TextView text = new TextView(this);
        text.setText(R.string.enter_first_password);
        layout.addView(text);

        final LinearLayout.LayoutParams passwordLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText password_edit = new EditText(this);
        password_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password_edit.setLayoutParams(passwordLayoutParams);
        password_edit.setEms(10);
        layout.addView(password_edit);

        final TextView confirm_text = new TextView(this);
        confirm_text.setText(R.string.confirm_password);
        layout.addView(confirm_text);

        final EditText password_copy_edit = new EditText(this);
        password_copy_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password_copy_edit.setLayoutParams(passwordLayoutParams);
        password_copy_edit.setEms(10);

        layout.addView(password_copy_edit);

        alert.setView(layout);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String password = password_edit.getText().toString();
                final String password_copy = password_copy_edit.getText().toString();

                if (password.equals(password_copy)){
                    if (password.length() < 6 ) {
                        repeatInputNewPasswordUI(R.string.password_length_message);
                    }
                    else {
                        setupNewPassword(password);
                        loadUI();
                    }
                }
                else {
                    repeatInputNewPasswordUI(R.string.password_mismatch);
                }
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        alert.setCancelable(false);

        alert.show();
    }


    /** Show error message and prompts to input new application password one more time
     * */
    private void repeatInputNewPasswordUI(int messageId) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.password);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final TextView message_text = new TextView(this);
        message_text.setText(messageId);
        layout.setPadding(40,20,40,20);
        layout.addView(message_text);
        alert.setView(layout);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputNewPasswordUI();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        alert.show();
    }

    private void loadUI() {
        setContentView(R.layout.main);

        Button btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setVisibility(View.GONE);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRemoveMode(false);
            }
        });

        m_mainListView = (ListView) findViewById(R.id.recordsView);
        m_mainListView.setAdapter(new RecordsListAdapter(this, m_engine, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final UUID record_id = (UUID) v.getTag();
                if (record_id != null) {
                    confirmRecordRemoving(record_id);
                }
            }
        }));
        m_mainListView.setOnItemClickListener(this);
        invalidateOptionsMenu();
    }

    private void changePasswordUI() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.change_password);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 0, 40, 0);

        final TextView text = new TextView(this);
        text.setText(R.string.enter_current_password);
        layout.addView(text);

        final LinearLayout.LayoutParams passwordLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText current_password_edit = new EditText(this);
        current_password_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        current_password_edit.setLayoutParams(passwordLayoutParams);
        current_password_edit.setEms(10);
        layout.addView(current_password_edit);

        final TextView new_text = new TextView(this);
        new_text.setText(R.string.enter_new_password);
        layout.addView(new_text);

        final EditText new_password_edit = new EditText(this);
        new_password_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new_password_edit.setLayoutParams(passwordLayoutParams);
        new_password_edit.setEms(10);
        layout.addView(new_password_edit);

        final TextView confirm_text = new TextView(this);
        confirm_text.setText(R.string.confirm_password);
        layout.addView(confirm_text);

        final EditText password_copy_edit = new EditText(this);
        password_copy_edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password_copy_edit.setLayoutParams(passwordLayoutParams);
        password_copy_edit.setEms(10);
        layout.addView(password_copy_edit);

        alert.setView(layout);

        final ILongActionCallback progress_dialog = new LongActionProgressUI(this, R.string.change_password);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String password = current_password_edit.getText().toString();
                final String new_password = new_password_edit.getText().toString();
                final String new_password_copy = password_copy_edit.getText().toString();

                if (new_password.equals(new_password_copy)){
                    if (password.length() < 6 ) {
                        showMessage(R.string.password_length_message);
                    }
                    else {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean result = m_engine.changePassword(password, new_password, progress_dialog);

                                if (result) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showMessage(R.string.password_changed);
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showMessage(R.string.password_not_changed);
                                        }
                                    });
                                }

                            }
                        }).start();
                    }
                }
                else {
                    showMessage(R.string.password_mismatch);
                }
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

    // ********************************************************************************

    private void exportDataListUI() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.export_to_file);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.export_data, null);

        final Spinner directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectories);
        directorySpinner.setAdapter(new DirectoriesListAdapter(this));

        final EditText editFilename= (EditText) view.findViewById(R.id.editFileName);
        editFilename.setText("user_data_export.xml");

        final EditText editPassword = (EditText) view.findViewById(R.id.editPassword);
        editPassword.setVisibility(EditText.GONE);

        final TextView textPassword = (TextView) view.findViewById(R.id.textPassword);
        textPassword.setVisibility(EditText.GONE);

        builder.setView(view);
        builder.setCancelable(false);

        final ILongActionCallback progress_dialog = new LongActionProgressUI(this, R.string.export_to_file);

        builder.setNeutralButton(R.string.export, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String filename = editFilename.getText().toString();

                final Spinner spinnerDir = (Spinner) view.findViewById(R.id.spinnerDirectories);
                final String dir = (String) spinnerDir.getSelectedItem();

                if (!filename.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            IStorage storage = new XmlExportStorage(dir, filename);
                            m_engine.exportTo(storage, progress_dialog);
                        }
                    }).start();
                }
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

    private void importDataFromFileUI() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.export_to_file);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.export_data, null);

        final Spinner directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectories);
        directorySpinner.setAdapter(new DirectoriesListAdapter(this));

        final EditText editFilename= (EditText) view.findViewById(R.id.editFileName);
        editFilename.setText("user_data_export.xml");

        final EditText editPassword = (EditText) view.findViewById(R.id.editPassword);

        final ILongActionCallback progress_dialog = new LongActionProgressUI(this, R.string.import_from_file);

        final CheckBox checkClearData = (CheckBox) view.findViewById(R.id.checkClearData);
        checkClearData.setVisibility(View.VISIBLE);

        builder.setView(view);
        builder.setCancelable(false);

        builder.setNeutralButton(R.string.do_import, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String filename = editFilename.getText().toString();

                final Spinner spinnerDir = (Spinner) view.findViewById(R.id.spinnerDirectories);
                final String dir = (String) spinnerDir.getSelectedItem();

                if (!filename.isEmpty()) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            XmlImportStorage storage = new XmlImportStorage(dir, filename);
                            final String password = editPassword.getText().toString();
                            AesCipher cipher = null;
                            if (!password.isEmpty()) {
                                cipher = new AesCipher(password);
                            }

                            final boolean import_result = m_engine.importFrom(storage, cipher, checkClearData.isChecked(), progress_dialog);

                            if (import_result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMessage(R.string.import_success);
                                        if (m_mainListView != null) {
                                            m_mainListView.invalidateViews();
                                        }
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMessage(R.string.import_fail);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*.xml");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//        try {
//            startActivityForResult(Intent.createChooser(intent, "Select a File to Import"),
//                    FILE_SELECT_CODE);
//        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
//        }
    }

    private void confirmRecordRemoving(final UUID recordId) {

        final Record record = m_engine.getRecord(recordId);

        if (record == null) {
            return;
        }

        final String record_name = record.getPublicName();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String format = getResources().getString(R.string.remove_confirm_format);
        final String message = String.format(format, record_name);
        builder.setTitle(message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final IDataKeeper dataKeeper = m_engine;
                dataKeeper.removeRecord(recordId);

                if (m_mainListView != null) {
                    m_mainListView.invalidateViews();
                }

                if (dataKeeper.getRecordCount() == 0) {
                    setRemoveMode(false);
                }
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

    private void setRemoveMode(boolean isSwitched) {
        if (m_mainListView != null) {
            RecordsListAdapter adapter = (RecordsListAdapter) m_mainListView.getAdapter();
            adapter.setMode(isSwitched ? RecordsListAdapter.Mode.Remove : RecordsListAdapter.Mode.View);
            m_mainListView.invalidateViews();

            Button btnDone = (Button) findViewById(R.id.btnDone);
            btnDone.setVisibility(isSwitched ? View.VISIBLE : View.GONE);
        }
    }

    private void createRecordFromTemplateUI() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_template);

        builder.setPositiveButton(R.string.empty, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createRecordFromTemplate(-1);
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();

        final ListView listView = new ListView(this);

        listView.setAdapter(new TemplatesListAdapter(this, m_engine.getTemplatesKeeper()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createRecordFromTemplate(position);
                alert.cancel();
            }
        });

        alert.setView(listView);

        alert.show();
    }

    private void createRecordFromTemplate(int templateIndex) {

        Record record = new Record();

        if (templateIndex >= 0)
        {
            ITemplatesKeeper templates = m_engine.getTemplatesKeeper();
            record.setPublicName(templates.getTemplateName(templateIndex));

            final Record template = templates.getTemplate(templateIndex);

            for (int i = 0; i < template.getItemsCount(); ++i)
            {
                final Record.Item template_item = template.getItem(i);
                record.addItem(template_item.key, template_item.value);
            }
        }
        else
        {
            record.setPublicName(getResources().getString(R.string.enter_public_name_here));
        }

        inputNewRecordNameUI(record);
    }

    private void inputNewRecordNameUI(final Record record) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.enter_public_name);

        final EditText editor = new EditText(this);
        editor.setText(record.getPublicName());
        editor.setSelection(0, record.getPublicName().length());
        alert.setView(editor);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                record.setPublicName(editor.getText().toString());
                IDataKeeper dataKeeper = m_engine;
                dataKeeper.addRecord(record);
                startEditRecordActivity(dataKeeper.getRecordCount() - 1);
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        dialog.show();
    }

    private void startEditRecordActivity(int index) {
        m_engine.setSelectedIndex(index);

        Intent intent = new Intent(this, RecordViewActivity.class);
        startActivity(intent);
    }

    private void showMessage(int id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(id);
        alert.setPositiveButton(R.string.ok, null);
        alert.show();
    }

    private int m_passwordAttempts = 3;

    private Engine m_engine = null;

    private ListView     m_mainListView = null;

    @Override
    public void onTimerStart(int secondsLeft) {
    }

    @Override
    public void onTimer(int secondsLeft) {
        Log.d("IdleTimer", "Timer left: " + (int)(secondsLeft / 1000) + " seconds" );
    }

    @Override
    public void onTimeOver() {
        Log.d("IdleTimer", "Time over");

        if (m_engine != null) {
            m_engine.stopIdleTimer();;
            m_engine = null;
            finish();
            System.exit(0);
        }
    }
}