public class TestInheritedMethod {
	public static void main(String[] args) {
		System.err.println(new InheritedMethodWidget());

		MethodWidget rw = new MethodWidget();
		rw.someCall();
		System.err.println(rw);
		
		System.err.println(new MethodWidget("a", 3));

		MethodWidget rw0 = new MethodWidget("a", 3);
		rw0.someCall("b", 33);
		System.err.println(rw0);

		System.err.println(new MethodWidget("a", 3, "b", 4));
		
		MethodWidget rw1 = new MethodWidget("a", 3, "b", 4);
		rw1.someCall();
		System.err.println(rw1);
	}
}
