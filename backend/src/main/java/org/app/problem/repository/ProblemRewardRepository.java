package org.app.problem.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ProblemRewardRepository extends JpaRepository<ProblemReward, Long> {

    Page<ProblemReward> findByProblemId(Long problemId, Pageable pageable);
}
