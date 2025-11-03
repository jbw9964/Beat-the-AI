package org.app.user.repository;

import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, Long> {

}
