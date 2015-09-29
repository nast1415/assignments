package ru.spbau.mit;

import java.util.*;

public class Collections {
    public static <T, R> Iterable<R> map(final Function1<? super T, R> outer, final Iterable<T> collection) {
        List<R> resultCollection = new ArrayList<>();
        for (T element : collection) {
            resultCollection.add(outer.apply(element));
        }
        return resultCollection;
    }

    public static <T> Iterable<T> filter(final Predicate<? super T> predicate, final Iterable<T> collection) {
        List<T> resultCollection = new ArrayList<>();
        for (T element : collection) {
            if (predicate.apply(element)) {
                resultCollection.add(element);
            }
        }
        return resultCollection;
    }

    public static <T> Iterable<T> takeWhile(final Predicate<? super T> predicate, final Iterable<T> collection) {
        List<T> resultCollection = new ArrayList<>();
        for (T element : collection) {
            while (!predicate.apply(element)) {
                return resultCollection;
            }
            resultCollection.add(element);
        }
        return resultCollection;
    }

    public static <T> Iterable<T> takeUnless(final Predicate<? super T> predicate, final Iterable<T> collection) {
        return takeWhile(predicate.not(), collection);
    }

    public static <T, R> R foldl(final Function2<? super R, ? super T, R> outer, R value, final Iterable<T> collection) {
        for (T element : collection) {
            value = outer.apply(value, element);
        }
        return value;
    }


    public static <T, R> R foldr(final Function2<? super T, ? super R, R> outer, R value, final Iterator<T> it) {
        if (!it.hasNext())
            return value;
        T element = it.next();
        return outer.apply(element, foldr(outer, value, it));
    }

    public static <T, R> R foldr(final Function2<? super T, ? super R, R> outer, R value, final Iterable<T> collection) {
        Iterator<T> it = collection.iterator();
        return foldr(outer, value, it);
    }

}

