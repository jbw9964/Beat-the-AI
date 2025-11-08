package org.app.user.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface UserInvitationRepository extends JpaRepository<Invitation, Long> {

    @Query("""
            select i from Invitation i
            inner join fetch i.problem
                where i.code = :code
            """)
    Optional<Invitation> findByCodeFetchingProblem(String code);

    @Query("""
            select i from Invitation i
                where i.code in :codes
            """)
    List<Invitation> findAllByCodes(List<String> codes);

    boolean existsByCode(String code);
}
