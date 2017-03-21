import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class InheritedMethodWidget {
	int a;
	int b;

	@KeywordArgsExtended("a=3,b=4")
	public InheritedMethodWidget(Object... args) {}

	@KeywordArgsExtended("b=10")
	public void someCall(Object... args){}
	
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
