# Spring Boot 消息与国际化最佳实践

## 前言

大家平常浏览一些网站的时候可能会注意到，很多网站允许我们选择不同国家的语言，这其实都是为了满足不同国家和语言的人使用的需求。
通常情况下，一些比较复杂而且内容个性化的网站都会针对不同的国家，在内容上做一些调整，甚至是在不同的国家有独立站点，比如：淘宝、京东、亚马逊。
而一些内容比较标准化的公司，如果切换成语言，只是替换了网站中的一些文本和消息。

## 国际化和本地化

国际化（Internationalization，简称I18N）和本地化（Localization，简称L10N）是使计算机软件适应不同语言、区域特性和目标语言环境技术要求的手段。

国际化：是软件设计过程，它可以适应各种语言和地区而无需进行工程更改。

本地化：是通过翻译文本和添加特定于区域设置的组件来使国际化适应特定区域或语言的过程。

## 使用`MessageSource`

Spring提供了三种`MessageSource`实现：`ResourceBundleMessageSource`、`ReloadableResourceBundleMessageSource`和`StaticMessageSource`，
它们都实现了`HierarchicalMessageSource`，以便进行嵌套消息传递，这些接口共同提供了弹簧效应消息解决的基础。
其中`StaticMessageSource`提供了向源添加消息的方法，但是它很少会被使用。

`ApplicationContext`接口扩展了`MessageSource`接口，因此提供了国际化功能。在这些接口上定义的方法包括：

1. 用于检索消息的基本方法。当找不到指定区域设置的消息时，将使用默认消息。使用标准库提供的MessageFormat功能，传入的任何参数都将成为替换值。

       @Nullable
       String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale);

2. 基本与前面的方法相同，除了不能指定默认消息。如果找不到该消息，将抛出`NoSuchMessageException`。

       String getMessage(String code,@Nullable Object[]args,Locale locale)throws NoSuchMessageException;

3. 将所有属性也包装在一个名为`MessageSourceResolvable`的类中，可以将该类与此方法一起使用。

       String getMessage(MessageSourceResolvable resolvable,Locale locale)throws NoSuchMessageException;

加载`ApplicationContext`时，它会自动搜索上下文中定义的`MessageSource`bean，且`name`必须是`messageSource`。

如果存在这样的bean，那么上述方法的所有调用都将委托给消息源。

如果找不到消息源，`ApplicationContext`将尝试从父容器中查找包含同名的bean。如果`ApplicationContext`找不到任何消息源，则会实例化一个`DelegatingMessageSource`对象，以便能够接受上述定义的方法的调用。

## 准备工作

通过`Maven`添加`Spring Boot`依赖，其中`spring-boot-starter-validation`和`spring-boot-starter-aop`是演示示例中所需要的模块，非强制依赖。

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
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
</dependencies>
```

## 消息配置

本例在类路径中定义了一个名为`messages`的资源包，资源包文件的内容如下：

```properties
# messages.properties
error=Oops! An Error Has Occurred.
simple=Hello, world!
args=Hello, {0}!
default=What happened?
key.invalid=Key is invalid.
value.invalid=Value is invalid.
```

在`application.properties`中指定这个资源包：

```properties
spring.messages.basename=messages
```

创建一个名为`Messages`的类，集中管理消息`Code`：

```java
public final class Messages {
    public static final String ERROR = "error";
    public static final String SIMPLE = "simple";
    public static final String ARGS = "args";
    public static final String MISSING = "missing";
    public static final String DEFAULT = "default";
    public static final String KEY_INVALID = "key.invalid";
    public static final String VALUE_INVALID = "value.invalid";
}
```

## 简单消息的使用方法

在`Controller`中定义以下三个方法：

```java
@GetMapping("testSimpleMessage")
public String testSimpleMessage() {
    return messageSource.getMessage(Messages.SIMPLE, null, null, LocaleContextHolder.getLocale());
}

@GetMapping("testArgsMessage")
public String testArgsMessage() {
    return messageSource.getMessage(Messages.ARGS, new String[]{"Paul"}, null, LocaleContextHolder.getLocale());
}

@GetMapping("testDefaultMessage")
public String testDefaultMessage() {
    // 'missing' is not configured
    return messageSource.getMessage(Messages.MISSING, null, "Message is missing", LocaleContextHolder.getLocale());
}
```

依次调用分别可以得到结果：

```
GET http://localhost:8080/testSimpleMessage

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8

Hello, world!
```

```
GET http://localhost:8080/testArgsMessage

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8

Hello, Paul!
```

```
GET http://localhost:8080/testDefaultMessage

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8

Message is missing
```

可以发现消息功能是可以正常工作的。但是直接使用`MessageSource`获取消息的弊端也很明显，如果在需要使用消息的对象中各自注入`MessageSource`的bean，势必会对长期的维护造成困难。

## 通过AOP替换返回结果的消息

项目开发中，我们通常会对数据结果进行包装，并加上请求成功标识，错误编码和错误原因等，方便请求者对不同结果进行处理。

定义一个名为`Result`的结果包装类，如下：

```java
public class Result<T> {
    private T data;
    private Boolean success;
    private String message;
    private Integer code;

    public static <T> Result<T> buildSuccess(String message, T data) {
        return new Result<>(data, true, message, null);
    }
    
    public static <T> Result<T> buildFailure(String message) {
        return new Result<>(null, false, message, null);
    }
    /* Constructor, Getter and Setter */
}
```

在配置中使用`@EnableAspectJAutoProxy`注解启用AOP，并定义一个名为`ResultMessageAspect`的类，如下：

```java
@Aspect
@Component
public class ResultMessageAspect {
    private final MessageSource messageSource;

