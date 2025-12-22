package org.app.problem.service;

import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public class ImageInfoBroker {

    private static boolean isPng(byte[] bytes) {
        return bytes != null &&
               bytes.length >= 8 &&
               bytes[0] == (byte) 0x89 &&
               bytes[1] == (byte) 0x50 &&
               bytes[2] == (byte) 0x4E &&
               bytes[3] == (byte) 0x47 &&
               bytes[4] == (byte) 0x0D &&
               bytes[5] == (byte) 0x0A &&
               bytes[6] == (byte) 0x1A &&
               bytes[7] == (byte) 0x0A;
    }

    private static boolean isJpg(byte[] bytes) {
        return bytes != null &&
               bytes.length >= 4 &&
               bytes[0] == (byte) 0xFF &&
               bytes[1] == (byte) 0xD8 &&
               bytes[2] == (byte) 0xFF &&
               (
                       bytes[3] == (byte) 0xE0 ||
                       bytes[3] == (byte) 0xE1
               );
    }

    public MediaType examineImageMediaType(byte[] image) {

        if (isPng(image)) {
            return MediaType.IMAGE_PNG;
        }

        if (isJpg(image)) {
            return MediaType.IMAGE_JPEG;
        }

        log.warn("Unrecognizable image given. Supplying APPLICATION_OCTET_STREAM");

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean acceptableImage(byte[] image) {
        return isPng(image) || isJpg(image);
    }
}
