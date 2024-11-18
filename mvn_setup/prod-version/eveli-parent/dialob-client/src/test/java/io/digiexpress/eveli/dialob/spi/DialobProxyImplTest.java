package io.digiexpress.eveli.dialob.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class DialobProxyImplTest {

  @Test
  void testSessionIdValidation() {
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId(null));
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId(""));
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId("x"));

    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("a"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("A"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("0"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("1"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("0123456789aBcDeF-123"));
  }
  
}