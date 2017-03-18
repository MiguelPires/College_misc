package ist.meic.pa;

import javassist.*;

public class KeywordTranslator implements Translator {

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc = pool.get(className);
		
		for (CtConstructor method : cc.getConstructors()) {

			Object[] annotations;

			try {
				annotations = method.getAnnotations();
			} catch (ClassNotFoundException e) {
				throw new NotFoundException(e.getMessage());
			}

			for (int e = 0; e < annotations.length; ++e) {
				System.out.println(annotations[e]);
			}

			// TODO: what if a method has multiple annotations
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String[] arguments = ((KeywordArgs) annotations[0]).value();

				for (int i = 0; i < arguments.length; ++i) {
					System.out.println(arguments[i]);
				}
			}
		}
	}
}