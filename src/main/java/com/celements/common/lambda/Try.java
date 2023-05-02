package com.celements.common.lambda;

import static java.util.Objects.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.common.MoreOptional;
import com.celements.common.lambda.LambdaExceptionUtil.ThrowingFunction;
import com.celements.common.lambda.LambdaExceptionUtil.ThrowingSupplier;

/**
 * Try is a monadic container type which represents a computation that may either result in an
 * exception or return a successfully computed value. Instances of Try are either an instance of
 * {@link TrySuccess} or {@link TryFailure}.
 *
 * Example usage:
 * String text = Try.to(() -> readSomeFileText())
 * .orElseTry(() -> loadDatabaseText())
 * .recover(exc -> exc.getMessage())
 */
@Immutable
public interface Try<T, E extends Exception> {

  @NotNull
  static <T, E extends Exception> Try<T, E> success(@Nullable T value) {
    return new TrySuccess<>(value);
  }

  @NotNull
  static <T, E extends Exception> Try<T, E> failure(@NotNull E exception) {
    return new TryFailure<>(exception);
  }

  @NotNull
  @SuppressWarnings("unchecked")
  static <T, E extends Exception> Try<T, E> to(@NotNull ThrowingSupplier<T, E> supplier) {
    requireNonNull(supplier);
    try {
      return Try.success(supplier.get());
    } catch (Exception exception) {
      return Try.failure((E) exception);
    }
  }

  boolean isSuccess();

  default boolean isFailure() {
    return !isSuccess();
  }

  @Nullable
  T getOrThrow() throws E;

  @NotNull
  Optional<T> getValue();

  @NotNull
  Optional<E> getException();

  @NotNull
  default Stream<T> stream() {
    return MoreOptional.stream(getValue());
  }

  @NotNull
  <R> Try<R, E> map(@NotNull Function<T, R> function);

  @NotNull
  <R> Try<R, E> mapTry(@NotNull ThrowingFunction<T, R, E> function);

  @NotNull
  <R> Try<R, E> flatMap(@NotNull Function<T, Try<R, E>> function);

  @NotNull
  <R> Try<R, E> flatMapTry(@NotNull ThrowingFunction<T, Try<R, E>, E> function);

  @NotNull
  Try<T, E> orElse(@Nullable T value);

  @NotNull
  Try<T, E> orElseGet(@NotNull Supplier<T> supplier);

  @NotNull
  <F extends Exception> Try<T, F> orElseTry(@NotNull ThrowingSupplier<T, F> supplier);

  @Nullable
  T recover(@NotNull Function<E, T> function);

}

class TrySuccess<T, E extends Exception> implements Try<T, E> {

  private final T value;

  TrySuccess(T value) {
    this.value = value;
  }

  @Override
  public boolean isSuccess() {
    return true;
  }

  @Override
  public T getOrThrow() throws E {
    return value;
  }

  @Override
  public Optional<T> getValue() {
    return Optional.ofNullable(value);
  }

  @Override
  public Optional<E> getException() {
    return Optional.empty();
  }

  @Override
  public <R> Try<R, E> map(Function<T, R> function) {
    return Try.success(function.apply(value));
  }

  @Override
  public <R> Try<R, E> mapTry(ThrowingFunction<T, R, E> function) {
    return Try.to(() -> function.apply(value));
  }

  @Override
  public <R> Try<R, E> flatMap(Function<T, Try<R, E>> function) {
    return function.apply(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> Try<R, E> flatMapTry(ThrowingFunction<T, Try<R, E>, E> function) {
    try {
      return function.apply(value);
    } catch (Exception exception) {
      return Try.failure((E) exception);
    }
  }

  @Override
  public Try<T, E> orElse(T value) {
    return this;
  }

  @Override
  public Try<T, E> orElseGet(Supplier<T> supplier) {
    return this;
  }

  @Override
  public <F extends Exception> Try<T, F> orElseTry(ThrowingSupplier<T, F> supplier) {
    return castThis();
  }

  @Override
  public T recover(Function<E, T> function) {
    return value;
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    } else if (that instanceof TrySuccess) {
      return Objects.equals(this.value, ((TrySuccess<?, ?>) that).value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @SuppressWarnings("unchecked")
  private <F extends Exception> Try<T, F> castThis() {
    return (Try<T, F>) this;
  }

}

class TryFailure<T, E extends Exception> implements Try<T, E> {

  private final E exception;

  TryFailure(E exception) {
    this.exception = requireNonNull(exception);
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public T getOrThrow() throws E {
    throw exception;
  }

  @Override

  public Optional<T> getValue() {
    return Optional.empty();
  }

  @Override

  public Optional<E> getException() {
    return Optional.ofNullable(exception);
  }

  @Override
  public <R> Try<R, E> map(Function<T, R> function) {
    return castThis();
  }

  @Override
  public <R> Try<R, E> mapTry(ThrowingFunction<T, R, E> function) {
    return castThis();
  }

  @Override
  public <R> Try<R, E> flatMap(Function<T, Try<R, E>> function) {
    return castThis();
  }

  @Override
  public <R> Try<R, E> flatMapTry(ThrowingFunction<T, Try<R, E>, E> function) {
    return castThis();
  }

  @Override
  public Try<T, E> orElse(T value) {
    return Try.success(value);
  }

  @Override
  public Try<T, E> orElseGet(Supplier<T> supplier) {
    return Try.success(supplier.get());
  }

  @Override
  public <F extends Exception> Try<T, F> orElseTry(ThrowingSupplier<T, F> supplier) {
    return Try.to(supplier);
  }

  @Override
  public T recover(Function<E, T> function) {
    return function.apply(exception);
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    } else if (that instanceof TryFailure) {
      return Objects.equals(this.exception, ((TryFailure<?, ?>) that).exception);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(exception);
  }

  @SuppressWarnings("unchecked")
  private <R> Try<R, E> castThis() {
    return (Try<R, E>) this;
  }

}
