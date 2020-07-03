package test;

import ist.meic.pa.KeywordArgs;

public class InvertedAssignWidget {
	int a;
	int b;

	@KeywordArgs("b=a,a=1")
	public InvertedAssignWidget(Object... args) {}

	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
