package ru.kostromin.preliminarymedexamtohrtransmitter.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntervalTimeValidator.class)
@Documented
public @interface IntervalTime {

  String message() default "Неверное заполнение стратегии kind-interval-time или свойств date-from/date-to";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}