package com.app.shopper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.app.shopper.R;

public class DeleteItemConfirmationDialogFragment extends DialogFragment {
    
    public static final String DELETE_DIALOG_RESULT_KEY = "delete_dialog_result_key";
    public static final String DELETE_DIALOG_REQUEST_KEY = "delete_dialog_request_key";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_delete_confirm);
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        
        // Make dialog window appear at the bottom of the screen
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        
        
        Button btn_pos = dialog.findViewById(R.id.dialog_delete_btn_confirm);
        btn_pos.setBackgroundTintList(getContext().getColorStateList(R.color.blue));
        // Custom positive button listener
        btn_pos.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean(DELETE_DIALOG_RESULT_KEY, true);
            getActivity().getSupportFragmentManager().setFragmentResult(DELETE_DIALOG_REQUEST_KEY, result);
            dismiss();
        });
        // No negative button, touch outside of dialog window to exit with negative result
    }
    
}
