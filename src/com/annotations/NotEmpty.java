package com.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@NotNull
@Size(min = 2)
@Constraint(validatedBy = {})
public @interface NotEmpty {
	String message() default "Not a valid input.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default { };
}
