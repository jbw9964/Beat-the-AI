package org.app.user.repository;

import java.util.*;
import org.app.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserGainedRewardRepository extends JpaRepository<GainedReward, Long> {

    Page<GainedReward> findByPlayRecordId(Long playRecordId, Pageable pageable);

    List<GainedReward> findAllByPlayRecordId(Long playRecordId);

    @Query("""
            delete GainedReward gr
                where gr.id in :ids
            """)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByIds(Iterable<Long> ids);
}
