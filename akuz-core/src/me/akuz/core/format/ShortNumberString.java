package me.akuz.core.format;

import java.text.DecimalFormat;

public class ShortNumberString {
	
	private final static DecimalFormat _fmt_0 = new DecimalFormat("0");
	private final static DecimalFormat _fmt_0_0 = new DecimalFormat("0.#");
	
	private final static double b1  = 1000000000;
	private final static double m10 =   10000000;
	private final static double m1  =    1000000;
	private final static double k10 =      10000;
	private final static double k1  =       1000;
	
	public final static String format(Double number) {
		
		double ceil = Math.ceil(number);
		
		if (ceil >= b1) {
			return "1B+";
		} else if (ceil >= m10) {
			double fraq = ceil / m1;
			int fraqInt = (int)fraq;
			if (fraqInt == 1000) {
				return "1B";
			} else {
				return String.format("%sM", _fmt_0.format(fraq));
			}
		} else if (ceil >= m1) {
			return String.format("%sM", _fmt_0_0.format(ceil / m1));
		} else if (ceil >= k10) {
			double fraq = ceil / k1;
			int fraqInt = (int)fraq;
			if (fraqInt == 1000) {
				return "1M";
			} else {
				return String.format("%sK", _fmt_0.format(fraq));
			}
		} else if (ceil >= k1) {
			return String.format("%sK", _fmt_0_0.format(ceil / k1));
		} else {
			return String.format("%s", _fmt_0.format(ceil));
		}
	}

}
