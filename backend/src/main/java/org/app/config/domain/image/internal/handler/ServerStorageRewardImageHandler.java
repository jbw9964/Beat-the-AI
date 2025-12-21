package org.app.config.domain.image.internal.handler;

import java.util.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

@Primary
@Component
public class ServerStorageRewardImageHandler extends AbstractRewardImageHandler {

    public ServerStorageRewardImageHandler(
            ActualRewardImageRepository actualImageRepo,
            OverviewRewardImageRepository overviewImageRepo,
            List<RewardImageInvokerStrategy> strategies
    ) {
        super(actualImageRepo, overviewImageRepo, strategies);
    }

    @Override
    public RewardStorageType savingStorageType() {
        return RewardStorageType.SERVER;
    }
}
