package com.mtelab.tasktags.drawables;

import android.graphics.drawable.GradientDrawable;

public class ColorStrip extends GradientDrawable {
	
	public ColorStrip(int color, float scale)
	{
		setColor(color);
		setShape(GradientDrawable.RECTANGLE);
		int size = (int) (36 * scale);
		setSize(6, size);
	}
}