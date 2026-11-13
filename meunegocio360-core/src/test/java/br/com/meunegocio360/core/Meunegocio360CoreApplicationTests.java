package br.com.meunegocio360.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {
      "JWT_SECRET=dGVzdC1pbnZhbGlkLXNlY3JldC1mb3ItamF2YS10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
      "CORS_ALLOWED_ORIGINS=http://localhost:5173"
    })
@ActiveProfiles("test")
class Meunegocio360CoreApplicationTests {

  @Test
  void contextLoads() {
  }
}
