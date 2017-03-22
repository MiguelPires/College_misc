public class TestMethod1 {
	public static void main(String[] args) {
		System.err.println(new MethodWidget1());

		MethodWidget1 rw = new MethodWidget1();
		rw.someCall();
		System.err.println(rw);
		
		System.err.println(new MethodWidget1("a", 3));

		MethodWidget1 rw0 = new MethodWidget1("a", 3);
		rw0.someCall("b", 33);
		System.err.println(rw0);

		System.err.println(new MethodWidget1("a", 3, "b", 4));
		
		MethodWidget1 rw1 = new MethodWidget1("a", 3, "b", 4);
		rw1.someCall();
		System.err.println(rw1);
	}
}
