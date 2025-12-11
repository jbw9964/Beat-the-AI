package org.app.problem.domain.search.order;

import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;

public interface OrderRequestAdaptingStrategy {

    ProblemOrder toOrder(OrderingRequest request)
            throws OrderTypeMismatchException;

    ProblemOrderType handleableOrderType();

}
