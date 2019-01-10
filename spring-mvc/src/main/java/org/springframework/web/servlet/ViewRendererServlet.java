package org.springframework.web.servlet;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.util.NestedServletException;

//视图渲染Servlet
@SuppressWarnings("serial")
public class ViewRendererServlet extends HttpServlet {

    /**
     * Request attribute to hold current web application context.
     * Otherwise only the global web app context is obtainable by tags etc.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#findWebApplicationContext
     */
    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE;

    /**
     * Name of request attribute that holds the View object
     */
    public static final String VIEW_ATTRIBUTE = ViewRendererServlet.class.getName() + ".VIEW";

    /**
     * Name of request attribute that holds the model Map
     */
    public static final String MODEL_ATTRIBUTE = ViewRendererServlet.class.getName() + ".MODEL";


    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    //处理HTTP请求
    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            renderView(request, response);
        } catch (ServletException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NestedServletException("View rendering failed", ex);
        }
    }

    //渲染视图
    @SuppressWarnings("unchecked")
    protected void renderView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        View view = (View) request.getAttribute(VIEW_ATTRIBUTE);
        if (view == null) {
            throw new ServletException("Could not complete render request: View is null");
        }
        Map<String, Object> model = (Map<String, Object>) request.getAttribute(MODEL_ATTRIBUTE);
        view.render(model, request, response);
    }

}
