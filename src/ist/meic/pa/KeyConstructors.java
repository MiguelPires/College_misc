package ist.meic.pa;

import javassist.*;

public class KeyConstructors {
	public static void main(String[] args) throws Throwable {

		if (args.length < 1) {
			System.out.println("Usage: java ist.meic.pa.KeyConstructors <class> <optional_args>");
		}

		String className = args[0];
		Loader classLoader = new Loader();
		ClassPool pool = ClassPool.getDefault();

		KeywordTranslator translat = new KeywordTranslator();
		classLoader.addTranslator(pool, translat);

		String[] programArgs = new String[args.length-1];
		System.arraycopy(args, 1, programArgs, 0, programArgs.length);
		
		classLoader.run(className, programArgs);
	}
}