package io.resys.limaone.tests;

import org.junit.jupiter.api.Test;

import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;

@DialobTest( enabled = true )
public class FormTest {
  
  @Test
  @DialobResetDB
  public void test(FormUrl formUrl) {
    System.out.println(formUrl);
  }

}
