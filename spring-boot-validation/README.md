# Spring Boot 数据校验最佳实践

## 前言

数据校验在项目开发中是重要的一环，即使是在客户端对数据进行校验的情况下，我们往往也还是需要对数据再进行一遍校验，
目的是为了防止用户绕过客户端或浏览器，使用某些工具或技术直接向服务端发起请求传递非法数据。需要特别注意的是，校验不应该仅绑定到`Web`层，它应该易于本地化，并且可以插入任何可用的校验器。

`Spring`为`Bean Validation API`提供全面支持，包括将`Bean Validation provider`引导为`Spring bean`。
这使得可以在应用程序中需要校验的任何地方注入`javax.validation.ValidatorFactory`或`javax.validation.Validator`实例对象。

有关`Bean Validation API`的信息，请参阅[Bean Validation]。

本文中示例基于`Java 8`和`Spring Boot 2.6.x`，若您使用`Spring Boot 3.0.0`及以上的版本，需要将`Java EE`相关依赖迁移至`Jakarta EE`。

## 准备工作

通过`Maven`添加`Spring Boot`依赖。

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
</dependencies>
```

## 通用注解和校验器

我们可以在`javax.validation.constraints`或`jakarta.validation.constraints`包下找到通用的校验注解，通常情况下这些注解可满足基本开发需求。

| 注解               | 说明                              |
|:-----------------|:--------------------------------|
| @AssertFalse     | 注解的元素必须是`false`。                |
| @AssertTrue      | 注解的元素必须是`true`。                 |
| @DecimalMax      | 注解的元素必须是数字，其值必须小于或等于指定的最大值。     | 
| @DecimalMin      | 注解的元素必须是数字，其值必须大于或等于指定的最小值。     | 
| @Digits          | 注解的元素必须是可接受范围内的数字。              | 
| @Email           | 注解的元素必须是有效电子邮件地址。               | 
| @Future          | 注解的元素必须是将来的日期时间。                | 
| @FutureOrPresent | 注解的元素必须是当前或将来的日期时间。             | 
| @Max             | 注解的元素必须是数字，其值必须小于或等于指定的最大值。     |  
| @Min             | 注解的元素必须是数字，其值必须大于或等于指定的最小值。     | 
| @Negative        | 注解的元素必须是严格的负数（即`0`被视为无效值）。      |
| @NegativeOrZero  | 注解的元素必须是负数或`0`。                 |
| @NotBlank        | 注解的元素不能是`null`，并且必须至少包含一个非空白字符。 |
| @NotEmpty        | 注解的元素不能是`null`或空。               | 
| @NotNul          | 注解的元素不能是`null`。                 | 
| @Null            | 注解的元素必须是`null`。                 | 
| @Past            | 注解的元素必须是过去的日期时间。                | 
| @PastOrPresent   | 注解的元素必须是当前或过去的日期时间。             | 
| @Pattern         | 注解的元素必须与指定的正则表达式匹配。             | 
| @Positive        | 注解的元素必须是严格的正数（即`0`被视为无效值）。      |    
| @PositiveOrZero  | 注解的元素必须是正数或`0`。                 |
| @Size            | 注解的元素必须在指定的边界（包含）之间。            | 

选用最常用的`@NotNull`注解进行演示：

1. 在需要校验的字段前添加注解。

```java
import javax.validation.constraints.NotNull;

public class NotNullValidateDTO {
    @NotNull(message = "无效数据")
    private String value;
    /* Getter and Setter */
}
```

2. 在`Controller`中定义一个方法，并在参数前加`@Validated`注解或`@Valid`注解启用校验。

```java
@GetMapping("testNotNull")
public boolean testNotNull(@Validated NotNullValidateDTO dto) {
    logger.debug("Valid value: {}", dto.getValue());
    return true;
}
```

3. 通过调用接口返回的结果可以发现校验功能已启用。

```
GET http://localhost:8080/testNotNull

