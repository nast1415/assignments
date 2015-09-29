package ru.spbau.mit;

abstract public class Function2<T1, T2, R> {
    abstract public R apply(T1 arg1, T2 arg2);

    public <R2> Function2<T1, T2, R2> compose(final Function1<? super R, R2> outer) {
        final Function2<T1, T2, R> inner = this;

        return new Function2<T1, T2, R2>() {
            @Override
            public R2 apply(T1 arg1, T2 arg2) {
                return outer.apply(inner.apply(arg1, arg2));
            }
        };
    }

    public Function1<T2, R> bind1(final T1 arg1) {
        final Function2<T1, T2, R> inner = this;

        return new Function1<T2, R>() {
            @Override
            public R apply(T2 arg2) {
                return inner.apply(arg1, arg2);
            }
        };
    }

    public Function1<T1, R> bind2(final T2 arg2) {
        final Function2<T1, T2, R> inner = this;

        return new Function1<T1, R>() {
            @Override
            public R apply(final T1 arg1) {
                return inner.apply(arg1, arg2);
            }
        };
    }

    public Function1<T2, Function1<T1, R>> curry() {
        final Function2<T1, T2, R> inner = this;

        return new Function1<T2, Function1<T1, R>>() {
            @Override
            public Function1<T1, R> apply(final T2 arg2) {
                return new Function1<T1, R>() {
                    @Override
                    public R apply(T1 arg1) {
                        return inner.apply(arg1, arg2);
                    }
                };
            }
        };
    }
}
