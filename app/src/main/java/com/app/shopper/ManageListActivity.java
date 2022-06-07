package com.app.shopper;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.shopper.dialogs.DeleteItemConfirmationDialogFragment;
import com.app.shopper.dialogs.LoadCustomDialogFragment;
import com.app.shopper.dialogs.LoadSaveConfirmationDialogFragment;
import com.app.shopper.dialogs.RenameItemDialogFragment;
import com.app.shopper.dialogs.SaveListDialogFragment;
import com.app.shopper.util.CacheHelper;
import com.app.shopper.util.DrawableUtils;
import com.app.shopper.util.SettingsHelper;
import com.app.shopper.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class ManageListActivity extends AppCompatActivity {
    
    protected ArrayList<String> items = new ArrayList<>(32);
    private ArrayAdapter<String> itemAdapter;
    private ListView itemList;
    private Toolbar toolbar;
    private TextView emptyListText;
    
    private AutoCompleteTextView itemInput;
    private ArrayList<String> autoCompleteStrings;
    private ArrayAdapter<String> itemInputAdapter;
    
    private MenuItem removeSelection;
    private MenuItem rename;
    
    private AdapterView.OnItemClickListener listClickListener;
    
    private View.OnClickListener navigationDefaultListener;
    private View.OnClickListener navigationSelectionListener;
    
    protected static final String TEMP_SAVE_NAME = "tempSave.txt";
    public static String SAVE_PATH;
    
    public static String CACHE_PATH;
    
    private final String SHOP_LIST_NOTIFICATION_CHANNEL_ID = "shopper_shopList_notificationChannel";
    private final int SHOP_LIST_NOTIFICATION_ID = 1; // Not sure if it can be used anywhere after notification init
    
    protected static final String START_CODE_EXTRA_NAME = "start_code";
    protected static final String SAVE_FILE_EXTRA_NAME = "save_file_extra";
    private final int START_CODE_EMPTY = 0; // Default activity start
    protected static final int START_CODE_LOAD_TEMP = 1; // To load temp save file
    protected static final int START_CODE_LOAD_SAVE = 2; // To load custom save file
    
    private boolean isToolbarModeSelection = false;
    
    // TODO: Background selector for cylindrical and circle buttons
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper.setLocale(this);
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.background));
        setContentView(R.layout.activity_manage_list);
        createNotificationChannel(SHOP_LIST_NOTIFICATION_CHANNEL_ID,
                                  getString(R.string.notification_shopList_name),
                                  getString(R.string.notification_shopList_description));
        
        SAVE_PATH = getFilesDir().getAbsolutePath() + "/save";
        CACHE_PATH = getCacheDir().getAbsolutePath();
        
        // ------------------------------------------------------------------------------------------------------
        // TOOLBAR SETUP
        // ------------------------------------------------------------------------------------------------------
        toolbar = findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setOverflowIcon(AppCompatResources.getDrawable(this, R.drawable.ic_overflow_24));
        
        navigationDefaultListener = v -> showNavigationPopupMenu();
        navigationSelectionListener = v -> {
            int itemCount = itemList.getCount();
            for (int i = 0; i < itemCount; i++) {
                itemList.setItemChecked(i, false);
            }
            setToolbarModeDefault();
        };
        
        toolbar.setNavigationOnClickListener(navigationDefaultListener);
        // ------------------------------------------------------------------------------------------------------
        
        // ------------------------------------------------------------------------------------------------------
        // LISTVIEW & ADAPTER SETUP
        // ------------------------------------------------------------------------------------------------------
        itemList = findViewById(R.id.list_shopList);
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
        itemList.setAdapter(itemAdapter);
        
        listClickListener = (parent, view, position, id) -> {
            updateToolbarTitle();
            updateRemoveSelectionMenuOption();
            int selectedCount = itemList.getCheckedItemCount();
            // If no items are selected => back to default toolbar
            if (selectedCount == 0) {
                setToolbarModeDefault();
            }
            else if (selectedCount == 1) {
                enableSingleItemMenuOptions();
            }
            // Single-item menu options are disabled if more than one are selected
            else if (selectedCount > 1) {
                disableSingleItemMenuOptions();
            }
        };
        
        AdapterView.OnItemLongClickListener listLongClickListener = (parent, view, position, id) -> {
            setToolbarModeSelection();
            itemList.setItemChecked(position, true);
            updateToolbarTitle();
            updateRemoveSelectionMenuOption();
            return true;
        };
        
        itemList.setOnItemLongClickListener(listLongClickListener);
        // ------------------------------------------------------------------------------------------------------
        
        // ------------------------------------------------------------------------------------------------------
        // INPUT AUTO COMPLETE TEXT VIEW SETUP
        // ------------------------------------------------------------------------------------------------------
        CacheHelper.createItemsCache(); // recreates file in case cache has been cleared
        itemInput = findViewById(R.id.list_itemNameInput);
        autoCompleteStrings = CacheHelper.getItemsCache();
        itemInputAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, autoCompleteStrings);
        itemInput.setAdapter(itemInputAdapter);
        // ------------------------------------------------------------------------------------------------------
        
        
        // ------------------------------------------------------------------------------------------------------
        // START CODE CHECK
        // ------------------------------------------------------------------------------------------------------
        emptyListText = findViewById(R.id.list_empty_text);
        int start_code = getIntent().getIntExtra(START_CODE_EXTRA_NAME, START_CODE_EMPTY);
        switch (start_code) {
            case START_CODE_EMPTY:
                items.clear();
                itemAdapter.notifyDataSetChanged();
                emptyListText.setVisibility(View.VISIBLE);
                break;
            
            case START_CODE_LOAD_TEMP:
                loadListTemp(true);
                break;
            
            case START_CODE_LOAD_SAVE:
                String saveFileName = getIntent().getStringExtra(SAVE_FILE_EXTRA_NAME);
                if (saveFileName == null) {
                    Toast.makeText(this, R.string.list_load_error_null, Toast.LENGTH_SHORT).show();
                    break;
                }
                loadList(saveFileName);
                break;
        }
        // ------------------------------------------------------------------------------------------------------
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Write current list to temp save
        // If any items are present of course
        if (items.size() < 1)
            return;
        saveListTemp();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        int count = itemList.getCount();
        if (count < 1)
            return;
        displayShopListNotification();
    }
    
    // ACTIVITY LIFE CYCLE ENDS HERE
    // ------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------
    
    public void addItem(String item) {
        if (items.size() < 1) {
            emptyListText.setVisibility(View.GONE);
        }
        items.add(item);
        itemAdapter.notifyDataSetChanged();
        if (isToolbarModeSelection) {
            updateToolbarTitle();
            updateRemoveSelectionMenuOption();
        }
    }
    
    public void removeItem(String item) {
        // Manually uncheck item before removal
        itemList.setItemChecked(itemAdapter.getPosition(item), false);
        items.remove(item);
        itemAdapter.notifyDataSetChanged();
        if (itemList.getCheckedItemCount() < 1) {
            setToolbarModeDefault();
        }
        if (items.size() < 1) {
            emptyListText.setVisibility(View.VISIBLE);
        }
    }
    
    public void renameItem(String item) {
        // int pos = getCheckedItemPosition() was kinda unreliable
        // so SparseBooleanArray is used instead
        SparseBooleanArray checked = itemList.getCheckedItemPositions();
        int pos = 0;
        while (!checked.get(pos) && pos < items.size())
            pos++;
        items.set(pos, item);
        setToolbarModeDefault();
        itemAdapter.notifyDataSetChanged();
    }
    
    // ONLY USE FOR 'SET TOOLBAR DEFAULT MODE' METHOD
    private void clearListViewChoices() {
        itemList.clearChoices();
        for (int i = 0; i < itemList.getCount(); i++) {
            itemList.setItemChecked(i, false);
        }
        itemList.post(() -> itemList.setChoiceMode(AbsListView.CHOICE_MODE_NONE));
    }
    
    public void onClickAddItem(View view) {
        // Make all necessary checks before adding item to list
        String item = itemInput.getText().toString();
        item = StringUtils.formatItemNameInput(item);
        int validity = StringUtils.validateItemNameInput(item, items);
        switch (validity) {
            case -1:
                Toast.makeText(this, getString(R.string.list_input_error_emptyItem), Toast.LENGTH_SHORT).show();
                break;
            case -2:
                Toast.makeText(this, getString(R.string.list_input_error_itemAlreadyExists), Toast.LENGTH_SHORT).show();
                break;
            case 0:
                addItem(item);
                // Adding item to cache is done on a separate thread so as not to slow UI thread
                final String itemFinal = item;
                new Thread(() -> {
                    if (CacheHelper.addToItemsCache(itemFinal)) {
                        // Hacky, but updating adapter via notifyDataSetChanged() doesn't work
                        itemInputAdapter.add(itemFinal);
                    }
                }).start();
                itemInput.getText().clear();
                break;
        }
    }
    
    private void createNotificationChannel(String id, CharSequence name, String description) {
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
    
    // This notification is displayed when activity is closed
    // Allows quick access to previously opened list
    private void displayShopListNotification() {
        Intent intent = new Intent(this, ManageListActivity.class);
        intent.putExtra(START_CODE_EXTRA_NAME, START_CODE_LOAD_TEMP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SHOP_LIST_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_shopList_contentTitle))
                .setContentText(getString(R.string.notification_shopList_contentText))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(SHOP_LIST_NOTIFICATION_ID, builder.build());
    }
    
    // ------------------------------------------------------------------------------------------------------
    // SAVE LOAD HANDLING METHODS
    // ------------------------------------------------------------------------------------------------------
    public void saveListTemp() {
        File tempSaveFile = new File(SAVE_PATH, TEMP_SAVE_NAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempSaveFile))) {
            writer.append(Boolean.toString(isToolbarModeSelection));// Also keep track of toolbar mode
            writer.newLine();
            
            SparseBooleanArray checked = itemList.getCheckedItemPositions(); // returns null if choice mode is NONE
            for (int i = 0; i < items.size(); i++) {
                int isChecked;
                if (checked == null)
                    isChecked = 0;
                else
                    isChecked = checked.get(i) ? 1 : 0;
                String name = items.get(i);
                String item = String.format("%s_%d", name, isChecked);
                writer.append(item);
                writer.newLine();
            }
        }
        catch (Exception e) {
            Log.d("DEBUG", "Temp Save Failed!");
            Log.d("DEBUG", e.getLocalizedMessage());
        }
    }
    
    public void loadListTemp(boolean loadSelection) {
        // Clear the list before loading anything
        items.clear();
        itemAdapter.notifyDataSetChanged();
        
        File tempSaveFile = new File(SAVE_PATH, TEMP_SAVE_NAME);
        try (BufferedReader reader = new BufferedReader(new FileReader(tempSaveFile))) {
            // Determine if saved list was in selection mode
            if (Boolean.parseBoolean(reader.readLine()) && loadSelection) {
                setToolbarModeSelection();
                onCreateOptionsMenu(toolbar.getMenu());
            }
            
            String item;
            int pos = 0;
            while ((item = reader.readLine()) != null) {
                int separator = item.indexOf('_');
                String name = item.substring(0, separator);
                int isCheckedInt = Integer.parseInt(item.substring(separator + 1));
                boolean isChecked = (isCheckedInt >= 1);
                addItem(name);
                if (loadSelection) {
                    itemList.setItemChecked(pos++, isChecked);
                    updateToolbarTitle();
                }
            }
        }
        catch (Exception e) {
            Log.d("DEBUG", Arrays.toString(e.getStackTrace()));
            String msg;
            if (!tempSaveFile.exists()) {
                msg = getString(R.string.list_load_temp_error_notExist);
            }
            else {
                msg = getString(R.string.list_load_temp_error_general);
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
    
    public void saveList(String fileName) {
        File saveFile = new File(SAVE_PATH, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for (int i = 0; i < items.size(); i++) {
                writer.append(items.get(i));
                writer.newLine();
            }
            String successMessage =
                    String.format(getString(R.string.list_save_success), fileName.replaceAll(".txt", ""));
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, R.string.list_save_error, Toast.LENGTH_LONG).show();
        }
    }
    
    public void loadList(String fileName) {
        // Clear the list before loading anything
        items.clear();
        itemAdapter.notifyDataSetChanged();
        
        File saveFile = new File(SAVE_PATH, fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String item;
            while ((item = reader.readLine()) != null) {
                addItem(item);
            }
        }
        catch (Exception e) {
            String msg = String.format(getString(R.string.list_load_error_general),
                                       fileName.replaceAll(".txt", ""));
            if (!saveFile.exists()) {
                msg = String.format(getString(R.string.list_load_error_notExist),
                                    fileName.replaceAll(".txt", ""));
            }
            else if (!saveFile.canRead()) {
                msg = String.format(getString(R.string.list_load_error_cantRead),
                                    fileName.replaceAll(".txt", ""));
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
    // ------------------------------------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------------------------------------
    // TOOLBAR & MENU METHODS
    // ------------------------------------------------------------------------------------------------------
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // There are 2 menu variants based on whether the user has selected any items in the list
        if (isToolbarModeSelection) {
            inflater.inflate(R.menu.menu_list_selection, menu);
            rename = menu.findItem(R.id.menu_action_rename);
            removeSelection = menu.findItem(R.id.menu_action_removeSelection);
            if (itemList.getCheckedItemCount() > 1) {
                disableSingleItemMenuOptions();
            }
            updateRemoveSelectionMenuOption();
        }
        else {
            inflater.inflate(R.menu.menu_list_default, menu);
        }
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true); // produces lint error for some reason
        }
        return true;
    }
    
    // Menu option click handler looks absolutely gargantuan
    // Note to self: in future try to divide menu options in smaller methods
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        
        // Various options menu variables
        SparseBooleanArray checked = itemList.getCheckedItemPositions();
        int itemCount = itemList.getCount();
        File saveDirectory = new File(SAVE_PATH);
        File[] saveFiles;
        Bundle args = new Bundle();
        
        switch (id) {
            
            case R.id.menu_action_delete:
                // Confirm deletion via dialog
                getSupportFragmentManager().setFragmentResultListener(
                        DeleteItemConfirmationDialogFragment.DELETE_DIALOG_REQUEST_KEY, this,
                        (requestKey, result) -> {
                            boolean delete =
                                    result.getBoolean(DeleteItemConfirmationDialogFragment.DELETE_DIALOG_RESULT_KEY,
                                                      false);
                            if (delete) {
                                for (int i = itemCount - 1; i >= 0; i--) {
                                    if (checked.get(i))
                                        removeItem(itemList.getItemAtPosition(i).toString());
                                }
                                itemList.setAdapter(
                                        itemList.getAdapter()); // A stupid hack, but without it selection checkboxes don't update properly
                            }
                        });
                new DeleteItemConfirmationDialogFragment().show(getSupportFragmentManager(), "delete_item_dialog");
                return true;
            
            case R.id.menu_action_selectAll:
                if (itemCount < 1) {
                    return false;
                }
                boolean menuUpdated = false;
                if (!isToolbarModeSelection) {
                    setToolbarModeSelection();
                    // Have to force redraw if menu options to change haven't been yet initialized
                    // onCreateOptionsMenu() slows app for half second when executed on UI thread
                    // Thus menu is redrawn on separate thread
                    if (rename == null || removeSelection == null) {
                        menuUpdated = true;
                        Runnable menuUpdateRunnable = () -> toolbar.post(() -> {
                            onCreateOptionsMenu(toolbar.getMenu());
                            if (itemCount > 1) {
                                disableSingleItemMenuOptions();
                            }
                            updateRemoveSelectionMenuOption();
                            invalidateOptionsMenu();
                        });
                        Thread menuUpdateThread = new Thread(menuUpdateRunnable);
                        menuUpdateThread.start();
                    }
                }
                for (int i = 0; i < itemCount; i++) {
                    itemList.setItemChecked(i, true);
                }
                updateToolbarTitle();
                // Update menu if it hasn't been in the separate thread
                if (!menuUpdated) {
                    if (itemCount > 1) {
                        disableSingleItemMenuOptions();
                    }
                    updateRemoveSelectionMenuOption();
                    invalidateOptionsMenu();
                }
                return true;
            
            case R.id.menu_action_removeSelection:
                for (int i = 0; i < itemCount; i++) {
                    itemList.setItemChecked(i, false);
                }
                setToolbarModeDefault();
                return true;
            
            case R.id.menu_action_loadLast:
                // If list is non-empty — confirm loading
                if (itemCount > 0) {
                    getSupportFragmentManager().setFragmentResultListener(
                            LoadSaveConfirmationDialogFragment.LOAD_CONFIRM_DIALOG_REQUEST_KEY, this,
                            (requestKey, result) -> {
                                boolean load =
                                        result.getBoolean(
                                                LoadSaveConfirmationDialogFragment.LOAD_CONFIRM_DIALOG_RESULT_KEY,
                                                false);
                                if (!load) {
                                    return;
                                }
                                loadListTemp(false);
                                if (isToolbarModeSelection) {
                                    setToolbarModeDefault();
                                }
                            });
                    new LoadSaveConfirmationDialogFragment().show(getSupportFragmentManager(), "dialog_load_confirm");
                    return true;
                }
                loadListTemp(false);
                if (isToolbarModeSelection) {
                    setToolbarModeDefault();
                }
                return true;
            
            case R.id.menu_action_rename:
                // Additional check that only one item is selected
                if (itemList.getCheckedItemCount() != 1) {
                    Toast.makeText(this, getString(R.string.list_toolbar_overflow_rename_error_moreThan1),
                                   Toast.LENGTH_SHORT).show();
                    return false;
                }
                
                // int pos = getCheckedItemPosition() was kinda unreliable
                // so SparseBooleanArray is used instead
                int pos = 0;
                while (!checked.get(pos) && pos < items.size())
                    pos++;
                String item = items.get(pos);
                
                // Renaming is done via dialog window
                RenameItemDialogFragment renameDialog = new RenameItemDialogFragment();
                args.putString(RenameItemDialogFragment.ITEM_NAME_KEY, item);
                args.putStringArrayList(RenameItemDialogFragment.ITEM_LIST_KEY, items);
                renameDialog.setArguments(args);
                renameDialog.show(getSupportFragmentManager(), "rename_dialog");
                return true;
            
            case R.id.menu_action_saveList:
                // Check if there's anything to save
                if (itemCount < 1) {
                    Toast.makeText(this, R.string.list_save_empty, Toast.LENGTH_SHORT).show();
                    return false;
                }
                
                // Input save name via dialog
                getSupportFragmentManager().setFragmentResultListener(SaveListDialogFragment.SAVE_DIALOG_REQUEST_KEY,
                                                                      this, (requestKey, result) -> {
                            String fileName = result.getString(SaveListDialogFragment.SAVE_DIALOG_RESULT_KEY);
                            if (fileName == null) {
                                Toast.makeText(this, R.string.list_save_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            saveList(fileName);
                        });
                SaveListDialogFragment saveDialog = new SaveListDialogFragment();
                // Send already existing file names to dialog
                saveFiles = saveDirectory.listFiles();
                ArrayList<String> saves = new ArrayList<>(32);
                if (!(saveFiles == null)) {
                    for (File saveFile : saveFiles) {
                        saves.add(saveFile.getName());
                    }
                }
                args.putStringArrayList(SaveListDialogFragment.FILE_NAMES_LIST_KEY, saves);
                saveDialog.setArguments(args);
                saveDialog.show(getSupportFragmentManager(), "save_list_dialog");
                return true;
            
            
            case R.id.menu_action_loadCustom:
                saveFiles = saveDirectory.listFiles((dir, name) -> !name.equals(TEMP_SAVE_NAME));
                if (saveFiles == null || saveFiles.length == 0) {
                    Toast.makeText(this, R.string.dialog_load_custom_noSaves, Toast.LENGTH_SHORT).show();
                    return false;
                }
                
                // If current list is non-empty — confirm loading
                if (itemCount > 0) {
                    // Nested dialog fragment call looks nasty
                    // No effect on performance though
                    // Maybe will come up with a better solution later
                    getSupportFragmentManager().setFragmentResultListener(
                            LoadSaveConfirmationDialogFragment.LOAD_CONFIRM_DIALOG_REQUEST_KEY, this,
                            (requestKeyConfirmation, resultConfirmation) -> {
                                boolean load =
                                        resultConfirmation.getBoolean(
                                                LoadSaveConfirmationDialogFragment.LOAD_CONFIRM_DIALOG_RESULT_KEY,
                                                false);
                                if (!load) {
                                    return;
                                }
                                
                                // Convert file array to array list of file names to be sent to dialog
                                ArrayList<String> saveNames = new ArrayList<>(saveFiles.length);
                                String saveName;
                                for (File saveFile : saveFiles) {
                                    saveName = saveFile.getName().replace(".txt", "");
                                    saveNames.add(saveName);
                                }
                                
                                getSupportFragmentManager().setFragmentResultListener(
                                        LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_REQUEST_KEY, this,
                                        (requestKeySave, resultSave) -> {
                                            String saveFileName =
                                                    resultSave.getString(
                                                            LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_RESULT_KEY);
                                            if (saveFileName == null) {
                                                Toast.makeText(this, R.string.dialog_load_custom_result_null,
                                                               Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            loadList(saveFileName);
                                            if (isToolbarModeSelection) {
                                                setToolbarModeDefault();
                                            }
                                        });
                                LoadCustomDialogFragment loadDialog = new LoadCustomDialogFragment();
                                args.putStringArrayList(LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_SAVE_FILES_KEY,
                                                        saveNames);
                                loadDialog.setArguments(args);
                                loadDialog.show(getSupportFragmentManager(), "load_custom_dialog");
                            });
                    new LoadSaveConfirmationDialogFragment().show(getSupportFragmentManager(), "dialog_load_confirm");
                }
                else {
                    // Convert file array to array list of file names to be sent to dialog
                    ArrayList<String> saveNames = new ArrayList<>(saveFiles.length);
                    String saveName;
                    for (File saveFile : saveFiles) {
                        saveName = saveFile.getName().replace(".txt", "");
                        saveNames.add(saveName);
                    }
                    
                    getSupportFragmentManager().setFragmentResultListener(
                            LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_REQUEST_KEY, this,
                            (requestKey, result) -> {
                                String selectedSaveName =
                                        result.getString(LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_RESULT_KEY);
                                if (selectedSaveName == null) {
                                    Toast.makeText(this, R.string.dialog_load_custom_result_null, Toast.LENGTH_LONG)
                                         .show();
                                    return;
                                }
                                loadList(selectedSaveName);
                                if (isToolbarModeSelection) {
                                    setToolbarModeDefault();
                                }
                            });
                    LoadCustomDialogFragment loadDialog = new LoadCustomDialogFragment();
                    args.putStringArrayList(LoadCustomDialogFragment.LOAD_CUSTOM_DIALOG_SAVE_FILES_KEY, saveNames);
                    loadDialog.setArguments(args);
                    loadDialog.show(getSupportFragmentManager(), "load_custom_dialog");
                }
                return true;
            
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
    
    private void enableSingleItemMenuOptions() {
        rename.setEnabled(true);
        rename.setVisible(true);
    }
    
    private void disableSingleItemMenuOptions() {
        rename.setEnabled(false);
        rename.setVisible(false);
    }
    
    private void updateRemoveSelectionMenuOption() {
        Menu menu = toolbar.getMenu();
        menu.close();
        MenuItem selectAll = menu.findItem(R.id.menu_action_selectAll);
        
        // Additional failsafe check
        // Forced menu redraw doesn't seem to slow the app, so done on UI thread
        if (removeSelection == null) {
            setToolbarModeSelection();
            onCreateOptionsMenu(toolbar.getMenu());
        }
        
        int itemCount = itemList.getCount();
        int checkedCount = itemList.getCheckedItemCount();
        if (itemCount == checkedCount && isToolbarModeSelection) {
            selectAll.setVisible(false);
            selectAll.setEnabled(false);
            removeSelection.setVisible(true);
            removeSelection.setEnabled(true);
        }
        else if (!selectAll.isVisible()) {
            removeSelection.setVisible(false);
            removeSelection.setEnabled(false);
            selectAll.setVisible(true);
            selectAll.setEnabled(true);
        }
    }
    
    private void setToolbarModeDefault() {
        isToolbarModeSelection = false;
        toolbar.setBackground(new ColorDrawable(getColor(R.color.toolbar)));
        updateStatusBarColor();
        itemList.setOnItemClickListener(null);
        clearListViewChoices();
        invalidateOptionsMenu();
        updateToolbarTitle();
        updateToolbarNavigation();
    }
    
    private void setToolbarModeSelection() {
        isToolbarModeSelection = true;
        updateStatusBarColor();
        toolbar.setBackground(new ColorDrawable(getColor(R.color.blue)));
        itemList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        itemList.setOnItemClickListener(listClickListener);
        invalidateOptionsMenu();
        updateToolbarNavigation();
    }
    
    private void updateStatusBarColor() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        if (isToolbarModeSelection) {
            window.setStatusBarColor(getColor(R.color.blue));
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        else {
            window.setStatusBarColor(getColor(R.color.status_bar));
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
    
    private void updateToolbarTitle() {
        // Toolbar displays number of checked items in selection mode
        if (isToolbarModeSelection) {
            int checkedCount = itemList.getCheckedItemCount();
            int totalCount = itemList.getCount();
            String title = String.format(getString(R.string.list_toolbar_title_selection), checkedCount, totalCount);
            toolbar.setTitle(title);
        }
        else {
            toolbar.setTitle(getString(R.string.list_toolbar_title_default));
        }
    }
    
    private void updateToolbarNavigation() {
        if (isToolbarModeSelection) {
            toolbar.setNavigationIcon(R.drawable.ic_close_24);
            toolbar.setNavigationOnClickListener(navigationSelectionListener);
        }
        else {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_menu_24);
            toolbar.setNavigationOnClickListener(navigationDefaultListener);
        }
    }
    
    @SuppressLint("NonConstantResourceId")
    private void showNavigationPopupMenu() {
        PopupMenu navigationMenu = new PopupMenu(this, toolbar);
        navigationMenu.inflate(R.menu.menu_popup_navigation);
        navigationMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.popup_navigation_saves:
                    startActivity(new Intent(getApplicationContext(), SaveFilesActivity.class));
                    return true;
                
                case R.id.popup_navigation_settings:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    return true;
                
                default:
                    return false;
                
            }
        });
        
        MenuItem main = navigationMenu.getMenu().findItem(R.id.popup_navigation_main);
        main.setEnabled(false);
        main.setVisible(false);
        
        navigationMenu.show();
    }
    
}