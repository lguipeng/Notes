package com.jenzz.materialpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;

public class SwitchPreference extends TwoStatePreference {

  public SwitchPreference(Context context) {
    super(context);
    init(context, null, 0, 0);
  }

  public SwitchPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0, 0);
  }

  public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, 0);
  }

  public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs, defStyleAttr, defStyleRes);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[] {
        android.R.attr.summaryOn, android.R.attr.summaryOff, android.R.attr.disableDependentsState
    }, defStyleAttr, defStyleRes);

    setSummaryOn(typedArray.getString(0));
    setSummaryOff(typedArray.getString(1));
    setDisableDependentsState(typedArray.getBoolean(2, false));

    typedArray.recycle();

    setWidgetLayoutResource(R.layout.mp_switch_preference);
  }

  @Override
  protected void onBindView(View view) {
    super.onBindView(view);

    SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.switch_compat);
    switchCompat.setChecked(isChecked());

    syncSummaryView(view);
  }
}
