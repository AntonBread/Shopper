package com.app.shopper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;

import com.app.shopper.util.SettingsHelper;

import java.util.Arrays;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    
    // Language selector listener call counter
    // Used to restart activity on language change
    private int x = 0;
    
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    public static final String PREFS_FILE_NAME = "settings";
    
    private Spinner languageSpinner;
    private ArrayAdapter<String> languageSpinnerAdapter;
    public static final String LANGUAGE = "PREFS_LANGUAGE";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper.setLocale(this);
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.background));
        setContentView(R.layout.activity_settings);
    
        // ------------------------------------------------------------------------------------------------------
        // TOOLBAR SETUP
        // ------------------------------------------------------------------------------------------------------
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, ManageListActivity.class)));
        // ------------------------------------------------------------------------------------------------------
    
        // ------------------------------------------------------------------------------------------------------
        // SETTINGS SETUP
        // ------------------------------------------------------------------------------------------------------
        settings = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        prefEditor = settings.edit();
        setupLanguageSetting();
        // ------------------------------------------------------------------------------------------------------
    }
    
    private void setupLanguageSetting() {
        String[] langNames = getResources().getStringArray(R.array.settings_language_names);
        String[] langCodes = getResources().getStringArray(R.array.settings_language_codes);
        languageSpinner = findViewById(R.id.settings_language_selector);
        languageSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item_right, langNames);
        languageSpinner.setAdapter(languageSpinnerAdapter);
        
        // Load language from prefs file
        String lang = settings.getString(LANGUAGE, null);
        if (lang == null) {
            String sysLang = Locale.getDefault().getLanguage();
            if (!sysLang.equals(new Locale("ru").getLanguage())) {
                sysLang = new Locale("en").getLanguage();
            }
            prefEditor.putString(LANGUAGE, sysLang);
            prefEditor.apply();
            lang = sysLang;
        }
        int pos = Arrays.binarySearch(langCodes, lang);
        languageSpinner.setSelection(pos);
        
        // Listener
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefEditor.putString(LANGUAGE, langCodes[position]);
                prefEditor.apply();
                x++;
                if (x > 1)
                    startActivity(getIntent());
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
}