package com.barma.udk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.barma.udk.core.Engine;
import com.barma.udk.core.interfaces.ILongActionCallback;

/**
 * Created by Vitalii Misiura on 12/24/14.
 */
public class LongActionProgressUI implements ILongActionCallback{
    public LongActionProgressUI(Activity activity, int captionId) {
        m_activity = activity;
        m_captionId = captionId;
    }

    @Override
    public void onActionBegin(final Object args) {

        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Engine.OperationProgress progress_params = (Engine.OperationProgress) args;

                AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
                builder.setTitle(m_captionId);

                final LinearLayout layout = new LinearLayout(m_activity);
                layout.setOrientation(LinearLayout.VERTICAL);

                m_progress_bar = new ProgressBar(m_activity, null, android.R.attr.progressBarStyleHorizontal);
                m_progress_bar.setMax(progress_params.maximum);
                m_progress_bar.setProgress(progress_params.progress);

                layout.setPadding(40,20,40,20);
                layout.addView(m_progress_bar);
                builder.setView(layout);

                builder.setCancelable(false);

                m_dialog = builder.create();

                m_dialog.show();
            }
        });
    }

    @Override
    public void onActionStep(final Object args) {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Engine.OperationProgress progress_params = (Engine.OperationProgress) args;
                m_progress_bar.setProgress(progress_params.progress);
            }
        });
    }

    @Override
    public void onActionDone(final Object args) {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_dialog.dismiss();
            }
        });
    }

    private Activity m_activity;
    private int     m_captionId;
    private AlertDialog m_dialog;
    private ProgressBar m_progress_bar;
}
