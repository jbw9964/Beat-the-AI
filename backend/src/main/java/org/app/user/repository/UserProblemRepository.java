package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserProblemRepository extends JpaRepository<Problem, Long> {

    Page<Problem> findByUserId(Long userId, Pageable pageable);
}
