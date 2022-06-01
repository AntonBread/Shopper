package com.app.shopper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.app.shopper.R;
import com.app.shopper.util.StringUtils;

import java.util.ArrayList;

public class RenameSaveDialogFragment extends DialogFragment {
    
    public static final String SAVE_NAME_KEY = "save_name_key";
    public static final String SAVES_LIST_KEY = "saves_list_key";
    public static final String SAVE_RENAME_DIALOG_RESULT_KEY = "save_rename_result";
    public static final String SAVE_RENAME_DIALOG_REQUEST_KEY = "save_rename_request";
    
    private EditText nameEdit;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        builder.setView(R.layout.dialog_save_list);
        builder.setTitle(R.string.dialog_rename_title);
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Bundle args = getArguments();
        
        nameEdit = dialog.findViewById(R.id.dialog_save_input);
        String startText = args.getString(SAVE_NAME_KEY, "").trim();
        nameEdit.setText(startText);
        nameEdit.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        
        TextView alreadyExistsMessage = dialog.findViewById(R.id.dialog_save_alreadyExists_message);
        alreadyExistsMessage.setText(R.string.dialog_save_nameUnavailable_message);
        
        ArrayList<String> saveNames = args.getStringArrayList(SAVES_LIST_KEY);
        // ------------------------------------------------------------------------------------------------------
        // POSITIVE BUTTON
        // ------------------------------------------------------------------------------------------------------
        Button btnPos = dialog.findViewById(R.id.dialog_save_btn_positive);
        btnPos.setOnClickListener(v -> {
            String fileName = nameEdit.getText().toString();
            fileName = StringUtils.formatSaveNameInput(fileName);
            if (StringUtils.validateSaveNameInput(fileName.replaceAll(".txt", "").trim(), saveNames)) {
                Bundle result = new Bundle();
                result.putString(SAVE_RENAME_DIALOG_RESULT_KEY, fileName.replaceAll(".txt", "").trim());
                getActivity().getSupportFragmentManager().setFragmentResult(SAVE_RENAME_DIALOG_REQUEST_KEY, result);
                dismiss();
            }
            // Notify user if save name is already used
            else {
                nameEdit.setBackground(getContext().getDrawable(R.drawable.edit_text_frame_red));
                alreadyExistsMessage.setVisibility(View.VISIBLE);
            }
        });
        
        // Positive button is disabled on start
        // EditText input has to be non-empty and different from original name for button to become active
        disable(btnPos);
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.isEmpty() || input.equals(startText)) {
                    disable(btnPos);
                }
                else {
                    enable(btnPos);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Remove name collision warning if it's visible
                if (alreadyExistsMessage.getVisibility() == View.VISIBLE) {
                    alreadyExistsMessage.setVisibility(View.GONE);
                    nameEdit.setBackground(getContext().getDrawable(R.drawable.edit_text_frame_bone));
                }
            }
        });
        // ------------------------------------------------------------------------------------------------------
        // Negative button just dismisses dialog
        dialog.findViewById(R.id.dialog_save_btn_negative).setOnClickListener(v -> dismiss());
    }
    
    private void disable(Button btn) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(getContext().getColorStateList(R.color.background));
    }
    
    private void enable(Button btn) {
        btn.setEnabled(true);
        btn.setBackgroundTintList(getContext().getColorStateList(R.color.custom));
    }
}
