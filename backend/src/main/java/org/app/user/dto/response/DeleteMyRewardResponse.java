package org.app.user.dto.response;

import java.util.*;

public record DeleteMyRewardResponse(
        List<Long> deletedRewardIds
) {

}
