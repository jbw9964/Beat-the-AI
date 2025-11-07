package org.support;

import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface TestUserRepository extends JpaRepository<User, Long> {

}
