package org.springframework.beans.factory.parsing;

//问题报告器
public interface ProblemReporter {

	void fatal(Problem problem);

	void error(Problem problem);

	void warning(Problem problem);

}
