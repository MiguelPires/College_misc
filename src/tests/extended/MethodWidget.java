import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class MethodWidget {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=2")
	public MethodWidget(Object... args) {}

	@KeywordArgsExtended("c=new Integer(8),d=new Integer(9),"+
		"e=new Float(-0.5),e=new Float(3.3),s=\"someString\"")
	public void someCall(Object... args){
		Object c = 1;
		Object d = 2;
		Object e = 3;
		Object s = "anotherString";

		System.out.println("c: "+c+", d: "+d +", e: "+e+", s: "+s);		
	}
	
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
