package io.leego.example.aspect;

import io.leego.example.util.Result;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Leego Yih
 */
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
