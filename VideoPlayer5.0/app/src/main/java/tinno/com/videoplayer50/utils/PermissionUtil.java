package tinno.com.videoplayer50.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by xiaoyao on 2017/8/1.
 */

public class PermissionUtil {
    public interface PermissionListener {
        void onPermissionGranted();

        void onPermissionExplained();
    }

    private PermissionListener listener;

    public PermissionUtil(PermissionListener listener) {
        this.listener = listener;
    }

    public void checkPermissions(Activity context, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                // TODO: show explanation
                listener.onPermissionExplained();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
            }
        } else {
            listener.onPermissionGranted();
        }
    }
}
