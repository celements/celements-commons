package com.celements.common.range;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.function.Function;
import java.util.function.Predicate;

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
    return new Builder<T>()
        .lower(lower).lowerType(firstNonNull(lowerType, BoundType.CLOSED))
        .upper(upper).upperType(firstNonNull(upperType, BoundType.CLOSED))
        .build();
  }

  @NotNull
  public static <F extends Comparable<?>, T extends Comparable<?>> Range<T> mapRange(
      @NotNull Range<? extends F> range,
      @NotNull Function<? super F, ? extends T> mapper) {
    checkNotNull(range);
    checkNotNull(mapper);
    var ret = new Builder<T>();
    if (range.hasLowerBound()) {
      ret.lower(mapper.apply(range.lowerEndpoint())).lowerType(range.lowerBoundType());
    }
    if (range.hasUpperBound()) {
      ret.upper(mapper.apply(range.upperEndpoint())).upperType(range.upperBoundType());
    }
    return ret.build();
  }

  @NotNull
  public static <T extends Comparable<?>> Range<T> filterRange(
      @NotNull Range<? extends T> range,
      @NotNull Predicate<T> filter) {
    checkNotNull(filter);
    return mapRange(range, r -> filter.test(r) ? r : null);
  }

  @NotNull
  public static <T extends Comparable<?>> Range<T> withoutLowerBound(
      @NotNull Range<? extends T> range) {
    return new Builder<T>(range).lower(null).build();
  }

  @NotNull
  public static <T extends Comparable<?>> Range<T> withoutUpperBound(
      @NotNull Range<? extends T> range) {
    return new Builder<T>(range).upper(null).build();
  }

  public static class Builder<T extends Comparable<?>> {

    private boolean autoCorrect = false;
    private T lower;
    private BoundType lowerType = BoundType.CLOSED;
    private T upper;
    private BoundType upperType = BoundType.CLOSED;

    public Builder() {}

    public Builder(@Nullable Range<? extends T> range) {
      this();
      if (range == null) {
        return;
      }
      if (range.hasLowerBound()) {
        lower(range.lowerEndpoint());
        lowerType(range.lowerBoundType());
      }
      if (range.hasUpperBound()) {
        upper(range.upperEndpoint());
        upperType(range.upperBoundType());
      }
    }

    public Builder<T> autoCorrect() {
      this.autoCorrect = true;
      return this;
    }

    public T getLower() {
      return lower;
    }

    public Builder<T> lower(@Nullable T lower) {
      this.lower = lower;
      if (autoCorrect && !isValid()) {
        upper(lower); // upper < lower
      }
      return this;
    }

    public BoundType getLowerType() {
      return lowerType;
    }

    public Builder<T> lowerType(@NotNull BoundType lowerType) {
      this.lowerType = checkNotNull(lowerType);
      return this;
    }

    public T getUpper() {
      return upper;
    }

    public Builder<T> upper(@Nullable T upper) {
      this.upper = upper;
      if (autoCorrect && !isValid()) {
        lower(upper); // lower > upper
      }
      return this;
    }

    public BoundType getUpperType() {
      return upperType;
    }

    public Builder<T> upperType(@NotNull BoundType upperType) {
      this.upperType = checkNotNull(upperType);
      return this;
    }

    @SuppressWarnings("unchecked")
    public boolean isValid() {
      return (lower == null) || (upper == null) || (((Comparable<T>) lower).compareTo(upper) <= 0);
    }

    public Range<T> build() {
      if ((lower != null) && (upper != null)) {
        return Range.range(lower, lowerType, upper, upperType);
      } else if (lower != null) {
        return Range.downTo(lower, lowerType);
      } else if (upper != null) {
        return Range.upTo(upper, upperType);
      } else {
        return Range.all();
      }
    }
  }

}
