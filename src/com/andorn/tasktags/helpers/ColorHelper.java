package com.andorn.tasktags.helpers;

import java.util.Random;

/*	Natural Color System (NCS)
 *	BCP-37 
 *	37 colors
 *	[dimensions of color appearance]
 * 	Hue
 * 		Red-Green
 * 		Blue-Yellow
 * 	Saturation (Chroma)
 * 	Brightness (Lightness, Valve)
 * 
 * 	
 *	8 Hues X 4 Cuts (Light-L, Saturated-S, Muted-M, Dark-D)
 *	5 Grays
 *	(unique)
 *		Red
 *		Green
 *		Blue
 *		Yellow
 *	(bisectors)
 *		Orange
 *		Purple
 *		Cyan
 *		Chartreuse	
 */
public class ColorHelper {
	
	public static int WHITE = 0xffffffff;
	public static int LIGHT_GRAY = 0xffe1e1e1; //0xffcccccc
	public static int GRAY = 0xffb6b6b6; //0xff888888
	public static int DARK_GRAY = 0xff787878; //0xff444444
	public static int BLACK = 0xff000000;

	public static int LIGHT_RED = 0xfff393b8;
	public static int SATURATED_RED = 0xffed2859;
	public static int MUTED_RED = 0xffcd768c;
	public static int DARK_RED = 0xffa31d41;
	
	public static int LIGHT_GREEN = 0xffbfe0c2;
	public static int SATURATED_GREEN = 0xff61be80;
	public static int MUTED_GREEN = 0xff80c67f;
	public static int DARK_GREEN = 0xff1d984e;
	
	public static int LIGHT_BLUE = 0xffa6ceed;
	public static int SATURATED_BLUE = 0xff5da1d8;
	public static int MUTED_BLUE = 0xff7b9dca;
	public static int DARK_BLUE = 0xff377cb6;

	public static int LIGHT_YELLOW = 0xfffdf39a;
	public static int SATURATED_YELLOW = 0xfffde500;
	public static int MUTED_YELLOW = 0xffdac671;
	public static int DARK_YELLOW = 0xffa28930;
	
	public static int LIGHT_ORANGE = 0xfffbc7a2;
	public static int SATURATED_ORANGE = 0xfff49122;
	public static int MUTED_ORANGE = 0xffd09974;
	public static int DARK_ORANGE = 0xff9f5a2a;
	
	public static int LIGHT_PURPLE = 0xffb79cc7;
	public static int SATURATED_PURPLE = 0xff9c4b9c;
	public static int MUTED_PURPLE = 0xffa271a7;
	public static int DARK_PURPLE = 0xff743492;
	
	public static int LIGHT_CYAN = 0xffa1dae4;
	public static int SATURATED_CYAN = 0xff50c4d0;
	public static int MUTED_CYAN = 0xff81cbd0;
	public static int DARK_CYAN = 0xff029a9a;
	
	public static int LIGHT_CHARTREUSE = 0xffe0e794;
	public static int SATURATED_CHARTREUSE = 0xffb1d135;
	public static int MUTED_CHARTREUSE = 0xffc2c855;
	public static int DARK_CHARTREUSE = 0xff7d983e;
	
	public static int getRandomColor()
	{		
		int color = BLACK;
		Random generator = new Random(System.currentTimeMillis());
		int colorIndex = generator.nextInt(37) + 1;
		
		switch(colorIndex)
		{
		case 1: color = WHITE; break;
		case 2: color = LIGHT_GRAY; break;
		case 3: color = GRAY; break;
		case 4: color = DARK_GRAY; break;
		case 5: color = BLACK; break;
		case 6: color = LIGHT_RED; break;
		case 7: color = SATURATED_RED; break;
		case 8: color = MUTED_RED; break;
		case 9: color = DARK_RED; break;
		case 10: color = LIGHT_GREEN; break;
		case 11: color = SATURATED_GREEN; break;
		case 12: color = MUTED_GREEN; break;
		case 13: color = DARK_GREEN; break;
		case 14: color = LIGHT_BLUE; break;
		case 15: color = SATURATED_BLUE; break;
		case 16: color = MUTED_BLUE; break;
		case 17: color = DARK_BLUE; break;
		case 18: color = LIGHT_YELLOW; break;
		case 19: color = SATURATED_YELLOW; break;
		case 20: color = MUTED_YELLOW; break;
		case 21: color = DARK_YELLOW; break;
		case 22: color = LIGHT_ORANGE; break;
		case 23: color = SATURATED_ORANGE; break;
		case 24: color = MUTED_ORANGE; break;
		case 25: color = DARK_ORANGE; break;
		case 26: color = LIGHT_PURPLE; break;
		case 27: color = SATURATED_PURPLE; break;
		case 28: color = MUTED_PURPLE; break;
		case 29: color = DARK_PURPLE; break;
		case 30: color = LIGHT_CYAN; break;
		case 31: color = SATURATED_CYAN; break;
		case 32: color = MUTED_CYAN; break;
		case 33: color = DARK_CYAN; break;
		case 34: color = LIGHT_CHARTREUSE; break;
		case 35: color = SATURATED_CHARTREUSE; break;
		case 36: color = MUTED_CHARTREUSE; break;
		case 37: color = DARK_CHARTREUSE; break;
		}
		return color;
	}
}
