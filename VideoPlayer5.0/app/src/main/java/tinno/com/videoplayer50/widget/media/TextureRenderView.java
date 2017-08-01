package tinno.com.videoplayer50.widget.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tinno.com.videoplayer50.mediaPlayer.IMediaPlayer;

/**
 * @author 吴雄峰
 * @date 17-7-25
 * @time 上午11:32
 * @description
 **/

public class TextureRenderView extends TextureView implements IRenderView {
    private static final String TAG = "TextureRenderView";

    private MeasureHelper mMeasureHelper;
    private SurfaceCallback mSurfaceCallback;

    public TextureRenderView(Context context) {
        this(context, null);
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback(this);
        setSurfaceTextureListener(mSurfaceCallback);
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    public View getView() {
        return this;
    }

    //--------------------
    // Layout & Measure
    //--------------------

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarDen > 0 && videoSarNum > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    @Override
    public void setAspectRatio(int aspectRadio) {
        mMeasureHelper.setAspectRatio(aspectRadio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasureWidth(), mMeasureHelper.getMeasureHeight());
    }


    //--------------------
    // TextureViewHolder
    //--------------------

    public IRenderView.ISurfaceHolder getSurfaceHolder() {
        return new InternalSurfaceHolder(this, mSurfaceCallback.mSurfaceTexture, mSurfaceCallback);
    }

    private static final class InternalSurfaceHolder implements IRenderView.ISurfaceHolder {

        private TextureRenderView mTextureView;
        private SurfaceTexture mSurfaceTexture;
        private ISurfaceTextureHost mSurfaceTextureHost;

        public InternalSurfaceHolder(@NonNull TextureRenderView textureView,
                                     @Nullable SurfaceTexture surfaceTexture,
                                     @NonNull ISurfaceTextureHost surfaceTextureHost) {
            mTextureView = textureView;
            mSurfaceTexture = surfaceTexture;
            mSurfaceTextureHost = surfaceTextureHost;
        }

        @Override
        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp == null)
                return;
            mp.setSurface(openSurface());
        }

        @NonNull
        @Override
        public IRenderView getRenderView() {
            return mTextureView;
        }

        @Nullable
        @Override
        public SurfaceHolder getSurfaceHolder() {
            return null;
        }

        @Nullable
        @Override
        public Surface openSurface() {
            return (mSurfaceTexture == null) ? null : new Surface(mSurfaceTexture);
        }

        @Nullable
        @Override
        public SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }
    }


    @Override
    public void addRenderCallback(@NonNull IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public void removeRenderCallback(@NonNull IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);

    }

    private final static class SurfaceCallback implements TextureView.SurfaceTextureListener, ISurfaceTextureHost {

        private SurfaceTexture mSurfaceTexture;
        private boolean mIsFormatChanged;
        private int mWidth;
        private int mHeight;

        private boolean mOwnSurfaceTexture = true;
        private boolean mWillDetachFromWindow = false;
        private boolean mDidDetachFromWindow = false;

        private WeakReference<TextureRenderView> mWeakRenderView;
        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<IRenderCallback, Object>();

        public SurfaceCallback(@NonNull TextureRenderView renderView) {
            mWeakRenderView = new WeakReference<TextureRenderView>(renderView);
        }

        public void setOwnSurfaceTexture(boolean ownSurfaceTexture) {
            mOwnSurfaceTexture = ownSurfaceTexture;
        }

        public void addRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceTexture != null) {
                if (surfaceHolder == null)
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), mSurfaceTexture, this);
                callback.onSurfaceCreated(surfaceHolder, mWidth, mHeight);
            }

            if (mIsFormatChanged) {
                if (surfaceHolder == null)
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), mSurfaceTexture, this);
                callback.onSurfaceChanged(surfaceHolder, 0, mWidth, mHeight);
            }
        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            Log.d(TAG, "onSurfaceTextureAvailable: ");
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface, this);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            mIsFormatChanged = true;
            mWidth = width;
            mHeight = height;

            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface, this);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(surfaceHolder, 0, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceTexture = surface;
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface, this);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }

            return mOwnSurfaceTexture;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        //-------------------------
        // ISurfaceTextureHost
        //-------------------------

//        @Override
//        public void releaseSurfaceTexture(SurfaceTexture surfaceTexture) {
//            if (surfaceTexture == null) {
//                Log.d(TAG, "releaseSurfaceTexture: null");
//            } else if (mDidDetachFromWindow) {
//                if (surfaceTexture != mSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: didDetachFromWindow(): release different SurfaceTexture");
//                    surfaceTexture.release();
//                } else if (!mOwnSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: didDetachFromWindow(): release detached SurfaceTexture");
//                    surfaceTexture.release();
//                } else {
//                    Log.d(TAG, "releaseSurfaceTexture: didDetachFromWindow(): already released by TextureView");
//                }
//            } else if (mWillDetachFromWindow) {
//                if (surfaceTexture != mSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: willDetachFromWindow(): release different SurfaceTexture");
//                    surfaceTexture.release();
//                } else if (!mOwnSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: willDetachFromWindow(): re-attach SurfaceTexture to TextureView");
//                    setOwnSurfaceTexture(true);
//                } else {
//                    Log.d(TAG, "releaseSurfaceTexture: willDetachFromWindow(): will released by TextureView");
//                }
//            } else {
//                if (surfaceTexture != mSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: alive: release different SurfaceTexture");
//                    surfaceTexture.release();
//                } else if (!mOwnSurfaceTexture) {
//                    Log.d(TAG, "releaseSurfaceTexture: alive: re-attach SurfaceTexture to TextureView");
//                    setOwnSurfaceTexture(true);
//                } else {
//                    Log.d(TAG, "releaseSurfaceTexture: alive: will released by TextureView");
//                }
//            }
//        }

        public void willDetachFromWindow() {
            Log.d(TAG, "willDetachFromWindow()");
            mWillDetachFromWindow = true;
        }

        public void didDetachFromWindow() {
            Log.d(TAG, "didDetachFromWindow()");
            mDidDetachFromWindow = true;
        }
    }

    //--------------------
    // Accessibility
    //--------------------

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextureRenderView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextureRenderView.class.getName());
    }
}
