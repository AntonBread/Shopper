package com.app.shopper.util;

import android.util.Log;

import com.app.shopper.ManageListActivity;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class CacheHelper {
    
    private static final String CACHE_ITEMS_FILE_NAME = "items.txt";
    private static File cacheItems = new File(ManageListActivity.CACHE_PATH, CACHE_ITEMS_FILE_NAME);
    
    public static boolean createItemsCache() {
        try {
            return cacheItems.createNewFile();
        }
        catch (IOException e) {
            Log.d("DEBUG", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }
    
    // Returns true if item has been successfully added. Returns false otherwise.
    public static boolean addToItemsCache(String item) {
        File temp = new File(ManageListActivity.CACHE_PATH, "temp.txt");
        try {
            // Since cache reader is created first, have to check if cache file exists
            if (!cacheItems.exists()) {
                cacheItems.createNewFile();
            }
            BufferedReader cacheReader = new BufferedReader(new FileReader(cacheItems));
            
            ArrayList<String> lines = cacheReader.lines().collect(Collectors.toCollection(ArrayList::new));
            cacheReader.close();
            // Items cache must not contain duplicates
            if (listContainsItem(lines, item)) {
                return false;
            }
            int itemCount = lines.size();
            
            // Items cache should hold no more than 200 items
            if (itemCount > 200) {
                // If it does hold more — create temp file with all items except first and write the new item at the bottom
                BufferedWriter tempWriter = new BufferedWriter(new FileWriter(temp));
                for (int i = 1; i <= 199; i++) {
                    tempWriter.append(lines.get(i));
                    tempWriter.newLine();
                }
                tempWriter.append(item);
                tempWriter.newLine();
                tempWriter.close();
                
                // Then copy temp's contents to cache file and delete it
                copyContent(temp, cacheItems);
                temp.delete();
                return true;
            }
            else {
                BufferedWriter cacheWriter = new BufferedWriter(new FileWriter(cacheItems, true));
                cacheWriter.append(item);
                cacheWriter.newLine();
                cacheWriter.close();
                return true;
            }
        }
        catch (IOException e) {
            Log.d("DEBUG", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }
    
    public static ArrayList<String> getItemsCache() {
        ArrayList<String> items = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(cacheItems))) {
            items = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        }
        catch (IOException e) {
            Log.d("DEBUG", Arrays.toString(e.getStackTrace()));
        }
        return items;
    }
    
    public static boolean deleteFromItemsCache(String item) {
        return true;
    }
    
    private static void copyContent(File donor, File receiver) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(receiver)); BufferedReader reader = new BufferedReader(new FileReader(donor))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.append(line);
            }
        }
        catch (IOException e) {
            Log.d("DEBUG", Arrays.toString(e.getStackTrace()));
        }
    }
    
    private static boolean listContainsItem(ArrayList<String> list, String item) {
        for (String listItem: list) {
            if (listItem.equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }
}
