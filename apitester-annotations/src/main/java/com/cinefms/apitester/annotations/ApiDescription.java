package com.cinefms.apitester.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={ ElementType.PARAMETER, ElementType.METHOD } )
public @interface ApiDescription {

	String value() default "";
	String since() default "0.0";
	String format() default "";
	String deprecatedSince() default "";
	
}
