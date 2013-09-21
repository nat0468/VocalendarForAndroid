/*
 * Copyright (C) 2011 Sergey Margaritov
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

package net.margaritov.preference.colorpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A preference type that allows a user to choose a time
 * @author Sergey Margaritov
 */
public class ColorPickerPreference
	extends
		Preference
	implements
		Preference.OnPreferenceClickListener,
		ColorPickerDialog.OnColorChangedListener,
		CompoundButton.OnCheckedChangeListener {

	View mView;
	ColorPickerDialog mDialog;
	boolean mShowCheckbox = false;
	boolean mPickerEnabled = false;
	int mDefaultValue = Color.BLACK;
	private int mValue = Color.BLACK;
	private float mDensity = 0;
	private boolean mAlphaSliderEnabled = false;
	private boolean mHexValueEnabled = false;

	private static final String androidns = "http://schemas.android.com/apk/res/android";

	public ColorPickerPreference(Context context) {
		super(context);
		init(context, null);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue)
			mPickerEnabled = !mShowCheckbox || getSharedPreferences().getBoolean(getKey() + "_enabled", mPickerEnabled);

		onColorChanged(restoreValue ? getValue() : mDefaultValue);
	}

	private void init(Context context, AttributeSet attrs) {
		mDensity = getContext().getResources().getDisplayMetrics().density;
		setOnPreferenceClickListener(this);
		if (attrs != null) {
			String defaultValue = attrs.getAttributeValue(androidns, "defaultValue");
			if (defaultValue == null || defaultValue.isEmpty()) {
				mDefaultValue = Color.BLACK;
			} else if (defaultValue.startsWith("#")) {
				try {
					mDefaultValue = convertToColorInt(defaultValue);
				} catch (NumberFormatException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					mDefaultValue = convertToColorInt("#FF000000");
				}
			} else {
				int resourceId = attrs.getAttributeResourceValue(androidns, "defaultValue", 0);
				if (resourceId != 0) {
					mDefaultValue = context.getResources().getInteger(resourceId);
				}
			}
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
			mHexValueEnabled = attrs.getAttributeBooleanValue(null, "hexValue", false);
			mShowCheckbox = attrs.getAttributeBooleanValue(null, "showCheckbox", false);
			mPickerEnabled = !mShowCheckbox || attrs.getAttributeBooleanValue(null, "enabledByDefault", false);
		}
		mValue = mDefaultValue;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mView = view;
		persistBothValues();
		setPreviewColor();
	}

	private void setPreviewColor() {
		if (mView == null) return;
		
		CheckBox cbPickerEnabled = null;
		if (mShowCheckbox) {
			cbPickerEnabled = new CheckBox(getContext());
			cbPickerEnabled.setFocusable(false);
			cbPickerEnabled.setEnabled(super.isEnabled());
			cbPickerEnabled.setChecked(mPickerEnabled);
			cbPickerEnabled.setOnCheckedChangeListener(this);
		}
		
		ImageView iView = new ImageView(getContext());
		LinearLayout widgetFrameView = ((LinearLayout)mView.findViewById(android.R.id.widget_frame));
		if (widgetFrameView == null) return;
		widgetFrameView.setVisibility(View.VISIBLE);
		widgetFrameView.setPadding(
			widgetFrameView.getPaddingLeft(),
			widgetFrameView.getPaddingTop(),
			(int)(mDensity * 8),
			widgetFrameView.getPaddingBottom()
		);
		// remove already create preview image
		int count = widgetFrameView.getChildCount();
		if (count > 0) {
			widgetFrameView.removeViews(0, count);
		}
		if (mShowCheckbox) {
			widgetFrameView.setOrientation(LinearLayout.HORIZONTAL);
			widgetFrameView.addView(cbPickerEnabled);
		}
		widgetFrameView.addView(iView);
		widgetFrameView.setMinimumWidth(0);
		iView.setBackgroundDrawable(new AlphaPatternDrawable((int)(5 * mDensity)));
		iView.setImageBitmap(getPreviewBitmap());
	}

	private Bitmap getPreviewBitmap() {
		int d = (int) (mDensity * 31); //30dip
		int color = getValue();
		Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		int w = bm.getWidth();
		int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++) {
			for (int j = i; j < h; j++) {
				c = (i <= 1 || j <= 1 || i >= w-2 || j >= h-2) ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				if (i != j) {
					bm.setPixel(j, i, c);
				}
			}
		}

		return bm;
	}

	public int getValue() {
		try {
			if (shouldPersist()) {
				mValue = getPersistedInt(mDefaultValue);
			}
		} catch (ClassCastException e) {
			mValue = mDefaultValue;
		}

		return mValue;
	}

	@Override
	public void onColorChanged(int color) {
		mValue = color;
		persistBothValues();
		setPreviewColor();
		notifyChanged();
	}
	

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mPickerEnabled = isChecked; 
		persistBothValues();
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
	}
	
    protected void persistBothValues() {
    	if (shouldPersist()) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(getKey(), mValue);
            if (mShowCheckbox)
            	editor.putBoolean(getKey() + "_enabled", mPickerEnabled);
            if (shouldCommit()) {
                try {
                    editor.apply();
                } catch (AbstractMethodError unused) {
                    editor.commit();
                }
            }
		}
    }
    
    @Override
    public boolean isEnabled() {
    	return super.isEnabled() && mPickerEnabled;
    }

	public boolean onPreferenceClick(Preference preference) {
		showDialog(null);
		return false;
	}
	
	protected void showDialog(Bundle state) {
		mDialog = new ColorPickerDialog(getContext(), getValue());
		mDialog.setOnColorChangedListener(this);
		if (mAlphaSliderEnabled) {
			mDialog.setAlphaSliderVisible(true);
		}
		if (mHexValueEnabled) {
			mDialog.setHexValueEnabled(true);
		}
		if (state != null) {
			mDialog.onRestoreInstanceState(state);
		}
		mDialog.show();
	}

	/**
	 * Toggle Hex Value visibility (by default it's disabled)
	 * @param enable
	 */
	public void setHexValueEnabled(boolean enable) {
		mHexValueEnabled = enable;
	}

	/**
	 * Toggle Alpha Slider visibility (by default it's disabled)
	 * @param enable
	 */
	public void setAlphaSliderEnabled(boolean enable) {
		mAlphaSliderEnabled = enable;
	}

	/**
	 * For custom purposes. Not used by ColorPickerPreferrence
	 * @param color
	 * @author Unknown
	 */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }
    
    /**
	 * For custom purposes. Not used by ColorPickerPreference
	 * @param color
	 * @author Charles Rosaaen
	 * @return A string representing the hex value of color,
	 * without the alpha value
	 */
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + red + green + blue;
    }
    
    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

    	if (argb.startsWith("#")) {
    		argb = argb.replace("#", "");
    	}

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState myState = new SavedState(superState);
        if (mDialog != null && mDialog.isShowing())
        	myState.dialogBundle = mDialog.onSaveInstanceState();
        myState.pickerEnabled = mPickerEnabled;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mPickerEnabled = myState.pickerEnabled;
        if (myState.dialogBundle != null)
        	showDialog(myState.dialogBundle);
    }

    private static class SavedState extends BaseSavedState {
        Bundle dialogBundle;
        boolean pickerEnabled;
        
        public SavedState(Parcel source) {
            super(source);
            dialogBundle = source.readBundle();
            pickerEnabled = source.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(dialogBundle);
            dest.writeInt(pickerEnabled ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
        
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}