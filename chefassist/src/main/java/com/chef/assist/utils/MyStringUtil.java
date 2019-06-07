package com.chef.assist.utils;

public class MyStringUtil {
    private static final String ALPHA_STRING = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
    private static final String NUMERIC_STRING = "0123456789";
    private static final String AlPHA_NUMERIC_STRING = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";

    public static String randomAlpha(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_STRING.length());
            builder.append(ALPHA_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String randomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*NUMERIC_STRING.length());
            builder.append(NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String randomAlphaNumeric(int count){
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*AlPHA_NUMERIC_STRING.length());
            builder.append(AlPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
