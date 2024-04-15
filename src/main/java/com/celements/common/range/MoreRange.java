package com.celements.common.range;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.Optional;
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

  /**
   * Builder for {@link Range}. The range is unbounded by default, i.e. lower and upper are null,
   * corresponding to {@link Range#all()}. The default type for both bounds is
   * {@link BoundType#CLOSED}.
   */
  public static class Builder<T extends Comparable<?>> {

    private boolean autoCorrect = false;
    private BoundType defaultType = BoundType.CLOSED;
    private T lower;
    private BoundType lowerType;
    private T upper;
    private BoundType upperType;

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

    /**
     * Automatically correct the range if it becomes invalid, enforcing lower <= upper. The default
     * is false, leading instead to fast failure on {@link #build()} if the range is invalid.
     *
     * @see #isValid()
     */
    public @NotNull Builder<T> autoCorrect() {
      this.autoCorrect = true;
      return this;
    }

    /**
     * Set the default type for both bounds if not explicitly set by {@link #lowerType} or
     * {@link #upperType}. The fallback is {@link BoundType#CLOSED}.
     */
    public @NotNull Builder<T> defaultType(@NotNull BoundType defaultType) {
      this.defaultType = checkNotNull(defaultType);
      return this;
    }

    public @NotNull Optional<T> getLower() {
      return Optional.ofNullable(lower);
    }

    /**
     * Set the lower bound of the range, null means no lower bound.
     */
    public @NotNull Builder<T> lower(@Nullable T lower) {
      this.lower = lower;
      if (autoCorrect && !isValid()) {
        upper(lower); // upper < lower
      }
      return this;
    }

    public @NotNull BoundType getLowerType() {
      return firstNonNull(lowerType, defaultType);
    }

    /**
     * Set the type of the lower bound.
     */
    public @NotNull Builder<T> lowerType(@NotNull BoundType lowerType) {
      this.lowerType = checkNotNull(lowerType);
      return this;
    }

    public @NotNull Optional<T> getUpper() {
      return Optional.ofNullable(upper);
    }

    /*
     * Set the upper bound of the range, null means no upper bound.
     */
    public @NotNull Builder<T> upper(@Nullable T upper) {
      this.upper = upper;
      if (autoCorrect && !isValid()) {
        lower(upper); // lower > upper
      }
      return this;
    }

    public @NotNull BoundType getUpperType() {
      return firstNonNull(upperType, defaultType);
    }

    /**
     * Set the type of the upper bound.
     */
    public @NotNull Builder<T> upperType(@NotNull BoundType upperType) {
      this.upperType = checkNotNull(upperType);
      return this;
    }

    /**
     * Check if the range is valid, i.e. lower <= upper if both are set
     */
    @SuppressWarnings("unchecked")
    public boolean isValid() {
      return (lower == null) || (upper == null) || (((Comparable<T>) lower).compareTo(upper) <= 0);
    }

    public @NotNull Range<T> build() {
      if ((lower != null) && (upper != null)) {
        return Range.range(lower, getLowerType(), upper, getUpperType());
      } else if (lower != null) {
        return Range.downTo(lower, getLowerType());
      } else if (upper != null) {
        return Range.upTo(upper, getUpperType());
      } else {
        return Range.all();
      }
    }
  }

}
