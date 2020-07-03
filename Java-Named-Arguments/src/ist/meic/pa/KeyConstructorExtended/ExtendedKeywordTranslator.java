package ist.meic.pa.KeyConstructorExtended;

import ist.meic.pa.*;
import javassist.*;
import javassist.expr.*;
import java.util.*;
import javassist.bytecode.*;
import java.util.regex.Pattern;

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
		CtClass originalClass, cc;
		try {
			originalClass = pool.get(className);
			cc = originalClass;
		} catch (NotFoundException e) {
			return;
		}

		CtClass objectParam = pool.get("java.lang.Object[]");
		List<Object[]> methodAnnotations = new ArrayList<Object[]>();
		List<CtMethod> objectParamMethods = new ArrayList<CtMethod>();

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


		setMethodBody(originalClass.getClassPool(), objectParamMethods, methodAnnotations);
	}

	private void setMethodBody(ClassPool pool, List<CtMethod> methods, List<Object[]> behaviorAnnotations) throws CannotCompileException{
		if (methods.isEmpty()) {
			return;
		} 
		
		CtMethod method = methods.get(0);
		Object[] annotations = behaviorAnnotations.get(0);

		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
			List<String> keywords = getKeywords(behaviorAnnotations);
			String defaults = getDefaults(behaviorAnnotations);

			int startLineNo = method.getMethodInfo().getLineNumber(0);
			
			method.instrument(new ExprEditor() {
				private boolean instrumented = false;

				public void edit(MethodCall e) throws CannotCompileException {
				    if (!instrumented && e.getLineNumber() == startLineNo+keywords.size()+1) {			    	
				    	String variableAssignment = ""; 
						for (String keyword : keywords) {
							variableAssignment += 
							"if (\""+keyword+"\".equals(keyword)) { " +
							"	"+keyword +" = args[i+1];" +
							"} "+"\n";
						}
						
						String[] separatedDefaults = defaults.split(";");
						String declaredDefaults = "";

						// aggregate default declarations
						for (String def : separatedDefaults) {
							String[] defParts = def.split("=");
							String defVar = defParts[0].trim();
							String defValue = defParts[1].trim();

							// it may be necessary to perform boxing of primitive types
							if (Pattern.matches("[+-]*[0-9]+.[0-9]+", defValue)) {
								declaredDefaults += defVar +" = Double.valueOf(\""+defValue+"\");";
							} else if (Pattern.matches("[+-]*[0-9]+", defValue)) {
								declaredDefaults += defVar +" = Integer.valueOf(\""+defValue+"\");";
							} else {
								declaredDefaults += def+";";
							}
						}

   					    e.replace(declaredDefaults+
							"for (int i = 0; i < args.length; i+=2) {" +
							"	String keyword = (String) args[i]; " +
							"	if (!\""+keywords+"\".contains(keyword)) {" +
							"		throw new java.lang.RuntimeException(\"Unrecognized keyword: \"+keyword); " +
							"	} else {" +
							variableAssignment +
							" 	}" +
							"}" +
   					    	"$_ = $proceed($$);"
						);

						// Access the code attribute
						CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
					    LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) 
					    codeAttribute.getAttribute(LineNumberAttribute.tag);

					    // get the program counters for the source declarations
					    int startPc = lineNumberAttribute.toStartPc(startLineNo);
					    int endPc = lineNumberAttribute.toStartPc(startLineNo+keywords.size()+1);
						
						// ignore the previous declarations
						byte[] code = codeAttribute.getCode();
						for (int i = startPc; i < endPc; i++) {
						   code[i] = CodeAttribute.NOP;
						}

						instrumented = true;
				    }

				}
			});
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
				break;
			}
		} while (cc != null);

		setConstructorBody(constructors, constructorAnnotations);
	}

	private void setConstructorBody(List<CtBehavior> constructors, List<Object[]> constructorAnnotations) throws CannotCompileException{
		if (constructors.isEmpty()) {
			return;
		} 

		CtBehavior constructor = constructors.get(0);
		Object[] annotations = constructorAnnotations.get(0);

		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
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

	// receives a list of annotations and returns a string with the default
	// assignments contained in it
	private String getDefaults(List<Object[]> behaviorAnnotations) {
		String defaultValues = "";
		List<String> argumentNames = new ArrayList<String>();

		for (int i = behaviorAnnotations.size()-1; i >= 0; --i) {
			Object[] annotations = behaviorAnnotations.get(i);

			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
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
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
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