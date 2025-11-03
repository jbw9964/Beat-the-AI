package org.app.user.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserPlayRecordRepository extends JpaRepository<PlayRecord, Long> {

    @Query("""
            select pr from PlayRecord pr
                where pr.user.id = :userId
                and pr.visibility = org.app.entity.PlayRecordVisibility.PUBLIC
            """)
    Page<PlayRecord> findPublicRecordsByUserId(Long userId, Pageable pageable);

    @Query("""
            select pr from PlayRecord pr
            left join fetch pr.scenarioRecords
                where pr.id = :playRecordId
                and pr.visibility = org.app.entity.PlayRecordVisibility.PUBLIC
            """)
    Optional<PlayRecord> findPublicRecordsByIdFetchingScenarioRecords(Long playRecordId);
}
