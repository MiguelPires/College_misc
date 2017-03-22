import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class MethodWidget2 {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=2")
	public MethodWidget2(Object... args) {}

	@KeywordArgsExtended("c=8,d=9")
	public void someCall(Object... args){
		Object c = null, d = null;
		System.out.println("c: "+c+", d: "+d);
	}
	
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
