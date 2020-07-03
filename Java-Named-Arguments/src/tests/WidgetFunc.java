package test;

import ist.meic.pa.KeywordArgs;

public class WidgetFunc {
	int a;
	int b;
	
	@KeywordArgs("a=addFunc(1,1),b=addFunc(-8,9)")
	public WidgetFunc(Object... args) {}

	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}

	public static int addFunc(int x, int y) {
		return x + y;
	}
}
