package test.extended;
public class TestMethodPrimitives {
	public static void main(String[] args) {
		System.err.println(new PrimitiveMethodWidget());

		PrimitiveMethodWidget rw = new PrimitiveMethodWidget();
		rw.someCall();
		
		System.err.println(new PrimitiveMethodWidget("a", 3));

		PrimitiveMethodWidget rw0 = new PrimitiveMethodWidget("a", 3);
		rw0.someCall("c", 33);		
	}
}
