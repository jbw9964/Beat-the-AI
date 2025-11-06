package org.app.auth.dto.response;

public record Tokens(
        String accessToken,
        String refreshToken
) {

}
