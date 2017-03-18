package ist.meic.pa;

import javassist.*;

public class KeywordTranslator implements Translator {

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc = pool.get(className);
		
		for (CtConstructor constructor : cc.getConstructors()) {

			Object[] annotations;

			try {
				annotations = constructor.getAnnotations();
			} catch (ClassNotFoundException e) {
				throw new NotFoundException(e.getMessage());
			}

			// TODO: what if a constructor has multiple annotations
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String annotationValue = ((KeywordArgs) annotations[0]).value();

				String[] arguments = annotationValue.split(",");

				// the default initializations
				String constructorCode = "";

				for (int i = 0; i < arguments.length; ++i) {
					if (arguments[i].contains("=")) {
						constructorCode += arguments[i]+";";
					}
				}

				// sets the fields to the values passed in the parameters
				constructor.setBody("{"+
					constructorCode +
					"for (int i = 0; i < $1.length;) {" +
					"	java.lang.reflect.Field field = $class.getDeclaredField((String) $1[i]);"+
					"	field.set(this, $1[i+1]);"+
					"	i += 2;"+
					"}"+
				"}");
			}
		}
	}
}