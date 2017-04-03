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

		if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
			String defaults = getDefaults(constructorAnnotations);
			List<String> keywords = getKeywords(constructorAnnotations);

			constructor.setBody("{"+
				buildSuperCall(constructors) + 
				defaults + 
				"for (int i = 0; i < $1.length; i+=2) {" + 
				"	String keyword = (String) $1[i]; " + 
				"	if (!\""+keywords+"\".contains(keyword)) {" + 
				"		throw new java.lang.RuntimeException(\"Unrecognized keyword: \"+keyword); " + 
				"	}" + 
				"	java.lang.Class searchInClass = $class; " + 
				"	while (searchInClass != null) { " + 
				"		try { " + 
				"			java.lang.reflect.Field field = searchInClass.getDeclaredField(keyword);" + 
				"			field.set(this, $1[i+1]);" +
				"			break;" + 
				" 		} catch (NoSuchFieldException e) { " +
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


	// receives a list of annotations and returns a string with the default
	// assignments contained in it
	private String getDefaults(List<Object[]> behaviorAnnotations) {
		String defaultValues = "";
		List<String> argumentNames = new ArrayList<String>();

		for (int i = behaviorAnnotations.size()-1; i >= 0; --i) {
			Object[] annotations = behaviorAnnotations.get(i);

			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String annotationValue = ((KeywordArgs) annotations[0]).value();
				List<String> separateAnnotations = splitAnnotation(annotationValue);

				for (String annotation : separateAnnotations) {
					// this keyword has a default value
					if (annotation.contains("=")) {
						defaultValues += annotation+";";
					} 
				}
			}
		}

		return defaultValues;
	}

	// receives a list of annotations and returns all keywords declared in it
	private List<String> getKeywords(List<Object[]> behaviorAnnotations) {
		List<String> argumentNames = new ArrayList<String>();

		for (Object[] annotations : behaviorAnnotations) {
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgs) {
				String annotationValue = ((KeywordArgs) annotations[0]).value();
				List<String> separateAnnotations = splitAnnotation(annotationValue);

				for (String annotation : separateAnnotations) {
					// this keyword has a default value
					if (annotation.contains("=")) {
						String keywordName = annotation.split("=")[0];

						if (!argumentNames.contains(keywordName)) {
							argumentNames.add(keywordName);
						}
					} else if (!argumentNames.contains(annotation)) {
						argumentNames.add(annotation);
					}
				}
			}
		}
		
		return argumentNames;
	}

	// splits the annotation value according to the "," separator
	// if the "," doesn't belong a method call
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