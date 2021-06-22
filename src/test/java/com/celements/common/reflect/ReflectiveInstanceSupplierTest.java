package com.celements.common.reflect;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;

import com.google.common.base.Supplier;

public class ReflectiveInstanceSupplierTest {

  @Test
  public void test_get() {
    Supplier<Date> supplier = new ReflectiveInstanceSupplier<>(Date.class);
    Date date = supplier.get();
    assertNotSame(supplier.get(), date);
  }

  public void test_get_IAE() {
    Supplier<ZonedDateTime> supplier = new ReflectiveInstanceSupplier<>(ZonedDateTime.class);
    IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
        () -> supplier.get());
    assertSame(NoSuchMethodException.class, iae.getCause().getClass());
  }

  public void test_NPE() {
    assertThrows(NullPointerException.class,
        () -> new ReflectiveInstanceSupplier<>(null));
  }

}
