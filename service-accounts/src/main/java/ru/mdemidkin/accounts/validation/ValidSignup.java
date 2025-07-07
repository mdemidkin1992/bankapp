
package ru.mdemidkin.accounts.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SignupValidator.class)
@Documented
public @interface ValidSignup {
    String message() default "Запрос на создание нового пользователя не соответствует критериям.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
