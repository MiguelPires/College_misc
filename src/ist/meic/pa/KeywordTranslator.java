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
			Object[] defaultsAndKeywords = defaultsAndKeywords(constructorAnnotations);

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

	// returns an array with: a string with the default values and 
	// a list of respective keywords
	private Object[] defaultsAndKeywords(List<Object[]> constructorAnnotations) {

		String defaultValues = "";
		List<String> argumentNames = new ArrayList<String>();

		for (int i = constructorAnnotations.size()-1; i >= 0; --i) {
			Object[] annotations = constructorAnnotations.get(i);

			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String annotationValue = ((KeywordArgs) annotations[0]).value();
				List<String> arguments = splitAnnotation(annotationValue);

				for (int e = 0; e < arguments.size(); ++e) {
					String annotPart = arguments.get(e);

					// this keyword has a default value
					if (annotPart.contains("=")) {
						String keywordName = annotPart.split("=")[0];

						// if a subclass hasn't already defined a default value
						//if (!defaultValues.contains(keywordName+"=")) {
							defaultValues += annotPart+";";

							if (!argumentNames.contains(keywordName)) {
								argumentNames.add(keywordName);
							}
						//}
					} else if (!argumentNames.contains(annotPart)) {
						argumentNames.add(annotPart);
					}
				}
			}
		}

		return new Object[] {defaultValues, argumentNames};
	}

	private List<String> splitAnnotation(String annotation) {
		List<String> annotValues = new ArrayList<String>();
		String value = "";
		int functionDepth = 0;

		for (int i = 0; i < annotation.length(); ++i) {
			switch (annotation.charAt(i)) {
				case ',':
					// parsed one keyword, move to next
					if (functionDepth <= 0) {
						annotValues.add(value);
						value = "";
						functionDepth = 0;
					} else { // the comma belongs to a function call
						value += annotation.charAt(i);
					}
					break;

				case '(':
					functionDepth++;
					value += annotation.charAt(i);
					break;

				case ')':
					functionDepth--;
					value += annotation.charAt(i);
					break;

				default:
					value += annotation.charAt(i);
					break;
			}
		}

		annotValues.add(value);
		return annotValues;

	}
}