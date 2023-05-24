package com.celements.common.range;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class MoreRangeTest {

  private final int lower = 5;
  private final int upper = 69;

  @Test
  public void test_asRange_all() {
    assertEquals(Range.all(), MoreRange.asRange(null, null));
  }

  @Test
  public void test_asRange_atLeast() {
    assertEquals(Range.atLeast(lower), MoreRange.asRange(lower, null));
  }

  @Test
  public void test_asRange_greaterThan() {
    assertEquals(Range.greaterThan(lower), MoreRange.asRange(lower, null, BoundType.OPEN));
  }

  @Test
  public void test_asRange_atMost() {
    assertEquals(Range.atMost(upper), MoreRange.asRange(null, upper));
  }

  @Test
  public void test_asRange_lessThan() {
    assertEquals(Range.lessThan(upper), MoreRange.asRange(null, upper, BoundType.OPEN));
  }

  @Test
  public void test_asRange_closed() {
    assertEquals(Range.closed(lower, upper), MoreRange.asRange(lower, upper));
    assertEquals(Range.closed(lower, upper), MoreRange.asRange(lower, upper, BoundType.CLOSED));
    assertEquals(Range.closed(lower, upper),
        MoreRange.asRange(lower, BoundType.CLOSED, upper, BoundType.CLOSED));
  }

  @Test
  public void test_asRange_open() {
    assertEquals(Range.open(lower, upper), MoreRange.asRange(lower, upper, BoundType.OPEN));
    assertEquals(Range.open(lower, upper),
        MoreRange.asRange(lower, BoundType.OPEN, upper, BoundType.OPEN));
  }

  @Test
  public void test_asRange_closedOpen() {
    assertEquals(Range.closedOpen(lower, upper),
        MoreRange.asRange(lower, BoundType.CLOSED, upper, BoundType.OPEN));
  }

  @Test
  public void test_asRange_openClosed() {
    assertEquals(Range.openClosed(lower, upper),
        MoreRange.asRange(lower, BoundType.OPEN, upper, BoundType.CLOSED));
  }

  @Test
  public void test_mapRange() {
    Range<Integer> range = Range.openClosed(lower, upper);
    assertEquals(Range.openClosed("" + lower, "" + upper),
        MoreRange.mapRange(range, i -> Integer.toString(i)));
  }

  @Test
  public void test_mapRange_toNull() {
    Range<Integer> range = Range.openClosed(lower, upper);
    assertEquals(Range.all(), MoreRange.mapRange(range, i -> null));
  }

}
