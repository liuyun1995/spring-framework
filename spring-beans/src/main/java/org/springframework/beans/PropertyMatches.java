package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

//属性匹配器
public abstract class PropertyMatches {

	//默认最大属性距离
	public static final int DEFAULT_MAX_DISTANCE = 2;

	public static PropertyMatches forProperty(String propertyName, Class<?> beanClass) {
		return forProperty(propertyName, beanClass, DEFAULT_MAX_DISTANCE);
	}

	public static PropertyMatches forProperty(String propertyName, Class<?> beanClass, int maxDistance) {
		return new BeanPropertyMatches(propertyName, beanClass, maxDistance);
	}

	public static PropertyMatches forField(String propertyName, Class<?> beanClass) {
		return forField(propertyName, beanClass, DEFAULT_MAX_DISTANCE);
	}

	public static PropertyMatches forField(String propertyName, Class<?> beanClass, int maxDistance) {
		return new FieldPropertyMatches(propertyName, beanClass, maxDistance);
	}

	// Instance state

	private final String propertyName;

	private String[] possibleMatches;

	private PropertyMatches(String propertyName, String[] possibleMatches) {
		this.propertyName = propertyName;
		this.possibleMatches = possibleMatches;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public String[] getPossibleMatches() {
		return this.possibleMatches;
	}

	public abstract String buildErrorMessage();

	// Implementation support for subclasses

	protected void appendHintMessage(StringBuilder msg) {
		msg.append("Did you mean ");
		for (int i = 0; i < this.possibleMatches.length; i++) {
			msg.append('\'');
			msg.append(this.possibleMatches[i]);
			if (i < this.possibleMatches.length - 2) {
				msg.append("', ");
			} else if (i == this.possibleMatches.length - 2) {
				msg.append("', or ");
			}
		}
		msg.append("'?");
	}

	/**
	 * Calculate the distance between the given two Strings according to the
	 * Levenshtein algorithm.
	 * 
	 * @param s1
	 *            the first String
	 * @param s2
	 *            the second String
	 * @return the distance value
	 */
	private static int calculateStringDistance(String s1, String s2) {
		if (s1.isEmpty()) {
			return s2.length();
		}
		if (s2.isEmpty()) {
			return s1.length();
		}
		int d[][] = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= s2.length(); j++) {
			d[0][j] = j;
		}

		for (int i = 1; i <= s1.length(); i++) {
			char s_i = s1.charAt(i - 1);
			for (int j = 1; j <= s2.length(); j++) {
				int cost;
				char t_j = s2.charAt(j - 1);
				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
			}
		}

		return d[s1.length()][s2.length()];
	}

	// Concrete subclasses

	private static class BeanPropertyMatches extends PropertyMatches {

		public BeanPropertyMatches(String propertyName, Class<?> beanClass, int maxDistance) {
			super(propertyName,
					calculateMatches(propertyName, BeanUtils.getPropertyDescriptors(beanClass), maxDistance));
		}

		/**
		 * Generate possible property alternatives for the given property and class.
		 * Internally uses the {@code getStringDistance} method, which in turn uses the
		 * Levenshtein algorithm to determine the distance between two Strings.
		 * 
		 * @param propertyDescriptors
		 *            the JavaBeans property descriptors to search
		 * @param maxDistance
		 *            the maximum distance to accept
		 */
		private static String[] calculateMatches(String propertyName, PropertyDescriptor[] propertyDescriptors,
				int maxDistance) {
			List<String> candidates = new ArrayList<String>();
			for (PropertyDescriptor pd : propertyDescriptors) {
				if (pd.getWriteMethod() != null) {
					String possibleAlternative = pd.getName();
					if (calculateStringDistance(propertyName, possibleAlternative) <= maxDistance) {
						candidates.add(possibleAlternative);
					}
				}
			}
			Collections.sort(candidates);
			return StringUtils.toStringArray(candidates);
		}

		@Override
		public String buildErrorMessage() {
			String propertyName = getPropertyName();
			String[] possibleMatches = getPossibleMatches();
			StringBuilder msg = new StringBuilder();
			msg.append("Bean property '");
			msg.append(propertyName);
			msg.append("' is not writable or has an invalid setter method. ");

			if (ObjectUtils.isEmpty(possibleMatches)) {
				msg.append("Does the parameter type of the setter match the return type of the getter?");
			} else {
				appendHintMessage(msg);
			}
			return msg.toString();
		}
	}

	private static class FieldPropertyMatches extends PropertyMatches {

		public FieldPropertyMatches(String propertyName, Class<?> beanClass, int maxDistance) {
			super(propertyName, calculateMatches(propertyName, beanClass, maxDistance));
		}

		private static String[] calculateMatches(final String propertyName, Class<?> beanClass, final int maxDistance) {
			final List<String> candidates = new ArrayList<String>();
			ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
				@Override
				public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
					String possibleAlternative = field.getName();
					if (calculateStringDistance(propertyName, possibleAlternative) <= maxDistance) {
						candidates.add(possibleAlternative);
					}
				}
			});
			Collections.sort(candidates);
			return StringUtils.toStringArray(candidates);
		}

		@Override
		public String buildErrorMessage() {
			String propertyName = getPropertyName();
			String[] possibleMatches = getPossibleMatches();
			StringBuilder msg = new StringBuilder();
			msg.append("Bean property '");
			msg.append(propertyName);
			msg.append("' has no matching field. ");

			if (!ObjectUtils.isEmpty(possibleMatches)) {
				appendHintMessage(msg);
			}
			return msg.toString();
		}
	}

}
