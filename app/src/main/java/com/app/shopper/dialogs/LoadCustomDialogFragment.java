package com.app.shopper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.*;

import com.app.shopper.R;
import com.app.shopper.util.StringUtils;

public class LoadCustomDialogFragment extends DialogFragment {
    
    private ArrayList<String> saveNames;
    private ListView saveList;
    private ArrayAdapter<String> saveListAdapter;
    
    private String selectedSaveName;
    
    public static final String LOAD_CUSTOM_DIALOG_SAVE_FILES_KEY = "load_custom_dialog_save_files_key";
    public static final String LOAD_CUSTOM_DIALOG_REQUEST_KEY = "load_custom_dialog_request_key";
    public static final String LOAD_CUSTOM_DIALOG_RESULT_KEY = "load_custom_dialog_result_key";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_load_custom);
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Bundle args = getArguments();
    
        // Receive save names list from activity
        saveNames = args.getStringArrayList(LOAD_CUSTOM_DIALOG_SAVE_FILES_KEY);
        
        saveList = dialog.findViewById(R.id.dialog_load_custom_saveList);
        saveListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, saveNames);
        saveList.setAdapter(saveListAdapter);
        saveList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        
        saveList.setOnItemClickListener(
                (parent, view, position, id) -> selectedSaveName = saveList.getItemAtPosition(position).toString());
    
        // ------------------------------------------------------------------------------------------------------
        // Positive Button
        // ------------------------------------------------------------------------------------------------------
        Button btnPos = dialog.findViewById(R.id.dialog_load_custom_btn_positive);
        btnPos.setOnClickListener(v -> {
            if (selectedSaveName == null) {
                Toast.makeText(getContext(), R.string.dialog_load_custom_noSelection, Toast.LENGTH_SHORT).show();
                return;
            }
            selectedSaveName = StringUtils.formatSaveNameInput(selectedSaveName);
            
            Bundle result = new Bundle();
            result.putString(LOAD_CUSTOM_DIALOG_RESULT_KEY, selectedSaveName);
            getActivity().getSupportFragmentManager().setFragmentResult(LOAD_CUSTOM_DIALOG_REQUEST_KEY, result);
            dismiss();
        });
    
        // Negative button
        dialog.findViewById(R.id.dialog_load_custom_btn_negative).setOnClickListener(v -> dismiss());
    }
}
