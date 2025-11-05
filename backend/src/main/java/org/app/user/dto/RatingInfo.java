package org.app.user.dto;

import java.time.*;

public record RatingInfo(
        Long ratingId,
        Long problemId,
        String comment,
        int score,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

}
