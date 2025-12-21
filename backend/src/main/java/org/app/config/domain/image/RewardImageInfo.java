package org.app.config.domain.image;

import org.springframework.modulith.*;

@NamedInterface
public record RewardImageInfo(
        Long actualRewardImageId,
        Long overviewRewardImageId
) {

}
