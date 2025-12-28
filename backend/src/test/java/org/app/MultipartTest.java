package org.app;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import org.*;
import org.MultipartTestController.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.*;

@AutoConfigureMockMvc
@Import(MultipartTestController.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MultipartTest extends IntegrationTestSupport {

    private static final String BASE_URL = MultipartTestController.BASE_URL;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objMapper;

    @Test
    @DisplayName("Multipart 와 body 가 결합된 요청을 받을 수 있다.")
    void testWithBody() throws Exception {
        String originalFilename = "file.txt";
        String contentType = "text/plain";
        long size = 256;

        MockMultipartFile multipartFile = new MockMultipartFile(
                MultipartTestController.MULTIPART_FILE_NAME, originalFilename,
                contentType, new byte[(int) size]
        );

        Body body = new Body("Hello!");
        MockMultipartFile bodyPart = new MockMultipartFile(
                MultipartTestController.MULTIPART_BODY_NAME,
                "", MediaType.APPLICATION_JSON_VALUE,
                objMapper.writeValueAsBytes(body)
        );

        mvc.perform(
                        multipart(HttpMethod.POST, BASE_URL)
                                .file(multipartFile)
                                .file(bodyPart)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Multipart 와 body 가 결합된 요청에도 validation 이 적용된다.")
    void testBadReqest() throws Exception {
        String originalFilename = "file.txt";
        String contentType = "text/plain";
        long size = 256;

        MockMultipartFile multipartFile = new MockMultipartFile(
                MultipartTestController.MULTIPART_FILE_NAME, originalFilename,
                contentType, new byte[(int) size]
        );

        Body body = new Body(null);
        MockMultipartFile bodyPart = new MockMultipartFile(
                MultipartTestController.MULTIPART_BODY_NAME,
                "", MediaType.APPLICATION_JSON_VALUE,
                objMapper.writeValueAsBytes(body)
        );

        mvc.perform(
                        multipart(HttpMethod.POST, BASE_URL)
                                .file(multipartFile)
                                .file(bodyPart)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().string(containsString(Body.ERR_MSG))
                )
                .andDo(print());
    }
}
