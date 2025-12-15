package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserProblemRepository extends JpaRepository<Problem, Long> {

    @Query("""
            select p from Problem p
                where p.user.id = :userId
                and p.schedueldRemoval.doesRemovalScheduled is false
            """)
    Page<Problem> findNonSoftDeletedProblemsByUserId(Long userId, Pageable pageable);

}
