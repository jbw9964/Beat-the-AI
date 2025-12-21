package org.app.config.domain.image;

import org.springframework.modulith.*;

@NamedInterface
public interface ImageBlurer {

    byte[] blurImage(byte[] image)
            throws FailedToCreateBlurImageException;

}
