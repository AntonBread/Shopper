package com.app.shopper.util;

import androidx.annotation.NonNull;

import java.util.*;

public class StringUtils {
    
    @NonNull
    public static String formatItemNameInput(String name) {
        name = name.replaceAll("\\p{Punct}", "").trim();
        return capitalizeString(name);
    }
    
    public static int validateItemNameInput(String name, ArrayList<String> list) {
        boolean empty = name.isEmpty();
        if (empty)
            return -1;
        
        boolean alreadyExists = false;
        for (String listItem: list) {
            if (name.equalsIgnoreCase(listItem)) {
                alreadyExists = true;
                break;
            }
        }
        if (alreadyExists)
            return -2;
        
        return 0;
    }
    
    @NonNull
    public static String capitalizeString(String str) {
        if (str.isEmpty())
            return str;
        String capStr = str.substring(0, 1).toUpperCase() + str.substring(1);
        return capStr.trim();
    }
    
    public static String formatSaveNameInput(String fileName) {
        fileName = fileName.replaceAll("\\p{Punct}", "").trim();
        fileName = capitalizeString(fileName);
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        return fileName;
    }
    
    public static boolean validateSaveNameInput(String fileName, ArrayList<String> saveNames) {
        String tempCheckName = fileName.replaceAll(".txt", "");
        boolean tempCollision = tempCheckName.equalsIgnoreCase("temp") || tempCheckName.equalsIgnoreCase("tempsave");
        boolean alreadyExists = false;
        for (String listName: saveNames) {
            if (fileName.equalsIgnoreCase(listName)) {
                alreadyExists = true;
                break;
            }
        }
        return (!fileName.isEmpty() && !tempCollision && !alreadyExists);
    }
}
