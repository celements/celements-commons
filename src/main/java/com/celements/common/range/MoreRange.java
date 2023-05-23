package com.celements.common.range;

import static com.google.common.base.MoreObjects.*;

import java.util.function.Function;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * More helpers around {@link com.google.common.collect.Range}
 */
public final class MoreRange {

  private MoreRange() {}

  @NotNull
  public static <T extends Comparable<?>> Range<T> asRange(
      @Nullable T lower, @Nullable T upper) {
    return asRange(lower, upper, null);
  }

  @NotNull
  public static <T extends Comparable<?>> Range<T> asRange(
      @Nullable T lower, @Nullable T upper, @Nullable BoundType type) {
    return asRange(lower, type, upper, type);
  }

  @NotNull
  public static <T extends Comparable<?>> Range<T> asRange(
      @Nullable T lower, @Nullable BoundType lowerType,
      @Nullable T upper, @Nullable BoundType upperType) {
    if ((lower != null) && (upper != null)) {
      return Range.range(
          lower, firstNonNull(lowerType, BoundType.CLOSED),
          upper, firstNonNull(upperType, BoundType.CLOSED));
    } else if (lower != null) {
      return Range.downTo(lower, firstNonNull(lowerType, BoundType.CLOSED));
    } else if (upper != null) {
      return Range.upTo(upper, firstNonNull(upperType, BoundType.CLOSED));
    } else {
      return Range.all();
    }
  }

  @NotNull
  public static <F, T extends Comparable<?>> Range<T> mapRange(
      @NotNull Range<? extends F> range,
      @NotNull Function<F, T> mapper) {
    return asRange(
        range.hasLowerBound() ? mapper.apply(range.lowerEndpoint()) : null,
        range.hasLowerBound() ? range.lowerBoundType() : null,
        range.hasUpperBound() ? mapper.apply(range.upperEndpoint()) : null,
        range.hasUpperBound() ? range.upperBoundType() : null);
  }

}
