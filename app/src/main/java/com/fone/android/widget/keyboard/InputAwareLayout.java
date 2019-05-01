package com.fone.android.widget.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class InputAwareLayout extends KeyboardAwareLinearLayout implements KeyboardAwareLinearLayout.OnKeyboardShownListener {
    private InputView current;

    public InputAwareLayout(Context context) {
        this(context, null);
    }

    public InputAwareLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputAwareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnKeyboardShownListener(this);
    }



    public void show(@NonNull final EditText imeTarget, @NonNull final InputView input) {
        if (isKeyboardOpen()) {
            hideSoftKey(imeTarget, () -> {
                hideAttachedInput(true);
                input.show(getKeyboardHeight(), true);
                current = input;
            });
        } else {
            if (current != null) current.hide(true);
            input.show(getKeyboardHeight(), current != null);
            current = input;
        }
    }

    @SuppressWarnings("unused")
    public InputView getCurrentInput() {
        return current;
    }

    @SuppressWarnings("unused")
    public void hideCurrentInput(EditText imeTarget) {
        if (isKeyboardOpen()) hideSoftKey(imeTarget, null);
        else hideAttachedInput(false);
    }

    public void hideAttachedInput(boolean instant) {
        if (current != null) current.hide(instant);
        current = null;
    }

    public boolean isInputOpen() {
        return (isKeyboardOpen() || (current != null && current.isShowing()));
    }

    @SuppressWarnings("unused")
    public void showSoftKey(final EditText inputTarget) {
        postOnKeyboardOpen(() -> hideAttachedInput(true));
        inputTarget.post(() -> {
            inputTarget.requestFocus();
            ((InputMethodManager) inputTarget.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(inputTarget, 0);
        });
    }

    private void hideSoftKey(final EditText inputTarget, @Nullable Runnable runAfterClose) {
        if (runAfterClose != null) postOnKeyboardClose(runAfterClose);

        ((InputMethodManager) inputTarget.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(inputTarget.getWindowToken(), 0);
    }

    @Override
    public void onKeyboardShown() {

    }

    public interface InputView {
        void show(int height, boolean immediate);

        void hide(boolean immediate);

        boolean isShowing();
    }
}

