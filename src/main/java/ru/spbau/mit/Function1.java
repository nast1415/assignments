package ru.spbau.mit;

abstract public class Function1<T, R> {
    public abstract R apply(T arg);

    public <R2> Function1<T, R2> compose(final Function1<? super R, ? extends R2> outer) {
        final Function1<T, R> inner = this;

        return new Function1<T, R2>() {
            @Override
            public R2 apply(T arg) {
                return outer.apply(inner.apply(arg));
            }
        };
    }
}
