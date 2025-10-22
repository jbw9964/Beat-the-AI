package org.app.util;

import jakarta.validation.*;
import java.lang.annotation.*;

/**
 * {@code enum} 속성 validation 을 위한 annotation.
 * <p>
 * {@link #target}, {@link #message} 속성만 잘 구성하면 사용 가능
 *
 * <li>간단한 사용 방법 :</li>
 * <pre>
 *     {@code
 *      public record SignInUpRequest(
 *          @ValidEnum(target = AuthType.class, message = "authType 을 매칭할 수 없습니다.")
 *          AuthType authType,
 *          @NotBlank(message = "토큰은 비어있을 수 없습니다.")
 *          String token
 *      ) {
 *          public enum AuthType {
 *              APPLE, GOOGLE, KAKAO
 *          }
 *      }
 *     }
 *     {@code
 *     {
 *      "success": false,
 *      "code": 400,
 *      "data": [
 *          "authType: authType 을 매칭할 수 없습니다."
 *      ],
 *      "message": "Bad Request"
 *      }
 *     }
 * </pre>
 *
 * @see CustomEnumValidator
 */
@Constraint(validatedBy = {CustomEnumValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {


    /**
     * Validation 에 사용할 {@code enum} class
     */
    Class<? extends Enum<?>> target();

    /**
     * 실패했을 때 보여줄 메시지
     */
    String message() default "Invalid Enum Value";

    /**
     * 뭔지 잘 모름. Validation grouping(?) 에 사용될 수 있다고 함.
     *
     * @see Constraint
     */
    Class<?>[] groups() default {};

    /**
     * 뭔지 잘 모름. 뭔가 확장하는(?) 용도로 사용될 수 있다 함.
     *
     * @see Constraint
     */
    Class<? extends Payload>[] payload() default {};
}
