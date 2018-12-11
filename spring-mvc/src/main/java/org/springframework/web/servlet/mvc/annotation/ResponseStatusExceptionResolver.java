package org.springframework.web.servlet.mvc.annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

//响应状态异常解析器
public class ResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver implements MessageSourceAware {

    private MessageSource messageSource;

    //设置消息源
    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    //解析异常
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
                                              Object handler, Exception ex) {
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            try {
                return resolveResponseStatus(responseStatus, request, response, handler, ex);
            } catch (Exception resolveEx) {
                logger.warn("Handling of @ResponseStatus resulted in Exception", resolveEx);
            }
        } else if (ex.getCause() instanceof Exception) {
            ex = (Exception) ex.getCause();
            return doResolveException(request, response, handler, ex);
        }
        return null;
    }

    //解析响应状态
    protected ModelAndView resolveResponseStatus(ResponseStatus responseStatus, HttpServletRequest request,
                                                 HttpServletResponse response, Object handler, Exception ex) throws Exception {
        int statusCode = responseStatus.code().value();
        String reason = responseStatus.reason();
        if (!StringUtils.hasLength(reason)) {
            response.sendError(statusCode);
        } else {
            String resolvedReason = (this.messageSource != null ?
                    this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale()) :
                    reason);
            response.sendError(statusCode, resolvedReason);
        }
        return new ModelAndView();
    }

}
