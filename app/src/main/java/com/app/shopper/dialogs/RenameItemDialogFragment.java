package com.app.shopper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.app.shopper.ManageListActivity;
import com.app.shopper.R;
import com.app.shopper.util.StringUtils;

import java.util.ArrayList;

public class RenameItemDialogFragment extends DialogFragment {

    public static final String ITEM_NAME_KEY = "item_name";
    public static final String ITEM_LIST_KEY = "item_list";
    private EditText name_edit;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(R.layout.dialog_rename_item);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Bundle args = getArguments();

        name_edit = dialog.findViewById(R.id.dialog_rename_input);
        String startText = args.getString(ITEM_NAME_KEY, "").trim();
        name_edit.setText(startText);
        name_edit.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Button shenanigans
        ArrayList<String> list = args.getStringArrayList(ITEM_LIST_KEY);
        assert list != null;
        Button btn_pos = dialog.findViewById(R.id.dialog_rename_btn_positive);
        btn_pos.setOnClickListener(v -> {
            String name = name_edit.getText().toString();
            name = StringUtils.formatItemNameInput(name);
            int validity = StringUtils.validateItemNameInput(name, list);
            switch (validity) {
                case -1:
                    Toast.makeText(getActivity(), getString(R.string.list_input_error_emptyItem), Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(getActivity(), R.string.list_input_error_itemAlreadyExists, Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    ((ManageListActivity) getActivity()).renameItem(name);
                    dismiss();
                    break;
            }
        });
        
        Button btn_neg = dialog.findViewById(R.id.dialog_rename_btn_negative);
        btn_neg.setOnClickListener(v -> dismiss());

        // Positive button is disabled if the name is unchanged
        btn_pos.setEnabled(false);
        btn_pos.setBackgroundTintList(getContext().getColorStateList(R.color.btn_cylindrical_blue_tint));
        name_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals(startText)) {
                    btn_pos.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = StringUtils.formatItemNameInput(s.toString());
                if (text.equals(startText)) {
                    btn_pos.setEnabled(false);
                }
            }
        });
    }

}
