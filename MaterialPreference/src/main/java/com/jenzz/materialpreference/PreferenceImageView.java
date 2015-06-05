package com.jenzz.materialpreference;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.getMode;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static java.lang.Integer.MAX_VALUE;

/**
 * Extension of ImageView that correctly applies maxWidth and maxHeight.
 */
public class PreferenceImageView extends ImageView {

  private int maxWidth = MAX_VALUE;
  private int maxHeight = MAX_VALUE;

  public PreferenceImageView(Context context) {
    super(context);
  }

  public PreferenceImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(LOLLIPOP)
  public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void setMaxWidth(int maxWidth) {
    super.setMaxWidth(maxWidth);
    this.maxWidth = maxWidth;
  }

  @Override
  public void setMaxHeight(int maxHeight) {
    super.setMaxHeight(maxHeight);
    this.maxHeight = maxHeight;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = getMode(widthMeasureSpec);
    if (widthMode == AT_MOST || widthMode == UNSPECIFIED) {
      int widthSize = getSize(widthMeasureSpec);
      if (maxWidth != MAX_VALUE && (maxWidth < widthSize || widthMode == UNSPECIFIED)) {
        widthMeasureSpec = makeMeasureSpec(maxWidth, AT_MOST);
      }
    }

    int heightMode = getMode(heightMeasureSpec);
    if (heightMode == AT_MOST || heightMode == UNSPECIFIED) {
      int heightSize = getSize(heightMeasureSpec);
      if (maxHeight != MAX_VALUE && (maxHeight < heightSize || heightMode == UNSPECIFIED)) {
        heightMeasureSpec = makeMeasureSpec(maxHeight, AT_MOST);
      }
    }

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}