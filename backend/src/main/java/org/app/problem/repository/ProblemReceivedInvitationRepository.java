package org.app.problem.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface ProblemReceivedInvitationRepository
        extends JpaRepository<ReceivedInvitation, Long> {

    List<ReceivedInvitation> findAllByUserIdAndProblemId(Long userId, Long problemId);

}
