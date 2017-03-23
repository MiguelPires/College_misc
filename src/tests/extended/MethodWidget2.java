import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class MethodWidget2 {
	int a;
	int b;

	@KeywordArgsExtended("a=1,b=2")
	public MethodWidget2(Object... args) {}

	@KeywordArgsExtended("c=new Integer(8),d=new Integer(9),e=new Float(3.3)")
	public void someCall(Object... args){
		Object c = 0;
		Object d = 0;
		Object e = 1;

		System.out.println("c: "+c+", d: "+d +", e: "+e);		
	}
	
	public String toString() {
		return String.format("a: %s, b: %s",
				a, b);
	}
}
