package com.heysound.superstar.module.my;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.heysound.superstar.R;
import com.heysound.superstar.util.Helper;

/**
 * Created by lufei on 2016/3/15.
 * 键值对View icon + text + text + arrow
 */
public class KeyValueView extends LinearLayout {
    private DisplayMetrics dm;
    private ImageView imgIcon;
    private TextView tvKey;
    private TextView tvValue;
    private ImageView imgArrow;

    private View divider;

    public KeyValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dm = context.getResources().getDisplayMetrics();
        LayoutInflater.from(context).inflate(R.layout.mine_key_value_view, this);

        imgIcon = (ImageView) findViewById(R.id.kv_icon);
        tvKey = (TextView) findViewById(R.id.kv_key);
        tvValue = (TextView) findViewById(R.id.kv_value);
        imgArrow = (ImageView) findViewById(R.id.kv_arrow);
        divider = findViewById(R.id.kv_divider);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KeyValueView);

        initIcon(ta);
        initKey(ta);
        initValue(ta);
        initArrow(ta);

        boolean isShowDivider = ta.getBoolean(R.styleable.KeyValueView_kv_show_divider, false);
        if (isShowDivider) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }

        ta.recycle();
    }

    private void initIcon(TypedArray ta) {
        boolean isShowIcon = ta.getBoolean(R.styleable.KeyValueView_kv_show_icon, true);
        if (isShowIcon) {
            int icon = ta.getResourceId(R.styleable.KeyValueView_kv_icon, 0);
            int iconLeftMargin = (int) ta.getDimension(R.styleable.KeyValueView_kv_icon_left_margin, 15f);
            iconLeftMargin = (int) Helper.calcuteDp(iconLeftMargin, getContext());
            imgIcon.setImageResource(icon);
            MarginLayoutParams lp = (MarginLayoutParams) imgIcon.getLayoutParams();
            lp.leftMargin = iconLeftMargin;
            imgIcon.setLayoutParams(lp);
        } else {
            imgIcon.setVisibility(GONE);
        }
    }

    private void initKey(TypedArray ta) {
        String key = ta.getString(R.styleable.KeyValueView_kv_key);
        tvKey.setText(key);

        int keyTextSize = (int) ta.getDimension(R.styleable.KeyValueView_kv_key_text_size, 15f);
        tvKey.setTextSize(TypedValue.COMPLEX_UNIT_DIP, keyTextSize);

        int keyLeftMargin = (int) ta.getDimension(R.styleable.KeyValueView_kv_key_left_margin, 15f);
        keyLeftMargin = (int) Helper.calcuteDp(keyLeftMargin, getContext());
        MarginLayoutParams lp = (MarginLayoutParams) tvKey.getLayoutParams();
        lp.leftMargin = keyLeftMargin;
        tvKey.setLayoutParams(lp);
    }

    private void initValue(TypedArray ta) {
        String value = ta.getString(R.styleable.KeyValueView_kv_value);
        tvValue.setText(value);

        int valueTextSize = (int) ta.getDimension(R.styleable.KeyValueView_kv_key_text_size, 15f);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, valueTextSize);

        int valueRightMargin = (int) ta.getDimension(R.styleable.KeyValueView_kv_value_right_margin, 15f);
        valueRightMargin = (int) Helper.calcuteDp(valueRightMargin, getContext());
        MarginLayoutParams lp = (MarginLayoutParams) tvValue.getLayoutParams();
        lp.rightMargin = valueRightMargin;
        tvValue.setLayoutParams(lp);
    }

    private void initArrow(TypedArray ta) {
        boolean isShowArrow = ta.getBoolean(R.styleable.KeyValueView_kv_show_arrow, true);
        if (isShowArrow) {
            int valueRightMargin = (int) ta.getDimension(R.styleable.KeyValueView_kv_arrow_right_margin, 15f);
            valueRightMargin = (int) Helper.calcuteDp(valueRightMargin, getContext());
            MarginLayoutParams lp = (MarginLayoutParams) imgArrow.getLayoutParams();
            lp.rightMargin = valueRightMargin;
            imgArrow.setLayoutParams(lp);
        } else {
            imgArrow.setVisibility(View.GONE);
        }
    }

    public void seKey(String key) {
        tvKey.setText(key);
    }

    public void setValue(String value) {
        tvValue.setText(value);
    }

    public String getValue() {
        return tvValue.getText().toString();
    }

}
