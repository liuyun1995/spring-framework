package org.springframework.context;

import java.util.Locale;

//消息源
public interface MessageSource {
	
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);
	
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;
	
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
