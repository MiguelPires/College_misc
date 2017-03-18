package ist.meic.pa;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface KeywordArgs {
	String[] value() default "*";
}