package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContextUtils;

//可参数化的视图控制器
public class ParameterizableViewController extends AbstractController {

    private Object view;
    private HttpStatus statusCode;
    private boolean statusOnly;

    //构造器
    public ParameterizableViewController() {
        super(false);
        setSupportedMethods(HttpMethod.GET.name(), HttpMethod.HEAD.name());
    }

    //设置视图名称
    public void setViewName(String viewName) {
        this.view = viewName;
    }

    //获取视图名称
    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    //设置视图
    public void setView(View view) {
        this.view = view;
    }

    //获取视图
    public View getView() {
        return (this.view instanceof View ? (View) this.view : null);
    }

    //设置状态码
    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    //获取状态码
    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    /**
     * The property can be used to indicate the request is considered fully
     * handled within the controller and that no view should be used for rendering.
     * Useful in combination with {@link #setStatusCode}.
     * <p>By default this is set to {@code false}.
     *
     * @since 4.1
     */
    public void setStatusOnly(boolean statusOnly) {
        this.statusOnly = statusOnly;
    }

    /**
     * Whether the request is fully handled within the controller.
     */
    public boolean isStatusOnly() {
        return this.statusOnly;
    }

    //处理请求
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取视图名称
        String viewName = getViewName();

        if (getStatusCode() != null) {
            if (getStatusCode().is3xxRedirection()) {
                request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, getStatusCode());
                viewName = (viewName != null && !viewName.startsWith("redirect:") ? "redirect:" + viewName : viewName);
            } else {
                response.setStatus(getStatusCode().value());
                if (isStatusOnly() || (getStatusCode().equals(HttpStatus.NO_CONTENT) && getViewName() == null)) {
                    return null;
                }
            }
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addAllObjects(RequestContextUtils.getInputFlashMap(request));

        if (getViewName() != null) {
            modelAndView.setViewName(viewName);
        } else {
            modelAndView.setView(getView());
        }

        return (isStatusOnly() ? null : modelAndView);
    }

}
