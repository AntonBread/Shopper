package com.app.shopper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.app.shopper.R;

public class LoadSaveConfirmationDialogFragment extends DialogFragment {
    
    public static final String LOAD_CONFIRM_DIALOG_REQUEST_KEY = "load_confirm_dialog_request_key";
    public static final String LOAD_CONFIRM_DIALOG_RESULT_KEY = "load_confirm_dialog_result_key";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_load_confirm);
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        
        // Positive button sends 'true' value to activity
        Button btnPos = dialog.findViewById(R.id.dialog_load_btn_positive);
        btnPos.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean(LOAD_CONFIRM_DIALOG_RESULT_KEY, true);
            getActivity().getSupportFragmentManager().setFragmentResult(LOAD_CONFIRM_DIALOG_REQUEST_KEY, result);
            dismiss();
        });
        
        // Negative button just dismisses dialog
        dialog.findViewById(R.id.dialog_load_btn_negative).setOnClickListener(v -> dismiss());
    }
}
