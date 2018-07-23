package org.springframework.beans.factory.parsing;

import java.util.Stack;


public final class ParseState {

	/**
	 * Tab character used when rendering the tree-style representation.
	 */
	private static final char TAB = '\t';

	/**
	 * Internal {@link Stack} storage.
	 */
	private final Stack<Entry> state;


	/**
	 * Create a new {@code ParseState} with an empty {@link Stack}.
	 */
	public ParseState() {
		this.state = new Stack<Entry>();
	}

	/**
	 * Create a new {@code ParseState} whose {@link Stack} is a {@link Object#clone clone}
	 * of that of the passed in {@code ParseState}.
	 */
	@SuppressWarnings("unchecked")
	private ParseState(ParseState other) {
		this.state = (Stack<Entry>) other.state.clone();
	}


	/**
	 * Add a new {@link Entry} to the {@link Stack}.
	 */
	public void push(Entry entry) {
		this.state.push(entry);
	}

	/**
	 * Remove an {@link Entry} from the {@link Stack}.
	 */
	public void pop() {
		this.state.pop();
	}

	/**
	 * Return the {@link Entry} currently at the top of the {@link Stack} or
	 * {@code null} if the {@link Stack} is empty.
	 */
	public Entry peek() {
		return this.state.empty() ? null : this.state.peek();
	}

	/**
	 * Create a new instance of {@link ParseState} which is an independent snapshot
	 * of this instance.
	 */
	public ParseState snapshot() {
		return new ParseState(this);
	}


	/**
	 * Returns a tree-style representation of the current {@code ParseState}.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < this.state.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(x));
		}
		return sb.toString();
	}


	/**
	 * Marker interface for entries into the {@link ParseState}.
	 */
	public interface Entry {

	}

}
