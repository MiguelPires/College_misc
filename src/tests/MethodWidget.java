import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class MethodWidget {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=2")
	public MethodWidget(Object... args) {}

	@KeywordArgsExtended("a=8,b=9")
	public void someCall(Object... args){}
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
