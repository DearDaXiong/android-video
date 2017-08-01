package tinno.com.videoplayer50.presenter;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by xiaoyao on 2017/7/30.
 */

public abstract class BasePresenter<T> {
    protected Reference<T> mViewRef;

    public void attachView(T view) {
        mViewRef = new WeakReference<T>(view);
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }


    protected T getView() {
        return mViewRef.get();
    }

    public boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

}
