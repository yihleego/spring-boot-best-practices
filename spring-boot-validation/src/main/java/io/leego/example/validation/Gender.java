package io.leego.example.validation;

import io.leego.example.enumeration.GenderEnum;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Leego Yih
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Gender.List.class)
@Documented
@Constraint(validatedBy = {Gender.Validator.class})
public @interface Gender {

    String message() default "{javax.validation.constraints.Gender.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 是否允许值为空，默认不允许为空。 */
    boolean nullable() default false;

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Gender[] value();
    }

    class Validator implements ConstraintValidator<Gender, Number> {
        private Gender annotation;

        /** 初始化时引用注解对象，线程安全。 */
        @Override
        public void initialize(Gender constraintAnnotation) {
            this.annotation = constraintAnnotation;
        }

        @Override
        public boolean isValid(Number value, ConstraintValidatorContext context) {
            if (value == null) {
                // 允许为空时校验通过，否则不通过。
                return this.annotation.nullable();
            }
            // 性别编码存在时校验通过，否则不通过。
            return GenderEnum.get(value.intValue()) != null;
        }
    }
}

