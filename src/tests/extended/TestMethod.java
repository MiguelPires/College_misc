public class TestMethod {
	public static void main(String[] args) {
		System.err.println(new MethodWidget());

		MethodWidget rw = new MethodWidget();
		rw.someCall();
		
		System.err.println(new MethodWidget("a", 3));

		MethodWidget rw0 = new MethodWidget("a", 3);
		rw0.someCall("c", 33);		
	}
}
