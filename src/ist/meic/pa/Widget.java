package ist.meic.pa;

public class Widget {
	int width;
	int height;
	int margin;
	
	@KeywordArgs("width=100,height=50,margin")
	public Widget(Object ...args) {}
	
	public String toString() {
		return String.format("width:%s,height:%s,margin:%s", width, height, margin);
	}

	public static void main (String[] args) {
		System.out.println(new Widget());
		System.out.println(new Widget("width", 80));
		System.out.println(new Widget("height", 30));
		System.out.println(new Widget("height", 20, "width", 90));
	}
}