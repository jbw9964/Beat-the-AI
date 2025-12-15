package org.app.problem.domain.search.order;

import com.querydsl.core.types.*;

public interface ProblemOrder {

    OrderSpecifier<?> orderSpecifier();

    ProblemOrderType getOrderType();
}
