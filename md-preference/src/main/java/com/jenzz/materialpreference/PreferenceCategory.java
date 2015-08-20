package com.jenzz.materialpreference;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.jenzz.materialpreference.ThemeUtils.resolveAccentColor;
import static com.jenzz.materialpreference.Typefaces.getRobotoMedium;

public class PreferenceCategory extends android.preference.PreferenceCategory {

  private int accentColor;

  public PreferenceCategory(Context context) {
    super(context);
    init();
  }

  public PreferenceCategory(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(LOLLIPOP)
  public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    accentColor = resolveAccentColor(getContext());
  }

  @Override
  protected View onCreateView(ViewGroup parent) {
    LayoutInflater layoutInflater =
        (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    return layoutInflater.inflate(R.layout.mp_preference_category, parent, false);
  }

  @Override
  protected void onBindView(View view) {
    super.onBindView(view);

    CharSequence title = getTitle();
    TextView titleView = (TextView) view.findViewById(R.id.title);
    titleView.setText(title);
    titleView.setTextColor(accentColor);
    titleView.setVisibility(!isEmpty(title) ? VISIBLE : GONE);
    titleView.setTypeface(getRobotoMedium(getContext()));
  }
}
