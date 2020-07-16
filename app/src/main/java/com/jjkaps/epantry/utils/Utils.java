package com.jjkaps.epantry.utils;

public class Utils {
    public static boolean isNotNullOrEmpty(Object o){
        return o != null && !o.equals("null") && !o.equals("");
    }
}
