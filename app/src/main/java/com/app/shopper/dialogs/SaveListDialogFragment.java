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

import java.util.*;

public class SaveListDialogFragment extends DialogFragment {
    
    public static final String SAVE_DIALOG_REQUEST_KEY = "save_dialog_request_key";
    public static final String SAVE_DIALOG_RESULT_KEY = "save_dialog_result_key"; // dialog returns file name
    
    public static final String FILE_NAMES_LIST_KEY = "save_file_list_key";
    
    private EditText saveNameInput;
    private TextView alreadyExistsMessage;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_save_list);
        builder.setCancelable(false);
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.setCanceledOnTouchOutside(false);
        
        // Get save names from calling activity
        // Used to check for overlapping names
        ArrayList<String> saves = getArguments().getStringArrayList(FILE_NAMES_LIST_KEY);
        
        saveNameInput = dialog.findViewById(R.id.dialog_save_input);
        saveNameInput.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alreadyExistsMessage = dialog.findViewById(R.id.dialog_save_alreadyExists_message);
        
        // Positive button setup
        Button btn_pos = dialog.findViewById(R.id.dialog_save_btn_positive);
        btn_pos.setOnClickListener(v -> {
            String saveName = saveNameInput.getText().toString();
            saveName = StringUtils.formatSaveNameInput(saveName);
            boolean doesNotExist = StringUtils.validateSaveNameInput(saveName, saves);
            // If save file with given name doesn't yet exist — proceed normally
            if (doesNotExist) {
                Bundle result = new Bundle();
                result.putString(SAVE_DIALOG_RESULT_KEY, saveName);
                getActivity().getSupportFragmentManager().setFragmentResult(SAVE_DIALOG_REQUEST_KEY, result);
                dismiss();
            }
            // If file already exists — notify user
            else {
                saveNameInput.setBackground(getContext().getDrawable(R.drawable.edit_text_frame_red));
                alreadyExistsMessage.setVisibility(View.VISIBLE);
            }
        });
        
        // Negative button
        dialog.findViewById(R.id.dialog_save_btn_negative).setOnClickListener(v -> dismiss());
        
        // Positive button is disabled if input is empty
        btn_pos.setEnabled(false);
        btn_pos.setBackgroundTintList(getContext().getColorStateList(R.color.background));
        saveNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable positive button if input is non-empty
                if (!s.toString().isEmpty()) {
                    btn_pos.setEnabled(true);
                    btn_pos.setBackgroundTintList(getContext().getColorStateList(R.color.custom));
                }
                else {
                    btn_pos.setEnabled(false);
                    btn_pos.setBackgroundTintList(getContext().getColorStateList(R.color.background));
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Revert input background and hide error message in case previous input was a name collision
                saveNameInput.setBackground(getContext().getDrawable(R.drawable.edit_text_frame_bone));
                alreadyExistsMessage.setVisibility(View.GONE);

            }
        });
    }
    
}
