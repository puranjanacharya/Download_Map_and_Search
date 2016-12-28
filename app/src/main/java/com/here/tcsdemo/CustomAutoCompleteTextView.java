package com.here.tcsdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

/**
 * Wrapper over AutoCompleteTextView to introduce some delay and to avoid multiple request for each
 * character.
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    // id
    private static final int MESSAGE_TEXT_CHANGED = 100;
    // delay for making network request, default, not changeable.
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 200;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CustomAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };
    // configurable delay, if not configured initially - will be set to default.
    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
    // ProgressBar
    private ProgressBar mLoadingIndicator;

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set loading indicator.
     *
     * @param progressBar
     */
    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    /**
     * Set delay
     *
     * @param autoCompleteDelay
     */
    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text),
                mAutoCompleteDelay);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }
        super.onFilterComplete(count);
    }
}