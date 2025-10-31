package com.mit.bodhiq.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for handling runtime permissions in the application.
 * Provides methods to check, request, and manage file access permissions.
 */
@Singleton
public class PermissionHelper {
    
    // Permission request codes
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1001;
    public static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1002;
    public static final int REQUEST_CODE_ALL_PERMISSIONS = 1003;
    
    // Required permissions for different Android versions
    private static final String[] STORAGE_PERMISSIONS_LEGACY = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private static final String[] STORAGE_PERMISSIONS_MODERN = {
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    };
    
    private static final String[] NOTIFICATION_PERMISSIONS = {
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    @Inject
    public PermissionHelper() {
        // Constructor for dependency injection
    }
    
    /**
     * Checks if storage permissions are granted.
     */
    public boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses scoped storage, no special permissions needed for app-specific directories
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 uses scoped storage but may need READ_EXTERNAL_STORAGE for some cases
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                   == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 9 and below need both READ and WRITE permissions
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                   == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                   == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Checks if notification permissions are granted (Android 13+).
     */
    public boolean hasNotificationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                   == PackageManager.PERMISSION_GRANTED;
        }
        return true; // No permission needed for older versions
    }
    
    /**
     * Gets the list of storage permissions needed for the current Android version.
     */
    public String[] getRequiredStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[0]; // No permissions needed for app-specific directories
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            return STORAGE_PERMISSIONS_LEGACY;
        }
    }
    
    /**
     * Gets the list of notification permissions needed.
     */
    public String[] getRequiredNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return NOTIFICATION_PERMISSIONS;
        }
        return new String[0];
    }
    
    /**
     * Requests storage permissions from the user.
     */
    public void requestStoragePermissions(Activity activity) {
        String[] permissions = getRequiredStoragePermissions();
        if (permissions.length > 0) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }
    
    /**
     * Requests notification permissions from the user.
     */
    public void requestNotificationPermissions(Activity activity) {
        String[] permissions = getRequiredNotificationPermissions();
        if (permissions.length > 0) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_NOTIFICATION_PERMISSION);
        }
    }
    
    /**
     * Requests all necessary permissions for the app.
     */
    public void requestAllPermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Add storage permissions if needed
        if (!hasStoragePermissions(activity)) {
            String[] storagePermissions = getRequiredStoragePermissions();
            for (String permission : storagePermissions) {
                permissionsToRequest.add(permission);
            }
        }
        
        // Add notification permissions if needed
        if (!hasNotificationPermissions(activity)) {
            String[] notificationPermissions = getRequiredNotificationPermissions();
            for (String permission : notificationPermissions) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            String[] permissions = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_ALL_PERMISSIONS);
        }
    }    
   
 /**
     * Checks if a specific permission is granted.
     */
    public boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Checks if multiple permissions are granted.
     */
    public boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the list of denied permissions from a permission array.
     */
    public String[] getDeniedPermissions(Context context, String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[0]);
    }
    
    /**
     * Checks if the user has permanently denied a permission (should show rationale).
     */
    public boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    /**
     * Handles the result of a permission request.
     */
    public PermissionResult handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 0 || grantResults.length == 0) {
            return new PermissionResult(requestCode, false, new String[0], new String[0]);
        }
        
        List<String> grantedPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();
        
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            } else {
                deniedPermissions.add(permissions[i]);
            }
        }
        
        boolean allGranted = deniedPermissions.isEmpty();
        
        return new PermissionResult(
            requestCode,
            allGranted,
            grantedPermissions.toArray(new String[0]),
            deniedPermissions.toArray(new String[0])
        );
    }
    
    /**
     * Checks if the app can write to external storage (for PDF generation).
     */
    public boolean canWriteToExternalStorage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage - can always write to app-specific directories
            return true;
        } else {
            return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
    
    /**
     * Checks if the app can read from external storage.
     */
    public boolean canReadFromExternalStorage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            return hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                   hasPermission(context, Manifest.permission.READ_MEDIA_VIDEO) ||
                   hasPermission(context, Manifest.permission.READ_MEDIA_AUDIO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    
    /**
     * Gets a user-friendly permission name for display.
     */
    public String getPermissionDisplayName(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage Access";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage Write Access";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notifications";
            case Manifest.permission.READ_MEDIA_IMAGES:
                return "Media Images Access";
            case Manifest.permission.READ_MEDIA_VIDEO:
                return "Media Video Access";
            case Manifest.permission.READ_MEDIA_AUDIO:
                return "Media Audio Access";
            default:
                return permission.substring(permission.lastIndexOf('.') + 1);
        }
    }
    
    /**
     * Result class for permission requests.
     */
    public static class PermissionResult {
        private final int requestCode;
        private final boolean allGranted;
        private final String[] grantedPermissions;
        private final String[] deniedPermissions;
        
        public PermissionResult(int requestCode, boolean allGranted, 
                              String[] grantedPermissions, String[] deniedPermissions) {
            this.requestCode = requestCode;
            this.allGranted = allGranted;
            this.grantedPermissions = grantedPermissions;
            this.deniedPermissions = deniedPermissions;
        }
        
        public int getRequestCode() { return requestCode; }
        public boolean areAllGranted() { return allGranted; }
        public String[] getGrantedPermissions() { return grantedPermissions; }
        public String[] getDeniedPermissions() { return deniedPermissions; }
        public boolean hasGrantedPermissions() { return grantedPermissions.length > 0; }
        public boolean hasDeniedPermissions() { return deniedPermissions.length > 0; }
    }
}