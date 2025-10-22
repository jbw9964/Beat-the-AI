package org.app.auth.dto;

public record Tokens(
        String accessToken,
        String refreshToken
) {

}
