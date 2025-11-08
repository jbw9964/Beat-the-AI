package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserRatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findByUserId(Long userId, Pageable pageable);

}
