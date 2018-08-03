package org.springframework.beans.factory.exception;

import org.springframework.beans.FatalBeanException;
import org.springframework.core.NestedRuntimeException;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class BeanCreationException extends FatalBeanException {

	private String beanName;

	private String resourceDescription;

	private List<Throwable> relatedCauses;
	
	public BeanCreationException(String msg) {
		super(msg);
	}
	
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public BeanCreationException(String beanName, String msg) {
		super("Error creating bean" + (beanName != null ? " with name '" + beanName + "'" : "") + ": " + msg);
		this.beanName = beanName;
	}
	
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		this(beanName, msg);
		initCause(cause);
	}
	
	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		super("Error creating bean" + (beanName != null ? " with name '" + beanName + "'" : "") +
				(resourceDescription != null ? " defined in " + resourceDescription : "") + ": " + msg);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}
	
	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable cause) {
		this(resourceDescription, beanName, msg);
		initCause(cause);
	}
	
	public String getBeanName() {
		return this.beanName;
	}
	
	public String getResourceDescription() {
		return this.resourceDescription;
	}
	
	public void addRelatedCause(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new LinkedList<Throwable>();
		}
		this.relatedCauses.add(ex);
	}
	
	public Throwable[] getRelatedCauses() {
		if (this.relatedCauses == null) {
			return null;
		}
		return this.relatedCauses.toArray(new Throwable[this.relatedCauses.size()]);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				sb.append("\nRelated cause: ");
				sb.append(relatedCause);
			}
		}
		return sb.toString();
	}

	@Override
	public void printStackTrace(PrintStream ps) {
		synchronized (ps) {
			super.printStackTrace(ps);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					ps.println("Related cause:");
					relatedCause.printStackTrace(ps);
				}
			}
		}
	}

	@Override
	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			super.printStackTrace(pw);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					pw.println("Related cause:");
					relatedCause.printStackTrace(pw);
				}
			}
		}
	}

	@Override
	public boolean contains(Class<?> exClass) {
		if (super.contains(exClass)) {
			return true;
		}
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				if (relatedCause instanceof NestedRuntimeException &&
						((NestedRuntimeException) relatedCause).contains(exClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
