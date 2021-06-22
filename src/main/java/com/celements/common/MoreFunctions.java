package com.celements.common;

import static com.google.common.base.Preconditions.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.function.ForkFunction;

public final class MoreFunctions {

  private MoreFunctions() {}

  /**
   * @deprecated since 5.2, instead use method reference {@code Object::hashCode}
   */
  @Deprecated
  @NotNull
  public static com.google.common.base.Function<Object, Integer> hashCodeFunction() {
    return o -> checkNotNull(o).hashCode();
  }

  /**
   * @return the given function as a consumer
   */
  @NotNull
  public static <F, T> Consumer<F> asConsumer(@Nullable Function<F, T> func) {
    return (func != null) ? func::apply : f -> {};
  }

  /**
   * @return the given consumer as a {@link Function#identity}.
   */
  @NotNull
  public static <T> Function<T, T> asFunction(@Nullable Consumer<T> consumer) {
    return (consumer != null) ? t -> {
      consumer.accept(t);
      return t;
    } : t -> t;
  }

  /**
   * @see ForkFunction
   */
  @NotNull
  public static <F, T> ForkFunction<F, T> fork(@Nullable Predicate<F> when,
      @Nullable Function<F, T> thenMap) {
    return fork(when, thenMap, null);
  }

  /**
   * @see ForkFunction
   */
  @NotNull
  public static <F, T> ForkFunction<F, T> fork(@Nullable Predicate<F> when,
      @Nullable Function<F, T> thenMap, @Nullable Function<F, T> elseMap) {
    return new ForkFunction<>(when, thenMap, elseMap);
  }

}
