package test.extended;
import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class RepeatedWidget {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=a,a=2")
	public RepeatedWidget(Object... args) {}

	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
