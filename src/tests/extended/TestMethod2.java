public class TestMethod2 {
	public static void main(String[] args) {
		System.err.println(new MethodWidget2());

		MethodWidget2 rw = new MethodWidget2();
		rw.someCall();
		
		System.err.println(new MethodWidget2("a", 3));

		MethodWidget2 rw0 = new MethodWidget2("a", 3);
		rw0.someCall("c", 33);		
	}
}
