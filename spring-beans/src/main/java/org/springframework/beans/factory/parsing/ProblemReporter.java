package org.springframework.beans.factory.parsing;

public interface ProblemReporter {

	void fatal(Problem problem);

	void error(Problem problem);

	void warning(Problem problem);

}
