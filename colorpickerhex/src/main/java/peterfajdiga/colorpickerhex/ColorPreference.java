/*
 Originally by CyanogenMod
 Modified by Peter Fajdiga

 Original license follows:
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package peterfajdiga.colorpickerhex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ColorPreference extends DialogPreference {

    public static final int DEFAULT_COLOR = 0xff000000;

    private Resources mResources;

    private ImageView mColorPreview;

    private int mColorValue;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColorValue = getDefaultColor(context.getResources(), attrs);
        init();
    }

    public ColorPreference(Context context, int color) {
        super(context, null);
        mColorValue = color;
        init();
    }

    private int getDefaultColor(final Resources res, final AttributeSet attrs) {
        final int defaultColor;
        final int defaultValueStr = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "defaultValue", 0);
        if (defaultValueStr == 0) {
            defaultColor = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "defaultValue", DEFAULT_COLOR);
        } else {
            defaultColor = res.getInteger(defaultValueStr);
        }
        return defaultColor;
    }

    private void init() {
        setLayoutResource(R.layout.preference_color);
        mResources = getContext().getResources();
    }

//    public void onStart() {
//        ColorPickerDialog d = (ColorPickerDialog) getDialog();
//        if (d != null) {
//            d.onStart();
//        }
//    }
//
//    public void onStop() {
//        ColorPickerDialog d = (ColorPickerDialog) getDialog();
//        if (d != null) {
//            d.onStop();
//        }
//    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mColorPreview = (ImageView)view.findViewById(R.id.light_color);

        mColorValue = getPersistedInt(mColorValue);
        updatePreferenceViews();
    }

    private void updatePreferenceViews() {
        final int size = (int) mResources.getDimension(R.dimen.oval_notification_size);

        if (mColorPreview != null) {
            mColorPreview.setEnabled(true);

            // adjust if necessary to prevent material whiteout
            int imageColor = mColorValue;
            if ((imageColor & 0xF0F0F0F0) == 0xF0F0F0F0) {
                imageColor -= 0x101010;
            }

            mColorPreview.setImageDrawable(createOvalShape(size, imageColor));

            // display checkered background if translucent
            if ((imageColor & 0xF0000000) != 0xF0000000) {
                final float density = getContext().getResources().getDisplayMetrics().density;
                mColorPreview.setBackground(new AlphaPatternDrawable((int)(5 * density)));
            }

            notifyChanged();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        final ColorPickerDialog d = (ColorPickerDialog) getDialog();
    }

//    @Override
    protected Dialog createDialog() {
        final ColorPickerDialog d = new ColorPickerDialog(getContext(), mColorValue, true);
//        d.setAlphaSliderVisible(true);

        d.setButton(AlertDialog.BUTTON_POSITIVE, mResources.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setColor(d.getColor());
                        callChangeListener(this);
                    }
                });
        d.setButton(AlertDialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), (DialogInterface.OnClickListener)null);

        return d;
    }

    /**
     * Getters and Setters
     */

    public int getColor() {
        return mColorValue;
    }

    public void setColor(int color) {
        mColorValue = color;
        updatePreferenceViews();
        persistInt(color);
    }

    /**
     * Utility methods
     */
    private static ShapeDrawable createOvalShape(int size, int color) {
        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        shape.setIntrinsicHeight(size);
        shape.setIntrinsicWidth(size);
        shape.getPaint().setColor(color);
        return shape;
    }
}
