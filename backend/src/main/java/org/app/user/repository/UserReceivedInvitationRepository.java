package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserReceivedInvitationRepository extends JpaRepository<ReceivedInvitation, Long> {

    Page<ReceivedInvitation> findByUserId(Long userId, Pageable pageable);

}
