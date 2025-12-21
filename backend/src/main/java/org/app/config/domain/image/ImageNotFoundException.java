package org.app.config.domain.image;

import org.app.util.exception.*;

public class ImageNotFoundException extends NotFoundException {

    private static final String msg = "주어진 ID 에 해당하는 이미지 (보상 파일) 을 찾을 수 없습니다.";

    public ImageNotFoundException() {
        super(msg);
    }
}
