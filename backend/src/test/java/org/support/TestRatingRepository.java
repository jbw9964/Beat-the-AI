package org.support;

import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface TestRatingRepository extends JpaRepository<Rating, Long> {

}
