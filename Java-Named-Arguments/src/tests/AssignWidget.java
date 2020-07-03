package test;

import ist.meic.pa.KeywordArgs;

public class AssignWidget {
	int a;
	int b;
	Integer ig;

	@KeywordArgs("a=1,b=a,a=2,ig=new Integer(3)")
	public AssignWidget(Object... args) {}

	public String toString() {
		return String.format("a: %s, b: %s, ig: %s",
				a, b, ig);
	}
}
