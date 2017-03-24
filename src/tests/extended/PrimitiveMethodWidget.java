import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class PrimitiveMethodWidget {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=2")
	public PrimitiveMethodWidget(Object... args) {}

	@KeywordArgsExtended("c=8,d=9,e=-0.5,e=3.3")
	public void someCall(Object... args){
		Object c = 1;
		Object d = 2;
		Object e = 3;

		System.out.println("c: "+c+", d: "+d +", e: "+e);		
	}
	
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
