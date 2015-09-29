package ru.spbau.mit;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AllTests {

    //Tests for Function1

    private static final Function1<Integer, Integer> mult5 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer arg) {
            return arg * 5;
        }
    };

    private static final Function1<Integer, Integer> doubleArg = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer arg) {
            return arg + arg;
        }
    };

    @Test
    public void composeFunction1Test() throws Exception {
        assertEquals(mult5.apply(3), (Integer) 15);
        assertEquals(mult5.compose(doubleArg).apply(3), (Integer) 30);
        assertEquals(doubleArg.compose(mult5).apply(6), (Integer) 60);
    }

    //Tests for Function2

    private static final Function2<Integer, Integer, Integer> mult = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer arg1, Integer arg2) {
            return arg1 * arg2;
        }
    };

    private static final Function2<Integer, Integer, Integer> minus = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer arg1, Integer arg2) {
            return arg1 - arg2;
        }
    };

    @Test
    public void allAboutFunction2Test() {
        assertEquals(mult.apply(2, 7), (Integer) 14);
        assertEquals(minus.bind1(7).apply(2), (Integer) 5);
        assertEquals(minus.bind2(7).apply(2), (Integer) (-5));
        assertEquals(mult.curry().apply(7).apply(2), (Integer) 14);
        assertEquals(minus.compose(mult5).apply(7, 2), (Integer) 25);
    }

    //Tests for Predicate

    private static final Predicate<Integer> lessThan239 = new Predicate<Integer>() {
        @Override
        boolean apply(Integer arg) {
            return arg < 239;
        }
    };

    private static final Predicate<Integer> greaterThan55 = new Predicate<Integer>() {
        @Override
        boolean apply(Integer arg) {
            return arg > 55;
        }
    };

    private static final Predicate<Integer> lessThan55 = new Predicate<Integer>() {
        @Override
        boolean apply(Integer arg) {
            return arg < 55;
        }
    };

    @Test
    public void allAboutPredicateTest() {
        assertTrue(lessThan239.and(greaterThan55).apply(58));
        assertFalse(lessThan239.and(greaterThan55).apply(10));
        assertFalse(lessThan239.and(greaterThan55).apply(240));

        assertTrue(lessThan239.or(greaterThan55).apply(11));
        assertTrue(lessThan239.or(greaterThan55).apply(240));
        assertFalse(lessThan55.or(greaterThan55).apply(55));

        assertFalse(Predicate.ALWAYS_FALSE.apply(null));
        assertTrue(Predicate.ALWAYS_TRUE.apply(null));

        assertFalse(lessThan239.and(greaterThan55).not().apply(60));
        assertTrue(lessThan239.and(greaterThan55).not().apply(50));
    }

    // Tests for Collections

    private static final List<Integer> collection = Arrays.asList(11, 30, 80, 400, 50, 40, 60);

    private static final Predicate<Integer> lessThan70 = new Predicate<Integer>() {
        @Override
        boolean apply(Integer arg) {
            return arg < 70;
        }
    };


    private static final Predicate<Integer> isEven = new Predicate<Integer>() {
        @Override
        boolean apply(Integer arg) {
            return arg % 2 == 0;
        }
    };

    @Test
    public void allAboutCollections() {
        List<Integer> listRes1 = (List<Integer>) Collections.map(mult5, collection);
        assertTrue(listRes1.equals(Arrays.asList(55, 150, 400, 2000, 250, 200, 300)));

        List<Integer> listRes2 = (List<Integer>) Collections.filter(isEven, collection);
        assertTrue(listRes2.equals(Arrays.asList(30, 80, 400, 50, 40, 60)));

        List<Integer> listRes3 = (List<Integer>) Collections.takeWhile(lessThan70, collection);
        assertTrue(listRes3.equals(Arrays.asList(11, 30)));

        List<Integer> listRes4 = (List<Integer>) Collections.takeUnless(lessThan70, collection);
        assertTrue(listRes4.equals(java.util.Collections.emptyList()));

        Integer result = Collections.foldl(minus, 0, collection);
        assertEquals(result, (Integer) (-671));

        result = Collections.foldr(minus, 0, collection);
        assertEquals(result, (Integer) (-269));
    }

}
