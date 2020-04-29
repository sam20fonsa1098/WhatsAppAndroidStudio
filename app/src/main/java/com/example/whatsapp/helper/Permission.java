package com.example.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {
    public static boolean permissionValidation(String[] permissions, Activity activity, int requestCode) {
        if(Build.VERSION.SDK_INT >= 23) {
            List<String> permissionList = new ArrayList<>();

            for(String perm: permissions) {
               Boolean hasPermission =  ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED;
               if(!hasPermission) {
                   permissionList.add(perm);
               }
            }

            if(permissionList.isEmpty()) {
                return true;
            }
            String[] newPermissionList = new String[permissionList.size()];
            permissionList.toArray(newPermissionList);
            ActivityCompat.requestPermissions(activity, newPermissionList, requestCode);
        }
        return true;
    }
}
