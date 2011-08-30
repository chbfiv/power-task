package com.mtelab.tasktags.drawables;

import android.graphics.drawable.GradientDrawable;

public class ColorPicker extends GradientDrawable {
	
	public ColorPicker(int color, float scale)
	{
		setColor(color);
		setShape(GradientDrawable.RECTANGLE);
		float radii = 10.0f * scale;
		setCornerRadii(new float[] { radii, radii, radii, radii, radii, radii, radii, radii });
		int size = (int) (36 * scale);
		setSize(size, size);
		int width = (int) (2 * scale);
		setStroke(width, 0xff6899ff);
	}
}