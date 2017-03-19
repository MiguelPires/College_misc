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

		do {
			try {
				CtClass[] objectParams = new CtClass[1];
				objectParams[0] = pool.get("java.lang.Object[]");
				constructors.add(cc.getDeclaredConstructor(objectParams));
				cc = cc.getSuperclass();
			} catch (NotFoundException e) {
				//System.out.println("Not found");
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
				String[] defaultsAndKeywords = defaultsAndKeywords(annotationValue);

				// TODO: if the superclass constructor can have keywords that aren't
				// declared in the subclass, we need to pass the arguments up and
				// we need to search for unrecognized keyword in the superclasses 
				constructor.setBody("{"+
					buildSuperCall(constructors, i) +
					defaultsAndKeywords[0] +
					//"System.out.println(\"\");"+
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
	}

	// builds the super() call depending if the superclass is annotated or not
	private String buildSuperCall(List<CtConstructor> constructors, int index) {
		// sets the fields to the values passed in the parameters
		if (index == constructors.size()-1) 
			return "super();";

		Object[] nextAnnotations;

		try {
			nextAnnotations = constructors.get(index+1).getAnnotations();
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
	private String[] defaultsAndKeywords(String annotationValue) {
		String[] arguments = annotationValue.split(",");

		// the default initializations
		String defaultValues = "";
		String argumentNames = "";

		for (int e = 0; e < arguments.length; ++e) {
			if (arguments[e].contains("=")) {
				defaultValues += arguments[e]+";";
				argumentNames += arguments[e].split("=")[0]+" ";
			} else {
				argumentNames += arguments[e] + " ";
			}
		}

		return new String[] {defaultValues, argumentNames};
	}
}