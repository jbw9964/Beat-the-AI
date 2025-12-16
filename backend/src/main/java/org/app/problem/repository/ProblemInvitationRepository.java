package org.app.problem.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface ProblemInvitationRepository extends JpaRepository<Invitation, Long> {

    @Query("""
            select i from Invitation i
            where i.code in :codes
            """)
    List<Invitation> findAllByCodes(List<String> codes);

}
