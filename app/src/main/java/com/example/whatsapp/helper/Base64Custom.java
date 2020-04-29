package com.example.whatsapp.helper;

import android.util.Base64;

public class Base64Custom {

    public static String encodeBase64(String number) {
        return Base64.encodeToString(number.getBytes(), Base64.DEFAULT).replaceAll("(\\n|\\r)", "");
    }

    public static String decodeBas64(String numberCode) {
        return new String(Base64.decode(numberCode, Base64.DEFAULT));
    }
}