    public ResultMessageAspect(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * 拦截返回类型为<code>Result</code>的所有方法，并替换消息内容。
     *
     * @param result 返回结果对象
     */
    @AfterReturning(
            pointcut = "@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)",
            returning = "result")
    public Object convertResultMessageAfterReturning(Result<?> result) {
        if (StringUtils.hasText(result.getMessage())) {
            result.setMessage(messageSource.getMessage(result.getMessage(), null, result.getMessage(), LocaleContextHolder.getLocale()));
        }
        return result;
    }
}
```

在`Controller`中定义以下方法：

```java
@GetMapping("testResultMessage")
public Result<Void> testResultMessage() {
    Result.buildFailure(Messages.ERROR);
}
```

调用接口可以得到结果：

```
http://localhost:8080/testResultMessage

HTTP/1.1 200 
Content-Type: application/json

{
  "data": null,
  "success": false,
  "message": "Oops! An Error Has Occurred.",
  "code": null
}
```

## 通过`@ExceptionHandler`替换消息

`@ExceptionHandler`用于处理特定处理程序类和处理程序方法中的异常，使用此注释进行注释的处理程序方法可以具有非常灵活的签名。

`@ExceptionHandler`注解的方法仅适用于声明它们的`@Controller`类或类层次结构。如果它们是在`@ControllerAdvice`或`@RestControllerAdvice`类中声明的，则适用于任何控制器。

得益于`@ControllerAdvice`或`@RestControllerAdvice`的全局异常处理能力，我们可以统一处理校验消息和异常消息。

所以我们定义一个名为`WebControllerAdvice`的类用于替换消息，如下：

```java
@RestControllerAdvice
public class WebControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(WebControllerAdvice.class);
    private final MessageSource messageSource;

    public WebControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error("", e);
        return Result.buildFailure(buildConstraintViolation(e.getBindingResult(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Result<Void> handleBindException(BindException e) {
        logger.error("", e);
        return Result.buildFailure(buildConstraintViolation(e.getBindingResult(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("", e);
        return Result.buildFailure(getMessage(e.getMessage(), null));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public Result<Void> handleException(Exception e) {
        logger.error("", e);
        return Result.buildFailure(0, getMessage(e.getMessage(), null));
    }

    private String buildConstraintViolation(BindingResult result, String defaultMessage) {
        if (result == null || result.getErrorCount() <= 0) {
            return defaultMessage;
        }
        String message = result.getAllErrors().get(0).getDefaultMessage();
        Object[] args = result.getFieldErrorCount() > 0 ? result.getFieldErrors().stream().map(FieldError::getRejectedValue).toArray() : null;
        return getMessage(message, args);
    }

    private String getMessage(String message, Object[] args) {
        return messageSource.getMessage(message, args, message, LocaleContextHolder.getLocale());
    }
}
```

在`Controller`中定义以下方法：

```java
@GetMapping("testException")
public boolean testException(@RequestParam(defaultValue = "true") boolean flag) {
    if (flag) {
        throw new RuntimeException(Messages.ERROR);
    }
    return true;
}

@GetMapping("testInvalid")
public boolean testInvalid(@Validated Param param) {
    return true;
}

public class Param {
    @NotNull(message = "key.invalid")
    private String key;
    @NotNull(message = "{value.invalid}")
    private String value;
}
```

调用接口可以得到结果：

```
GET http://localhost:8080/testException?flag=true

HTTP/1.1 500 
Content-Type: application/json

{
  "data": null,
  "success": false,
  "message": "Oops! An Error Has Occurred.",
  "code": 0
}
```

```
GET http://localhost:8080/testInvalid?value=1

HTTP/1.1 400 
Content-Type: application/json

{
  "data": null,
  "success": false,
  "message": "Key is invalid.",
  "code": null
}
```

```
GET http://localhost:8080/testInvalid?key=1

HTTP/1.1 400 
Content-Type: application/json

{
  "data": null,
  "success": false,
  "message": "Value is invalid.",
  "code": null
}
```

虽然`@NotNull(message = "key.invalid")`和`@NotNull(message = "{value.invalid}")`都可以获取到正确的消息，但是其工作原理并不相同，
前者是通过上述定义的`WebControllerAdvice`拦截异常进行替换，而后者则是通过校验器本身的`MessageCodesResolver`实现的，具体内容可以查阅[Spring Framework Validation]，这里就不再赘述。

## 国际化和本地化

`MessageSource`接口提供了国际化功能，调用方法时指定`Locale`即可自动解析消息。

在原来`messages`资源包的基础上，再定义一个`messages_zh_CN`本地化资源包，资源包内容如下：

```properties
# messages_zh_CN.properties
error=哎呀！服务器开小差了。
simple=你好，世界！
args=你好, {0}!
default=发生什么事了？
key.invalid=非法键
value.invalid=非法值
```

将`Headers`的`Accept-Language`指定为`zh-CN`调用接口可以得到结果：

```
GET http://localhost:8080/testSimpleMessage
Accept-Language: zh-CN

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8

你好，世界！
```

## 示例源码

通过Git下载源码或[直接浏览][spring-boot-message]。

```bash
git clone https://github.com/yihleego/spring-boot-best-practices.git
cd spring-boot-best-practices/spring-boot-message
```

## 参考资料

- [Internationalization using MessageSource]
- [Spring Framework Validation]
- [Spring Boot]

[Internationalization using MessageSource]:https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-messagesource

[Spring Framework Validation]:https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation

[Spring Boot]:https://spring.io/projects/spring-boot

[spring-boot-message]:https://github.com/yihleego/spring-boot-best-practices/tree/main/spring-boot-message
