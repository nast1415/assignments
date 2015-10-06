package ru.spbau.mit;

import com.sun.org.apache.bcel.internal.generic.NEW;
import java.util.Objects;

abstract public class Predicate<T> {
    abstract boolean apply(T arg);

    public static final Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public boolean apply(Object arg) {
            return true;
        }
    };

    public static final Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {

        @Override
        boolean apply(Object arg) {
            return false;
        }
    };

    public <T1 extends T> Predicate<T1> or (final Predicate<? super T1> outer) {
        final Predicate<T> inner = this;

        return  new Predicate<T1>() {
            @Override
            boolean apply(T1 arg) {
                return (inner.apply(arg) || outer.apply(arg));
            }
        };
    }

    public <T1 extends T> Predicate<T1> and (final Predicate<? super T1> outer) {
        final Predicate<T> inner = this;

        return  new Predicate<T1>() {
            @Override
            boolean apply(T1 arg) {
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
