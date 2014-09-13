package com.xebia.xtime.monthoverview.approve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.xebia.xtime.R;

/**
 * Confirmation dialog for approval requests.
 * <p/>
 * Note: call #setListener to get callbacks.
 */
public class ApproveConfirmDialog extends DialogFragment {

    private Listener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.approve_confirm_title);
        builder.setMessage(R.string.approve_confirm_msg);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onApproveConfirmed();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        public abstract void onApproveConfirmed();
    }
}
