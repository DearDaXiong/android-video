package tinno.com.videoplayer50.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import tinno.com.videoplayer50.presenter.BasePresenter;

/**
 * Created by xiaoyao on 2017/7/31.
 */

public abstract class BaseMVPActivity<V, T extends BasePresenter<V>> extends Activity {
    protected T mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        mPresenter.attachView((V) this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    protected abstract T createPresenter();
}
