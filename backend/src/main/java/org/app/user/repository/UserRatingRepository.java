package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserRatingRepository extends JpaRepository<Rating, Long> {

    @Query(
            value = """
                    select r from Rating r
                    inner join r.problem
                        where r.userId = :userId
                    """,
            countQuery = """
                    select count(r) from Rating r
                        where r.userId = :userId
                        and r.problem.id is not null
                    """)
    Page<Rating> findByUserId(Long userId, Pageable pageable);

}
