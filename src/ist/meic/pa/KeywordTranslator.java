package ist.meic.pa;

import javassist.*;
import java.util.*;

public class KeywordTranslator implements Translator {

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc = pool.get(className);
		
		List<CtConstructor> constructors = new ArrayList<CtConstructor>();
		CtConstructor constr;

		do {
			try {
				constructors.addAll(Arrays.asList(cc.getConstructors()));
				cc = cc.getSuperclass();
			} catch (NotFoundException e) {
				System.out.println("Not found");
				break;
			}
		} while (cc != null);


		for (int i = 0; i < constructors.size(); ++i) {
			CtConstructor constructor = constructors.get(i);

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
				String defaultValues = "";
				String argumentNames = "";//new String[arguments.length];

				for (int e = 0; e < arguments.length; ++e) {
					if (arguments[e].contains("=")) {
						defaultValues += arguments[e]+";";
						argumentNames += arguments[e].split("=")[0]+" ";
					} else {
						argumentNames += arguments[e] + " ";
					}
				}

				// sets the fields to the values passed in the parameters
				Object[] nextAnnotations;

				try {
					nextAnnotations = constructors.get(i+1).getAnnotations();
				} catch (ClassNotFoundException e) {
					throw new NotFoundException(e.getMessage());
				}

				String superclassCall;

				if (nextAnnotations.length == 1 && nextAnnotations[0] instanceof KeywordArgs) {
					superclassCall = "super(new Object[0]);";
				} else {
					superclassCall = "super();";
				}

				System.out.println(argumentNames);

				constructor.setBody("{"+
					superclassCall +
					defaultValues +
					//"System.out.println(\"\");"+
					"for (int i = 0; i < $1.length; i+=2) {" +
					"	String keyword = (String) $1[i]; " +
					"	if (!\""+argumentNames+"\".contains(keyword)) {" +
					"		throw new java.lang.RuntimeException(\"Unrecognized keyword: \"+keyword); " +
					"	}" +
					"	java.lang.Class searchInClass = $class; " +
					"	while (searchInClass != null) { " +
					//"		System.out.println(\"Searching for \"+$1[i]+\" in \"+searchInClass);"+
					"		try { " + 
					"			java.lang.reflect.Field field = searchInClass.getDeclaredField(keyword);" +
					"			field.set(this, $1[i+1]);" +
					//"			System.out.println(\"Setting \"+$1[i]+\" to \"+$1[i+1]); "+
					"			break;" +
					" 		} catch (NoSuchFieldException e) { " +
					//"			System.out.println(\"Didn't find \"+$1[i]+\" in \"+searchInClass.getName());"+
					"			searchInClass = searchInClass.getSuperclass();" +
					"		}" +
					"	}" +
					"}" +
				"}");
			}
		}
	}
}