package ist.meic.pa.KeyConstructorExtended;

import ist.meic.pa.*;
import javassist.*;
import java.util.*;
import javassist.bytecode.Descriptor;

public class ExtendedKeywordTranslator implements Translator {

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		translateConstructor(pool, className);
		translateMethods(pool, className);
	}

	private void translateMethods(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc;
		try {
			cc = pool.get(className);
		} catch (NotFoundException e) {
			return;
		}

		CtClass objectParam = pool.get("java.lang.Object[]");
		List<Object[]> methodAnnotations = new ArrayList<Object[]>();
		List<CtBehavior> objectParamMethods = new ArrayList<CtBehavior>();

		do {
			CtMethod[] methods = cc.getDeclaredMethods();

			for (CtMethod method : methods) {
				CtClass[] params = method.getParameterTypes();
				if (params.length != 1 || !params[0].equals(objectParam)) {
					continue;
				} else {
					try {
						objectParamMethods.add(method);
						methodAnnotations.add(method.getAnnotations());
					} catch (ClassNotFoundException e) {
						continue;
					}
				}
			}

			cc = cc.getSuperclass();
		} while (cc != null);


		setMethodBody(objectParamMethods, methodAnnotations);
	}

	private void setMethodBody(List<CtBehavior> behaviors, List<Object[]> behaviorAnnotations) throws CannotCompileException{
		if (behaviors.isEmpty()) {
			return;
		} 
		
		CtBehavior behavior = behaviors.get(0);
		Object[] annotations = behaviorAnnotations.get(0);

		// TODO: what if a constructor has multiple annotations
		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
			String[] defaultsAndKeywords = methodDefaultsAndKeywords(behaviorAnnotations);
			System.out.println("M: "+defaultsAndKeywords[0]);

			behavior.setBody("{"+
				defaultsAndKeywords[0] +
				"for (int i = 0; i < $1.length; i+=2) {" +
				"	String keyword = (String) $1[i]; " +
				"	if (!\""+defaultsAndKeywords[1]+"\".contains(keyword)) {" +
				"		throw new java.lang.RuntimeException(\"Unrecognized keyword: \"+keyword); " +
				"	}" +
				"}" +
			"}");
		}
	}

	private void translateConstructor(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass cc = pool.get(className);
		List<CtBehavior> constructors = new ArrayList<CtBehavior>();
		List<Object[]> constructorAnnotations = new ArrayList<Object[]>();
		CtClass[] objectParams = new CtClass[1];
		objectParams[0] = pool.get("java.lang.Object[]");

		do {
			try {
				CtConstructor objectArrayConstr = cc.getDeclaredConstructor(objectParams);
				constructors.add(objectArrayConstr);
				constructorAnnotations.add(objectArrayConstr.getAnnotations());
				cc = cc.getSuperclass();
			} catch (ClassNotFoundException | NotFoundException e) {
			//	System.out.println("Not found");
				break;
			}
		} while (cc != null);

		setConstructorBody(constructors, constructorAnnotations, true);
	}

	private void setConstructorBody(List<CtBehavior> behaviors, List<Object[]> behaviorAnnotations, boolean constructor) throws CannotCompileException{
		if (behaviors.isEmpty()) {
			return;
		} 
		
		CtBehavior behavior = behaviors.get(0);
		Object[] annotations = behaviorAnnotations.get(0);

		// TODO: what if a constructor has multiple annotations
		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
			String[] defaultsAndKeywords = defaultsAndKeywords(behaviorAnnotations);
			String 	superCall =	constructor ? buildSuperCall(behaviors) : "" ;

			behavior.setBody("{"+
				superCall +
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
	private String buildSuperCall(List<CtBehavior> behaviors) {
		if (behaviors.size() == 1) {
			return "super();";
		}

		Object[] nextAnnotations;

		try {
			nextAnnotations = behaviors.get(1).getAnnotations();
		} catch (ClassNotFoundException e) {
			return "super();";
		}

		if (nextAnnotations.length == 1 && nextAnnotations[0] instanceof KeywordArgsExtended) {
			return "super(new Object[0]);";
		} else {
			return "super();";
		}
	}

	// returns an array with 2 strings: one with the default values and 
	// another with the respective keywords
	private String[] defaultsAndKeywords(List<Object[]> methodAnnotations) {

		String defaultValues = "";
		String argumentNames = "";

		for (int i = methodAnnotations.size()-1; i >= 0; --i) {
			Object[] annotations = methodAnnotations.get(i);

			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
				String[] arguments = annotationValue.split(",");

				for (int e = 0; e < arguments.length; ++e) {

					// this keyword has a default value
					if (arguments[e].contains("=")) {
						String keywordName = arguments[e].split("=")[0];

						// if a subclass hasn't already defined a default value
						//if (!defaultValues.contains(keywordName+"=")) {
							defaultValues += arguments[e]+";";

							if (!argumentNames.contains(keywordName)) {
								argumentNames += keywordName + " ";
							}
						//}
					} else if (!argumentNames.contains(arguments[e])) {
						argumentNames += arguments[e] + " ";
					}
				}
			}
		}

		return new String[] {defaultValues, argumentNames};
	}

		// returns an array with 2 strings: one with the default values and 
	// another with the respective keywords
	private String[] methodDefaultsAndKeywords(List<Object[]> methodAnnotations) {

		String defaultValues = "";
		String argumentNames = "";
		String declarations = "";

		for (Object[] annotations : methodAnnotations) {
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
				String[] arguments = annotationValue.split(",");

				for (int e = 0; e < arguments.length; ++e) {

					// this keyword has a default value
					if (arguments[e].contains("=")) {
						String keywordName = arguments[e].split("=")[0];

						// if a subclass hasn't already defined a default value
						//if (!defaultValues.contains(keywordName+"=")) {
							defaultValues += arguments[e]+";";

							if (!argumentNames.contains(keywordName)) {
								argumentNames += keywordName + " ";
								declarations += "Object "+keywordName+";";
							}
						//}
					} else if (!argumentNames.contains(arguments[e])) {
						argumentNames += arguments[e] + " ";
						declarations += "Object "+arguments[e]+";";
					}
				}
			}
		}

		
		return new String[] {declarations+defaultValues, argumentNames};
	}
}