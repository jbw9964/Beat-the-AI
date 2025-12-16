package org.app.problem.repository;

import org.app.entity.*;
import org.springframework.data.jpa.repository.*;

public interface ProblemUserRepository extends JpaRepository<User, Long> {

}
