package ist.meic.pa;

public class ExtendedWidget extends Widget {
	String name;

	@KeywordArgs("name=\"Extended\",width=200,margin=10,height")
	public ExtendedWidget(Object... args) {}

	public String toString() {
		return String.format("width:%s,height:%s,margin:%s,name:%s",
			width, height, margin, name);
	}

	public static void main(String[] args) {
		System.out.println(new ExtendedWidget());
		System.out.println(new ExtendedWidget("width", 80));
		System.out.println(new ExtendedWidget("height", 30));
		System.out.println(new ExtendedWidget("height", 20, "width", 90));
		System.out.println(new ExtendedWidget("height", 20, "width", 90, "name", "Nice"));
		System.err.println(new ExtendedWidget("foo", 1, "bar", 2));
	}
}