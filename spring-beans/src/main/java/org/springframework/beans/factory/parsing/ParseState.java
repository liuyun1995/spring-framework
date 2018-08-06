package org.springframework.beans.factory.parsing;

import java.util.Stack;

//解析状态
public final class ParseState {

	private static final char TAB = '\t';
	private final Stack<Entry> state;

	public ParseState() {
		this.state = new Stack<Entry>();
	}

	@SuppressWarnings("unchecked")
	private ParseState(ParseState other) {
		this.state = (Stack<Entry>) other.state.clone();
	}

	public void push(Entry entry) {
		this.state.push(entry);
	}

	public void pop() {
		this.state.pop();
	}

	public Entry peek() {
		return this.state.empty() ? null : this.state.peek();
	}

	public ParseState snapshot() {
		return new ParseState(this);
	}

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

	//实体接口
	public interface Entry {

	}

}
