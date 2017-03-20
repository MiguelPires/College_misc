import ist.meic.pa.KeyConstructorExtended.KeywordArgsExtended;

public class RepeatedWidget {
	int firstNum;
	int secondNum;

	@KeywordArgsExtended("firstNum=1,secondNum=firstNum,firstNum=2")
	public RepeatedWidget(Object... args) {}

	public String toString() {
		return String.format("firstNum: %s, secondNum: %s",
				firstNum, secondNum);
	}
}
