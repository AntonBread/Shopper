package com.app.shopper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentResultListener;

import com.app.shopper.dialogs.DeleteItemConfirmationDialogFragment;
import com.app.shopper.dialogs.RenameSaveDialogFragment;
import com.app.shopper.util.SettingsHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveFilesActivity extends AppCompatActivity {
    
    private ArrayList<String> saves = new ArrayList<>(32);
    private ListView savesListView;
    private ArrayAdapter<String> savesAdapter;
    private Toolbar toolbar;
    private TextView emptyListText;
    
    private File saveDirectory;
    
    private ArrayList<File> saveFiles = new ArrayList<>(32);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper.setLocale(this);
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.background));
        setContentView(R.layout.activity_save_files);
        
        // ------------------------------------------------------------------------------------------------------
        // TOOLBAR SETUP
        // ------------------------------------------------------------------------------------------------------
        toolbar = findViewById(R.id.saves_toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(
                v -> startActivity(new Intent(SaveFilesActivity.this, ManageListActivity.class)));
        // ------------------------------------------------------------------------------------------------------
        
        // ------------------------------------------------------------------------------------------------------
        // LISTVIEW & ADAPTER SETUP
        // ------------------------------------------------------------------------------------------------------
        savesListView = findViewById(R.id.saves_listOfFiles);
        savesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, saves);
        savesListView.setAdapter(savesAdapter);
        
        savesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            File saveFile = saveFiles.get(position);
            SaveFilesActivity.this.showPopupMenu(view, saveFile);
            return true;
        });
        // ------------------------------------------------------------------------------------------------------
    }
    
    @Override
    public void onStart() {
        super.onStart();
        saves.clear();
        savesAdapter.notifyDataSetChanged();
        
        emptyListText = findViewById(R.id.saves_empty_text);
        
        // LOAD ALL SAVE FILES EXCEPT TEMP TO LIST
        saveDirectory = new File(getFilesDir().getAbsolutePath() + "/save");
        File[] temp = saveDirectory.listFiles((dir, name) -> !name.equals(ManageListActivity.TEMP_SAVE_NAME));
        if (temp == null || temp.length == 0) {
            emptyListText.setVisibility(View.VISIBLE);
            return;
        }
        saveFiles.addAll(Arrays.asList(temp));
        for (File saveFile : saveFiles) {
            addSaveFile(saveFile);
        }
    }
    
    // ACTIVITY LIFE CYCLE ENDS HERE
    // ------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------
    
    private void addSaveFile(File saveFile) {
        saves.add(saveFile.getName().replace(".txt", ""));
        savesAdapter.notifyDataSetChanged();
    }
    
    // ------------------------------------------------------------------------------------------------------
    // POPUP MENU CLICK HANDLING
    // ------------------------------------------------------------------------------------------------------
    @SuppressLint("NonConstantResourceId")
    public void showPopupMenu(View v, File saveFile) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.popup_save_open:
                    openSaveFile(saveFile);
                    return true;
                
                case R.id.popup_save_delete:
                    getSupportFragmentManager().setFragmentResultListener(
                            DeleteItemConfirmationDialogFragment.DELETE_DIALOG_REQUEST_KEY, this,
                            (requestKey, result) -> {
                                boolean delete = result.getBoolean(
                                        DeleteItemConfirmationDialogFragment.DELETE_DIALOG_RESULT_KEY, false);
                                if (delete) {
                                    deleteSaveFile(saveFile);
                                }
                            });
                    DeleteItemConfirmationDialogFragment deleteDialog = new DeleteItemConfirmationDialogFragment();
                    Bundle args = new Bundle();
                    args.putBoolean(DeleteItemConfirmationDialogFragment.DELETE_DIALOG_ARGS_BOOL_KEY, true);
                    args.putString(DeleteItemConfirmationDialogFragment.DELETE_DIALOG_ARGS_STRING_KEY,
                                   saveFile.getName().replace(".txt", ""));
                    deleteDialog.setArguments(args);
                    deleteDialog.show(getSupportFragmentManager(), "delete_save_dialog");
                    return true;
                
                case R.id.popup_save_rename:
                    renameSaveFile(saveFile);
                    return true;
                
                default:
                    return false;
            }
        });
        popupMenu.inflate(R.menu.menu_popup_save_file);
        popupMenu.show();
    }
    
    private void deleteSaveFile(File saveFile) {
        int pos = saves.indexOf(saveFile.getName().replace(".txt", ""));
        if (!saveFiles.get(pos).delete()) {
            Toast.makeText(this, R.string.saves_file_delete_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        saves.remove(pos);
        savesAdapter.notifyDataSetChanged();
        saveFiles.remove(pos);
        if (saves.size() == 0) {
            emptyListText.setVisibility(View.VISIBLE);
        }
    }
    
    private void openSaveFile(File saveFile) {
        Intent intent = new Intent(this, ManageListActivity.class);
        intent.putExtra(ManageListActivity.SAVE_FILE_EXTRA_NAME, saveFile.getName());
        intent.putExtra(ManageListActivity.START_CODE_EXTRA_NAME, ManageListActivity.START_CODE_LOAD_SAVE);
        startActivity(intent);
    }
    
    private void renameSaveFile(File saveFile) {
        // Set dialog result listener, create dialog, send original name and list for dialog logic
        String oldName = saveFile.getName().replaceAll(".txt", "");
        getSupportFragmentManager().setFragmentResultListener(RenameSaveDialogFragment.SAVE_RENAME_DIALOG_REQUEST_KEY,
                                                              this, (requestKey, result) -> {
                    String newName = result.getString(RenameSaveDialogFragment.SAVE_RENAME_DIALOG_RESULT_KEY);
                    String newFileName = newName.concat(".txt");
                    File newSaveFile = new File(saveFile.getParent(), newFileName);
                    if (saveFile.renameTo(newSaveFile)) {
                        // If file's successfully renamed, update listview and file arraylist
                        int pos = saves.indexOf(oldName);
                        saves.set(pos, newName);
                        savesAdapter.notifyDataSetChanged();
                        saveFiles.set(pos, newSaveFile);
                    }
                    else {
                        // If not — notify user via toast
                        Toast.makeText(this, R.string.saves_file_rename_fail, Toast.LENGTH_LONG).show();
                    }
                });
        RenameSaveDialogFragment renameDialog = new RenameSaveDialogFragment();
        Bundle args = new Bundle();
        args.putString(RenameSaveDialogFragment.SAVE_NAME_KEY, oldName);
        args.putStringArrayList(RenameSaveDialogFragment.SAVES_LIST_KEY, saves);
        renameDialog.setArguments(args);
        renameDialog.show(getSupportFragmentManager(), "save_rename_dialog");
    }
    
    
}