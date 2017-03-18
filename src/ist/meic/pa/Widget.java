package ist.meic.pa;

public class Widget {
	int width;
	int height;
	int margin;
	
	@KeywordArgs("width=100,height=50,margin")
	public Widget(Object ...args) {}

	public static void main (String[] args) {
		Widget widget = new Widget("margin", 5);
		System.out.println(widget);
	}
	
	public String toString() {
		return String.format("width:%s,height:%s,margin:%s", width, height, margin);
	}
}