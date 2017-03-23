package ist.meic.pa.KeyConstructorExtended;

import ist.meic.pa.*;
import javassist.*;
import javassist.expr.*;
import java.util.*;
import javassist.bytecode.*;

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

		// TODO: what if a constructor has multiple annotations
		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
			Object[] defaultsAndKeywords = methodDefaultsAndKeywords(behaviorAnnotations);		
			int startLineNo = method.getMethodInfo().getLineNumber(0);

			method.instrument(new ExprEditor() {
				private boolean instrumented = false;

				public void edit(MethodCall e) throws CannotCompileException {

				    if (!instrumented && e.getLineNumber() == startLineNo+2) {			    	
				    	String variableAssignement = ""; 
						for (String keyword : (List<String>) defaultsAndKeywords[1]) {
							variableAssignement += 
							"if (\""+keyword+"\".equals(keyword)) { " +
							//"	System.out.println(\"AAA \"+instanceof(args[i+1]));"+
							//"System.out.println(\"C: \"+((Object) args[i+1]).getClass().getName());"+
							"	"+keyword +" = ((Object) args[i+1]);"+//.getClass().cast(args[i+1]); " +
							"} "+"\n";
						}
						
						// build defaults
						String[] defaults = ((String) defaultsAndKeywords[0]).split(";");
						String declaredDefaults = "";

						for (String def : defaults) {
							String[] defParts = def.split("=");
							String defVar = defParts[0].trim();
							String defValue = defParts[1].trim();

							/*Float.parseFloat()
							declaredDefaults += 
							"String typeName = ((Object) "+defValue+").getClass().getName();" +
							"switch(typeName) { " +
							"	case \"java.lang.Integer\": " +
							"		"+ defVar +" = new Integer("+defValue+");" +
							"		break;" +
							"	case \"java.lang.Float\": "+
							"		"+ defVar +" = new Float("+defValue+");" +
							"		break;" +
							"	case \"java.lang.Double\": "+
							"		"+ defVar +" = new Double("+defValue+");" +
							"		break;" +
							";\n";*/
							//defaults += def.split("=")[0] + " = ("+
						}
						//	"	"+keyword +" = new  ((Object) args[i+1]).getClass().cast(args[i+1]); " +

/*						System.out.println("ASD  "+variableAssignement);
						System.out.println("ASD  "+declaredDefaults);*/

   					    e.replace(defaultsAndKeywords[0]+
							"for (int i = 0; i < args.length; i+=2) {" +
							"	String keyword = (String) args[i]; " +
							"	if (!\""+defaultsAndKeywords[1]+"\".contains(keyword)) {" +
							"		throw new java.lang.RuntimeException(\"Unrecognized keyword: \"+keyword); " +
							"	} else {" +
							variableAssignement +
							" 	}" +
							"}" +
   					    	"$_ = $proceed($$);"
						);

						instrumented = true;
				    }
				}
			});
			//(new InstructionPrinter(new java.io.PrintStream(System.out))).print(method);
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

		setConstructorBody(constructors, constructorAnnotations);
	}

	private void setConstructorBody(List<CtBehavior> constructors, List<Object[]> constructorAnnotations) throws CannotCompileException{
		if (constructors.isEmpty()) {
			return;
		} 

		CtBehavior constructor = constructors.get(0);
		Object[] annotations = constructorAnnotations.get(0);

		// TODO: what if a constructor has multiple annotations
		if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
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
	private Object[] methodDefaultsAndKeywords(List<Object[]> methodAnnotations) {
		String defaultValues = "";
		List<String> argumentNames = new ArrayList<String>();

		for (Object[] annotations : methodAnnotations) {
			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
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

	// returns an array with: a string with the default values and 
	// a list of respective keywords
	private Object[] defaultsAndKeywords(List<Object[]> constructorAnnotations) {

		String defaultValues = "";
		List<String> argumentNames = new ArrayList<String>();

		for (int i = constructorAnnotations.size()-1; i >= 0; --i) {
			Object[] annotations = constructorAnnotations.get(i);

			if (annotations.length == 1 && annotations[0] instanceof KeywordArgsExtended) {
				String annotationValue = ((KeywordArgsExtended) annotations[0]).value();
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

	// TODO: adicionar suporte para caracteres especiais dentro de strings
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