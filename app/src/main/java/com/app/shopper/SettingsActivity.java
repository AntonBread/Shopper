package com.app.shopper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.background));
        setContentView(R.layout.activity_settings);
    
        // ------------------------------------------------------------------------------------------------------
        // TOOLBAR SETUP
        // ------------------------------------------------------------------------------------------------------
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, ManageListActivity.class)));
        // ------------------------------------------------------------------------------------------------------
    }
}