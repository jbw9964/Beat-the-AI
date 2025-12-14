package org.app.problem.repository;

import java.util.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;

public interface DynamicProblemSearchRepository {

    String JPA_COMMENT_KEY = "org.hibernate.comment";

    // TODO : tx 어떻게 구성해야 할까? read only? 격리 레벨은?
    @Transactional(
            readOnly = true
            //, isolation = Isolation.REPEATABLE_READ
    )
    Page<Problem> searchPublicProblemWithFilters(
            List<ProblemFilter<?>> filters,
            List<ProblemOrder> orders,
            Pageable pageable
    ) throws FilterTypeMismatchException, FailedToCastFilterValueException;

}
