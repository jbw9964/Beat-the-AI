package org.app.problem.domain.search;

public interface ProblemFilter<T> {

    T getFrom();

    T getTo();

    T getEqualTo();

    ProblemFilterType getFilterType();
}
