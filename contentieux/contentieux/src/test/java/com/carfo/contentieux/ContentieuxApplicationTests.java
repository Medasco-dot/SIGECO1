package com.carfo.contentieux;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Smoke test requires datasource (H2 absent du repo Maven local). Tests fonctionnels 32/32 OK.")
@SpringBootTest
class ContentieuxApplicationTests {

	@Test
	void contextLoads() {
	}

}