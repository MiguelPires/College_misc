package ist.meic.pa.KeyConstructorExtended;

import javassist.*;

public class KeyConstructorsExtended {
	public static void main(String[] args) throws Throwable {

		if (args.length < 1) {
			System.out.println("Usage: java ist.meic.pa.KeyConstructorExtended.KeyConstructorsExtended <class> <optional_args>");
		}

		String className = args[0];
		Loader classLoader = new Loader();
		ClassPool pool = ClassPool.getDefault();

		ExtendedKeywordTranslator translat = new ExtendedKeywordTranslator();
		classLoader.addTranslator(pool, translat);

		String[] programArgs = new String[args.length-1];
		System.arraycopy(args, 1, programArgs, 0, programArgs.length);
		
		classLoader.run(className, programArgs);
	}
}