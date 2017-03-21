package ist.meic.pa;

import javassist.*;
import java.util.*;
import javassist.bytecode.Descriptor;
public class KeywordTranslator implements Translator {

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc = pool.get(className);
		List<CtConstructor> constructors = new ArrayList<CtConstructor>();
		List<Object[]> constructorAnnotations = new ArrayList<Object[]>();

		// get constructors and corresponding annotations
		do {
			try {
				CtClass[] objectParams = new CtClass[1];
				objectParams[0] = pool.get("java.lang.Object[]");
				CtConstructor objectArrayConstr= cc.getDeclaredConstructor(objectParams);
				constructors.add(objectArrayConstr);
				constructorAnnotations.add(objectArrayConstr.getAnnotations());
				cc = cc.getSuperclass();
			} catch (ClassNotFoundException | NotFoundException e) {
			//	System.out.println("Not found");
				break;
			}
		} while (cc != null);

		if (constructors.isEmpty()) {
			return;
		}

		setBody(constructors, constructorAnnotations);
	}

	private void setBody(List<CtConstructor> constructors, List<Object[]> constructorAnnotations) throws CannotCompileException{
		if (constructors.isEmpty()) {
			return;
		} 
		
		CtConstructor constructor = constructors.get(0);
		Object[] annotations = constructorAnnotations.get(0);

		// TODO: what if a constructor has multiple annotations
		if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
			String[] defaultsAndKeywords = defaultsAndKeywords(constructorAnnotations);

			constructor.setBody("{"+
				buildSuperCall(constructors) +
				defaultsAndKeywords[0] +
				"for (int i = 0; i < $1.length; i+=2) {" +
				"	String keyword = (String) $1[i]; " +
				"	if (!\""+defaultsAndKeywords[1]+"\".contains(keyword)) {" +
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

	// builds the super() call depending if the superclass is annotated or not
	private String buildSuperCall(List<CtConstructor> constructors) {
		if (constructors.size() == 1) {
			return "super();";
		}

		Object[] nextAnnotations;

		try {
			nextAnnotations = constructors.get(1).getAnnotations();
		} catch (ClassNotFoundException e) {
			return "super();";
		}

		if (nextAnnotations.length == 1 && nextAnnotations[0] instanceof KeywordArgs) {
			return "super(new Object[0]);";
		} else {
			return "super();";
		}
	}

	// returns an array with 2 strings: one with the default values and 
	// another with the respective keywords
	private String[] defaultsAndKeywords(List<Object[]> constructorAnnotations) {

		String defaultValues = "";
		String argumentNames = "";

		for (Object[] annotations : constructorAnnotations) {
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String annotationValue = ((KeywordArgs) annotations[0]).value();
				String[] arguments = annotationValue.split(",");

				for (int e = 0; e < arguments.length; ++e) {

					// this keyword has a default value
					if (arguments[e].contains("=")) {
						String keywordName = arguments[e].split("=")[0];

						// if a subclass hasn't already defined a default value
						if (!defaultValues.contains(keywordName+"=")) {
							defaultValues += arguments[e]+";";

							if (!argumentNames.contains(keywordName)) {
								argumentNames += keywordName + " ";
							}
						}
					} else if (!argumentNames.contains(arguments[e])) {
						argumentNames += arguments[e] + " ";
					}
				}
			}
		}

		return new String[] {defaultValues, argumentNames};
	}
}