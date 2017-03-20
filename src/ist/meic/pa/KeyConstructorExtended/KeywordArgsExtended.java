package ist.meic.pa.KeyConstructorExtended;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface KeywordArgsExtended {
	String value() default "*";
}