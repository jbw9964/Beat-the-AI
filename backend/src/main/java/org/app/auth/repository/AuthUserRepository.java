package org.app.auth.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface AuthUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

}
