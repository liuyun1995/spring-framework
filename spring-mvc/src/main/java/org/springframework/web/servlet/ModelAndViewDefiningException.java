package org.springframework.web.servlet;

import javax.servlet.ServletException;
import org.springframework.util.Assert;

@SuppressWarnings("serial")
public class ModelAndViewDefiningException extends ServletException {

	private ModelAndView modelAndView;

	public ModelAndViewDefiningException(ModelAndView modelAndView) {
		Assert.notNull(modelAndView, "ModelAndView must not be null in ModelAndViewDefiningException");
		this.modelAndView = modelAndView;
	}

	public ModelAndView getModelAndView() {
		return modelAndView;
	}

}
