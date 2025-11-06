package org;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;

@SpringBootTest
@Import(TestConfig.class)
public abstract class IntegrationTestSupport {

}
