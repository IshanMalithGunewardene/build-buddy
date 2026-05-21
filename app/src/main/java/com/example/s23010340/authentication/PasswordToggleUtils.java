package com.example.s23010340.authentication;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;
import androidx.appcompat.content.res.AppCompatResources;
import com.example.s23010340.R;

public final class PasswordToggleUtils {
    private PasswordToggleUtils() {
    }

    public static void attach(EditText passwordInput) {
        passwordInput.setTag(Boolean.FALSE);
        passwordInput.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            if (!isEndDrawableTapped(passwordInput, event)) {
                return false;
            }

            toggle(passwordInput);
            return true;
        });
    }

    private static boolean isEndDrawableTapped(EditText editText, MotionEvent event) {
        if (editText.getCompoundDrawablesRelative()[2] == null) {
            return false;
        }
        int drawableWidth = editText.getCompoundDrawablesRelative()[2].getBounds().width();
        float touchX = event.getX();
        int startOfDrawable = editText.getWidth() - editText.getPaddingEnd() - drawableWidth;
        return touchX >= startOfDrawable;
    }

    private static void toggle(EditText editText) {
        boolean isVisible = Boolean.TRUE.equals(editText.getTag());
        if (isVisible) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                editText.getCompoundDrawablesRelative()[0],
                editText.getCompoundDrawablesRelative()[1],
                AppCompatResources.getDrawable(editText.getContext(), R.drawable.ic_visibility_off),
                editText.getCompoundDrawablesRelative()[3]
            );
            editText.setTag(Boolean.FALSE);
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                editText.getCompoundDrawablesRelative()[0],
                editText.getCompoundDrawablesRelative()[1],
                AppCompatResources.getDrawable(editText.getContext(), R.drawable.ic_visibility),
                editText.getCompoundDrawablesRelative()[3]
            );
            editText.setTag(Boolean.TRUE);
        }
        editText.setSelection(editText.getText().length());
    }
}
