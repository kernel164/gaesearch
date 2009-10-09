package org.gaesearch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface SearchField {
	String value() default "";

	/*
	enum Type {
		String
	};
	SearchField.Type type() default SearchField.Type.String;
	*/
}
