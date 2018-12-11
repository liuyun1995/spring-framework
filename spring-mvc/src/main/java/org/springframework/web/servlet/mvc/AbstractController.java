package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.WebUtils;

//抽象控制器
public abstract class AbstractController extends WebContentGenerator implements Controller {

	private boolean synchronizeOnSession = false;

	public AbstractController() {
		this(true);
	}

	public AbstractController(boolean restrictDefaultSupportedMethods) {
		super(restrictDefaultSupportedMethods);
	}


	//设置会话中同步
	public final void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	//是否会话中同步
	public final boolean isSynchronizeOnSession() {
		return this.synchronizeOnSession;
	}

	//处理请求
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//若HTTP请求方式是OPTIONS
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			response.setHeader("Allow", getAllowHeader());
			return null;
		}
		//校验HTTP请求
		checkRequest(request);
		//预备HTTP响应
		prepareResponse(response);
		//是否在会话中同步
		if (this.synchronizeOnSession) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				//获取对象锁
				Object mutex = WebUtils.getSessionMutex(session);
				//对请求进行同步处理
				synchronized (mutex) {
					return handleRequestInternal(request, response);
				}
			}
		}
		//处理HTTP请求
		return handleRequestInternal(request, response);
	}

	/**
	 * Template method. Subclasses must implement this.
	 * The contract is the same as for {@code handleRequest}.
	 * @see #handleRequest
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
