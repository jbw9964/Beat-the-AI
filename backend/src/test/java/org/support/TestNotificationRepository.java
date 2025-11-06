package org.support;

import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface TestNotificationRepository extends JpaRepository<Notification, Long> {

}
