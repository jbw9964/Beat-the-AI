package org.app.problem.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ProblemRepository
        extends JpaRepository<Problem, Long>, DynamicProblemSearchRepository {

    @Query("""
            select p from Problem p
            left join fetch p.problemAggregation
                where p.visibility = org.app.entity.ProblemVisibility.PUBLIC
                and p.scheduledRemoval.doesRemovalScheduled is false
            """)
    Page<Problem> findPublicAndNonSoftDeletedProblemsFetchingAgg(Pageable pageable);

    @Query("""
            select p from Problem p
            inner join fetch p.user
            left join fetch p.problemAggregation
                where p.id = :problemId
                and p.scheduledRemoval.doesRemovalScheduled is false
            """)
    Optional<Problem> findNonSoftDeletedProblemFetchingUserAndAgg(Long problemId);
}
