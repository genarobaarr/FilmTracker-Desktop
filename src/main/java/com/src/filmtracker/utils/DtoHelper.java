package com.src.filmtracker.utils;

public final class DtoHelper {

    private DtoHelper() {
        // Constructor privado para clase utilitaria estática
    }

    public static String parseSafeId(Object id) {
        if (id == null) {
            return "";
        }
        
        String strId = String.valueOf(id);
        
        if (strId.endsWith(".0")) {
            return strId.substring(0, strId.length() - 2);
        }
        
        return strId;
    }

    public static String parseOwnerId(Object authId) {
        if (authId == null) {
            return "";
        }
        
        return String.valueOf(authId);
    }

    public static String parseImageUrl(String imageUrl) {
        if (imageUrl == null) {
            return "";
        }
        
        return imageUrl;
    }

    public static int parseCount(Object countObj) {
        try { 
            if (countObj == null) {
                return 0;
            }
            
            String val = String.valueOf(countObj);
            
            if (val.endsWith(".0")) {
                val = val.substring(0, val.length() - 2);
            }
            
            return Integer.parseInt(val); 
        } catch (Exception e) { 
            return 0; 
        }
    }

    public static boolean parseIsLiked(Object likedByMe) {
        if (likedByMe == null) {
            return false;
        }
        
        if (likedByMe instanceof Boolean b) {
            return b;
        }
        
        String s = likedByMe.toString().trim().toLowerCase();
        
        return s.equals("true") || s.equals("1");
    }

    public static int parseSafeInteger(Integer value) {
        if (value == null) {
            return 0;
        }
        
        return value;
    }
}