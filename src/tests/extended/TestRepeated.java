package test.extended;
public class TestRepeated {
	public static void main(String[] args) {
		System.err.println(new RepeatedWidget());
		System.err.println(new RepeatedWidget("a", 9));
		System.err.println(new RepeatedWidget("b", 9));
		System.err.println(new RepeatedWidget("a", 5, "b", 4));
	}
}