HTTP/1.1 400 
Content-Type: text/plain;charset=UTF-8

无效数据
```

```
GET http://localhost:8080/testNotNull?value=hello

HTTP/1.1 200 
Content-Type: application/json

true
```

## 自定义注解和校验器

实际项目开发中，我们往往需要在多处对某个有特殊属性的格式进行校验，例如手机号码校验：

```java
import javax.validation.constraints.Pattern;

public class PhoneValidateDTO {
    @Pattern(regexp = "^(1[3-9])\\d{9}$", message = "手机号码格式不正确")
    private String phone;
    /* Getter and Setter */
}
```

若需要变更校验规则，则可能出现由于漏改导致出现校验结果不统一的问题，因此我们应该避免使用这种方式。

以手机号码校验为例：

1. 创建一个名为`Phone`的注解类。

```java
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Phone.List.class)
@Documented
@Constraint(validatedBy = {Phone.Validator.class})
public @interface Phone {

    String message() default "{javax.validation.constraints.Phone.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Phone[] value();
    }

    class Validator implements ConstraintValidator<Phone, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return false;
            }
            return Pattern.matches("^(1[3-9])\\d{9}$", value);
        }
    }
}
```

2. 将`@Pattern`注解替换为`@Phone`注解。

```java
public class PhoneValidateDTO {
    @Phone(message = "手机号码格式不正确")
    //@Pattern(regexp = "^(1[3-9])\\d{9}$", message = "手机号码格式不正确")
    private String phone;
    /* Getter and Setter */
}
```

3. 在`Controller`中定义一个方法，并在参数前加`@Validated`注解或`@Valid`注解启用校验。

```java
@GetMapping("testPhone")
public boolean testPhone(@Validated PhoneValidateDTO dto) {
    logger.debug("Valid phone: {}", dto.getPhone());
    return true;
}
```

4. 通过调用接口返回的结果可以发现校验功能已启用。

```
GET http://localhost:8080/testPhone?phone=110

HTTP/1.1 400 
Content-Type: text/plain;charset=UTF-8

手机号码格式不正确
```

```
GET http://localhost:8080/testPhone?phone=13888888888

HTTP/1.1 200 
Content-Type: application/json

true
```

到这里可能有开发者会觉得自定义的`@Phone`注解和通用的`@Pattern`注解没什么区别，不过是换了个地方写正则表达式罢了。
非也，基于自定义注解可以实现更多的能力，我们继续往下看。

## 可配置的校验规则

众所周知，在项目中硬编码是非常糟糕的实践，需求发生变化时，通常需要修改源码、编译工程、部署服务。

通过读取配置的方式可以很有效地解决这个问题，例如：

1. 创建一个名为`ValidationProperties`的类，并且在类上添加`@ConfigurationProperties`注解。

```java
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("validation")
public class ValidationProperties {
    private String username;
    /* Getter and Setter */
}
```

2. 在`Spring`上下文中启用`ValidationProperties`。

```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationConfiguration { }
```

3. 在`application.properties`中为`username`配置校验规则。

```properties
validation.username=^[A-Za-z0-9]{6,20}$
```

4. 创建一个名为`Username`的注解，且在`Validator`中注入`ValidationProperties`实例对象。

```java
import io.leego.example.config.ValidationProperties;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Username.List.class)
@Documented
@Constraint(validatedBy = {Username.Validator.class})
public @interface Username {

    String message() default "{javax.validation.constraints.Username.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Username[] value();
    }

    class Validator implements ConstraintValidator<Username, String> {
        private final ValidationProperties properties;

        /** 由于Spring会引导Validator为Spring Bean，因此这里可以注入Spring Context中的Bean。 */
        public Validator(ValidationProperties properties) {
            this.properties = properties;
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return false;
            }
            return Pattern.matches(properties.getUsername(), value);
        }
    }
}
```

5. 在需要校验的字段前加上`@Username`注解。

```java
public class UsernameValidateDTO {
    @Username(message = "用户名格式不正确")
    private String username;
    /* Getter and Setter */
}
```

6. 在`Controller`中定义一个方法，并在参数前加`@Validated`注解或`@Valid`注解启用校验。

```java
@GetMapping("testUsername")
public boolean testUsername(@Validated UsernameValidateDTO dto) {
    logger.debug("Valid username: {}", dto.getUsername());
    return true;
}
```

7. 通过调用接口返回的结果可以发现校验功能已启用。

```
GET http://localhost:8080/testUsername?username=hello

