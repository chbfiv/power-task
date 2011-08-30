package com.mtelab.tasktags.activities;

import com.mtelab.tasktags.drawables.ColorStrip;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ColorStripItem extends ImageView {
	
	public ColorStripItem(Context context, int color, float scale) {
		super(context);
		ColorStrip strip = new ColorStrip(color, scale);
		setImageDrawable(strip);
		LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		                  								LinearLayout.LayoutParams.FILL_PARENT,
		                  								1.0f);
		setBackgroundColor(Color.TRANSPARENT);		
		setLayoutParams( lp );
	}
	
	public ColorStripItem(Context context) {
		super(context);		
		LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.FILL_PARENT,
					1.0f);
		setBackgroundColor(Color.TRANSPARENT);		
		setLayoutParams( lp );
	}
}
