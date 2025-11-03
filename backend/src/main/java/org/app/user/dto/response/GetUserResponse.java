package org.app.user.dto.response;

public record GetUserResponse(
        Long userId,
        String username,
        String email,
        String thumbnailUrl,
        boolean isMine
) {

}
