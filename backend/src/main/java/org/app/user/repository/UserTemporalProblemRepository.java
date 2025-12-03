package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserTemporalProblemRepository extends JpaRepository<TemporalProblem, Long> {

    Page<TemporalProblem> findByUserId(Long userId, Pageable pageable);

}
