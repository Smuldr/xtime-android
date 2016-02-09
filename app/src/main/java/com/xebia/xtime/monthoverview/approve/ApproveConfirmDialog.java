package com.xebia.xtime.monthoverview.approve;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.xebia.xtime.R;

/**
 * Confirmation dialog for approval requests.
 * <p/>
 * Note: call #setListener to get callbacks.
 */
public class ApproveConfirmDialog extends DialogFragment {

    private Listener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.approve_confirm_title);
        builder.setMessage(R.string.approve_confirm_msg);
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
        void onApproveConfirmed();
    }
}
