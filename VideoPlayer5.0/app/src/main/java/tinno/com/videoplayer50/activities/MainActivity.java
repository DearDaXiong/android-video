package tinno.com.videoplayer50.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import tinno.com.videoplayer50.R;
import tinno.com.videoplayer50.fragments.NoPermissionFragment;

/**
 * Created by xiaoyao on 2017/8/1.
 */

public class MainActivity extends AppActivity {
    private static final String TAG = "daxiong";

    @Override
    protected void onActivityPermissionGranted() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onActivityPermissionDenied() {
        Log.d(TAG, "onActivityPermissionDenied: ");
        Fragment noPermissionFragment = NoPermissionFragment.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, noPermissionFragment);
        transaction.commitAllowingStateLoss();
    }
}
