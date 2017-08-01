package tinno.com.videoplayer50.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import tinno.com.videoplayer50.R;
import tinno.com.videoplayer50.utils.PermissionUtil;

/**
 * Created by xiaoyao on 2017/8/1.
 */

public abstract class AppActivity extends AppCompatActivity implements PermissionUtil.PermissionListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String TAG = "daxiong";
    private static final String permissionString = Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        new PermissionUtil(this).checkPermissions(this, permissionString, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onActivityPermissionGranted();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: ");
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    onActivityPermissionDenied();
                    
                }
            }
        }
    }

    @Override
    public void onPermissionGranted() {
        onActivityPermissionGranted();
    }

    @Override
    public void onPermissionExplained() {
        onActivityPermissionDenied();
        Log.d(TAG, "onPermissionExplained: ");
    }

    protected abstract void onActivityPermissionGranted();
    
    protected abstract void onActivityPermissionDenied();
}
