package ru.spbau.mit;

import com.sun.org.apache.bcel.internal.generic.NEW;
import java.util.Objects;

abstract public class Predicate<T> {
    abstract boolean apply(T arg);

    public static final Predicate ALWAYS_TRUE = new Predicate() {
        @Override
        public boolean apply(Object arg) {
            return true;
        }
    };

    public static final Predicate ALWAYS_FALSE = new Predicate() {

        @Override
        boolean apply(Object arg) {
            return false;
        }
    };

    public Predicate<T> or (final Predicate<? super T> outer) {
        final Predicate<T> inner = this;

        return  new Predicate<T>() {
            @Override
            boolean apply(T arg) {
                return (inner.apply(arg) || outer.apply(arg));
            }
        };
    }

    public Predicate<T> and (final Predicate<? super T> outer) {
        final Predicate<T> inner = this;

        return  new Predicate<T>() {
            @Override
            boolean apply(T arg) {
                return (inner.apply(arg) && outer.apply(arg));
            }
        };
    }

    public Predicate<T> not() {
        final Predicate<T> inner = this;

        return new Predicate<T>() {
            @Override
            boolean apply(T arg) {
                return !inner.apply(arg);
            }
        };
    }

}
