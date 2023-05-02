package com.celements.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;

public final class MoreObjectsCel {

  private MoreObjectsCel() {}

  @NotNull
  public static <F, T> Optional<T> tryCast(@Nullable F candidate, @NotNull Class<T> targetClass) {
    return targetClass.isInstance(candidate)
        ? Optional.of(targetClass.cast(candidate))
        : Optional.empty();
  }

  /**
   * When the returned {@code Function} is passed as an argument to {@link Stream#flatMap}, the
   * result is a stream of instances of {@code targetClass}.
   *
   * <pre>
   *   Stream<SourceType> streamSource = ...
   *   Stream<TargetType> streamTarget = streamSource.flatMap(tryCast(TargetType.class))
   * </pre>
   */
  @NotNull
  public static <F, T> Function<F, Stream<T>> tryCast(@NotNull Class<T> targetClass) {
    return candidate -> targetClass.isInstance(candidate)
        ? Stream.of(targetClass.cast(candidate))
        : Stream.empty();
  }

  /**
   * When the returned {@code Function} is passed as an argument to {@link Optional#flatMap}, the
   * result is an optional of {@code targetClass}.
   *
   * <pre>
   *   Optional<SourceType> optSource = ...
   *   Optional<TargetType> optTarget = optSource.flatMap(tryCastOpt(TargetType.class))
   * </pre>
   */
  @NotNull
  public static <F, T> Function<F, Optional<T>> tryCastOpt(@NotNull Class<T> targetClass) {
    return candidate -> MoreObjectsCel.tryCast(candidate, targetClass);
  }

  /**
   * @deprecated since 5.2, instead use {@link MoreOptional#toJavaUtil}
   */
  @Deprecated
  @NotNull
  public static <F, T> Function<F, Optional<T>> optToJavaUtil(
      @NotNull Function<F, com.google.common.base.Optional<T>> func) {
    return MoreOptional.toJavaUtil(func);
  }

  /**
   * @deprecated since 5.2, instead use {@link MoreOptional#asNonBlank}
   */
  @Deprecated
  @NotNull
  public static Optional<String> asOptNonBlank(@Nullable String str) {
    return MoreOptional.asNonBlank(str);
  }

  /**
   * @deprecated since 5.2, instead use {@link MoreOptional#findFirstPresent(Supplier...)}
   */
  @Deprecated
  @NotNull
  @SafeVarargs
  public static <T> Optional<T> findFirstPresent(@NotNull Supplier<Optional<T>>... suppliers) {
    return MoreOptional.findFirstPresent(suppliers);
  }

  /**
   * Similar to {@link Defaults#defaultValue(Class)} but also supports {@link String} and commonly
   * non-nullable types, see {@link #defaultValueNonNullable(Class)}
   */
  @Nullable
  public static <T> T defaultValue(@Nullable Class<T> type) {
    return defaultValue(type, false);
  }

  /**
   * Similar to {@link Defaults#defaultValue(Class)} but also supports {@link String} and commonly
   * non-nullable types, see {@link #defaultMutableValueNonNullable(Class)}
   */
  @Nullable
  public static <T> T defaultMutableValue(@Nullable Class<T> type) {
    return defaultValue(type, true);
  }

  private static <T> T defaultValue(Class<T> type, boolean mutable) {
    T value = (type != null) ? Defaults.defaultValue(Primitives.unwrap(type)) : null;
    if (value != null) {
      return value;
    } else if (String.class.equals(type)) {
      return type.cast("");
    } else {
      return defaultValueNonNullable(type, mutable);
    }
  }

  /**
   * @return the default immutable value for the following commonly non-nullable types:
   *         List, Set, Queue, Iterable, Map, Stream, Optional
   */
  @Nullable
  public static <T> T defaultValueNonNullable(@Nullable Class<T> type) {
    return defaultValueNonNullable(type, false);
  }

  /**
   * @return the default mutable (if available) value for the following commonly non-nullable types:
   *         List, Set, Queue, Iterable, Map, Stream, Optional
   */
  @Nullable
  public static <T> T defaultMutableValueNonNullable(@Nullable Class<T> type) {
    return defaultValueNonNullable(type, true);
  }

  private static <T> T defaultValueNonNullable(Class<T> type, boolean mutable) {
    if (type == null) {
      return null;
    }
    Object ret = null;
    if (List.class.isAssignableFrom(type)) {
      ret = (mutable ? new ArrayList<>() : ImmutableList.of());
    } else if (Set.class.isAssignableFrom(type)) {
      ret = (mutable ? new LinkedHashSet<>() : ImmutableSet.of());
    } else if (Queue.class.isAssignableFrom(type)) {
      ret = new LinkedList<>();
    } else if (Iterable.class.isAssignableFrom(type)) {
      ret = defaultValue(List.class, mutable);
    } else if (Properties.class.isAssignableFrom(type)) {
      ret = new Properties();
    } else if (Map.class.isAssignableFrom(type)) {
      ret = (mutable ? new LinkedHashMap<>() : ImmutableMap.of());
    } else if (Stream.class.isAssignableFrom(type)) {
      ret = Stream.empty();
    } else if (Optional.class.isAssignableFrom(type)) {
      ret = Optional.empty();
    } else if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
      ret = com.google.common.base.Optional.absent();
    }
    return type.cast(ret);
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> stream(Object value) {
    if (value instanceof Stream) {
      return (Stream<T>) value;
    } else if (value instanceof Collection) {
      return ((Collection<T>) value).stream();
    } else if (value instanceof Spliterator) {
      return StreamSupport.stream((Spliterator<T>) value, false);
    } else if (value instanceof Iterable) {
      return Streams.stream((Iterable<T>) value);
    } else if (value instanceof Iterator) {
      return Streams.stream((Iterator<T>) value);
    } else if (value instanceof Optional) {
      return MoreOptional.stream((Optional<T>) value);
    } else if (value instanceof com.google.common.base.Optional) {
      return MoreOptional.stream(((com.google.common.base.Optional<T>) value).toJavaUtil());
    } else if (value != null) {
      return Stream.of((T) value);
    } else {
      return Stream.empty();
    }
  }

  private static final List<Class<?>> UTIL_CLASSES = ImmutableList.of(Stream.class,
      List.class, Set.class, Queue.class, Collection.class, Iterable.class,
      Properties.class, Map.class,
      Spliterator.class, Iterator.class);

  @SuppressWarnings("unchecked")
  public static <T> Stream<Class<T>> findAssignableUtilClasses(Object value) {
    if (value == null) {
      return Stream.empty();
    }
    return UTIL_CLASSES.stream()
        .filter(c -> c.isInstance(value))
        .map(c -> (Class<T>) c);
  }

}