HTTP/1.1 400 
Content-Type: text/plain;charset=UTF-8

用户名格式不正确
```

```
GET http://localhost:8080/testUsername?username=username

HTTP/1.1 200 
Content-Type: application/json

true
```

## 获取自定义注解上的值

通常我们有特殊处理逻辑时才会自定义注解，因此通过注解附带属性可以很好的满足这种需求。

1. 创建一个名为`GenderEnum`的枚举类和带`nullable`属性`Gender`的注解。

```java
public enum GenderEnum {
    UNKNOWN(0),
    MALE(1),
    FEMALE(2);

    private final int code;
    
    private static final Map<Integer, GenderEnum> map = Arrays.stream(values())
            .collect(Collectors.toMap(GenderEnum::getCode, Function.identity()));

    public static GenderEnum get(Integer code) {
        return map.get(code);
    }
    /* Constructor and Getter */
}
```

```java
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
                // 允许为空时校验通过，否则返回400。
                return this.annotation.nullable();
            }
            // 性别编码存在时校验通过，否则不通过。
            return GenderEnum.get(value.intValue()) != null;
        }
    }
}
```

2. 在需要校验的字段前加上`@Gender`注解。

```java
public class GenderValidateDTO {
    @Gender(nullable = true, message = "性别格式不正确")
    private Integer gender;
    /* Getter and Setter */
}

```

3. 在`Controller`中定义一个方法，并在参数前加`@Validated`注解或`@Valid`注解启用校验。

```java
@GetMapping("testGender")
public boolean testGender(@Validated GenderValidateDTO dto) {
    logger.debug("Valid gender: {}", dto.getGender());
    return true;
}
```

4. 通过调用接口返回的结果可以发现校验功能已启用。

```
GET http://localhost:8080/testGender

HTTP/1.1 200 
Content-Type: application/json

true
```

```
GET http://localhost:8080/testGender?gender=100

HTTP/1.1 400 
Content-Type: text/plain;charset=UTF-8

性别格式不正确
```

```
GET http://localhost:8080/testGender?gender=0

HTTP/1.1 200 
Content-Type: application/json

true
```

## 使用配置中心

使用配置中心实现不重启服务实现动态修改配置，推荐配置中心（或集成了配置中心的框架/组件/中间件）：

- [Spring Cloud Config]
- [Spring Cloud Consul]
- [Nacos]
- [Apollo]

## 使用数据库或缓存配置

在`Validator`中注入`Repository`或`Template`对象并实现相关功能即可，实现过程不再赘述。

## 示例源码

通过Git下载源码或[直接浏览][spring-boot-validation]。

```bash
git clone https://github.com/yihleego/spring-boot-best-practices.git
cd spring-boot-best-practices/spring-boot-validation
```

## 参考资料

- [Bean Validation]
- [Spring Framework Validation]
- [Spring Boot]

[Bean Validation]:https://beanvalidation.org/

[Spring Framework Validation]:https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation

[Spring Boot]:https://spring.io/projects/spring-boot

[Spring Cloud Config]:https://spring.io/projects/spring-cloud-config

[Spring Cloud Consul]:https://spring.io/projects/spring-cloud-consul

[Nacos]:https://nacos.io/

[Apollo]:https://www.apolloconfig.com/

[spring-boot-validation]:https://github.com/yihleego/spring-boot-best-practices/tree/main/spring-boot-validation

