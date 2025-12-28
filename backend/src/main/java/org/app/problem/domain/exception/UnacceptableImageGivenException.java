package org.app.problem.domain.exception;

import org.app.util.exception.*;

public class UnacceptableImageGivenException extends BadRequestException {

    private static final String message = "허용되지 않는 이미지 파일이 제공되었습니다. "
                                          + "PNG 또는 JPG 이미지를 제공해 주세요.";

    public UnacceptableImageGivenException() {
        super(message);
    }
}
