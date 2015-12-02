package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;


public class Injector {
    private static Map<String, Object> createdObjects = new HashMap<>();
    private static Set<String> isStartedCreatingObject = new HashSet<>();

    private static Object makeObject(String mainCurrentClassName, List<String> implementationClassNames) throws Exception {
        List<Object> parameters = new ArrayList<>();
        isStartedCreatingObject.add(mainCurrentClassName);

        Constructor<?> myClassConstructor = Class.forName(mainCurrentClassName).getConstructors()[0];
        Class<?>[] myParameters = myClassConstructor.getParameterTypes();
        for (Class<?> myParameter : myParameters) {
            Object myObject2 = findRelation(myParameter, implementationClassNames);
            parameters.add(myObject2);
        }

        Object myClassObject = myClassConstructor.newInstance(parameters.toArray());

        createdObjects.put(mainCurrentClassName, myClassObject);
        return myClassObject;
    }

    public static Object findRelation(Class<?> mainCurrentClass, List<String> implementationClassNames) throws Exception {
        Class<?> neededClass = null;
        for (String name : implementationClassNames) {
            Class<?> currentClass = Class.forName(name);
            if (!mainCurrentClass.isAssignableFrom(currentClass)) {
                continue;
            }

            if (neededClass != null) {
                throw new AmbiguousImplementationException();
            }

            neededClass = currentClass;
        }
        if (neededClass == null) {
            throw new ImplementationNotFoundException();
        }

        String neededClassName = neededClass.getName();
        if (!createdObjects.containsKey(neededClassName)) {
            if (isStartedCreatingObject.contains(neededClassName)) {
                throw new InjectionCycleException();
            }
            makeObject(neededClassName, implementationClassNames);
        }
        return createdObjects.get(neededClassName);

    }

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        List<String> allClasses = new ArrayList<>(implementationClassNames);
        allClasses.add(rootClassName);
        return makeObject(rootClassName, allClasses);
    }
}