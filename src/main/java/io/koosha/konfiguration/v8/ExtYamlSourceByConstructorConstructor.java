package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.error.KfgAssertionException;
import io.koosha.konfiguration.error.KfgTypeException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

final class ExtYamlSourceByConstructorConstructor<A extends Annotation> extends org.yaml.snakeyaml.constructor.Constructor {

    @NotNull
    private final Class<? extends A> marker;

    @NotNull
    private final Function<? super A, String[]> markerExtractor;

    ExtYamlSourceByConstructorConstructor(@NonNull @NotNull final Class<? extends A> marker,
                                          @NotNull @NonNull final Function<? super A, String[]> markerExtractor) {
        this.marker = marker;
        this.markerExtractor = markerExtractor;
        this.yamlClassConstructors.put(NodeId.mapping, new KonstructMapping());
    }

    private static <A extends Annotation> Constructor<?> findByAnnotation(
            final Class<? extends A> marker,
            final Function<? super A, String[]> markerExtractor,
            final Class<?> origin,
            final LinkedHashMap<String, ? extends Param> cArgsByName,
            final List<String> cArgNames,
            final Object[] values) {
        final List<Constructor<?>> constructors = Arrays
                .stream(origin.getDeclaredConstructors())
                .filter(it -> it.getAnnotation(marker) != null)
                .filter(it -> {
                    final List<String> ex = asList(markerExtractor.apply(it.getAnnotation(marker)));
                    return ex.containsAll(cArgNames) && cArgNames.containsAll(ex);
                })
                .filter(it -> {
                    final Parameter[] ps = it.getParameters();
                    final String[] ns = markerExtractor.apply(it.getAnnotation(marker));
                    for (int i = 0; i < ns.length; i++)
                        if (!upper(ps[i].getType()).isAssignableFrom(upper(cArgsByName.get(ns[i]).type)))
                            return false;
                    return true;
                })
                .collect(toList());

        if (constructors.size() != 1)
            return null;

        final Constructor<?> c = constructors.get(0);
        final String[] names = markerExtractor.apply(c.getAnnotation(marker));
        final Map<Integer, Integer> nameMapping = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            nameMapping.put(i, cArgNames.indexOf(name));
        }
        final Object[] clone = values.clone();
        nameMapping.forEach((cIndex, vIndex) -> values[cIndex] = clone[vIndex]);

        return c;
    }

    private static Constructor<?> findByParamType(
            final Class<?> origin,
            final List<Class<?>> cArgTypes,
            final Object[] values) {

        //                    .filter(it -> {
        //                        // Make sure there's no overlapping type.
        //                        final List<Class<?>> pTypes = asList(it.getParameterTypes());
        //                        for (final Class<?> pType : pTypes)
        //                            if (pTypes.stream().filter(pType::isAssignableFrom).count() != 1)
        //                                return false;
        //                        return true;
        //                    })

        final List<Constructor<?>> constructors = Arrays
                .stream(origin.getDeclaredConstructors())
                .filter(it -> it.getParameterCount() == cArgTypes.size())
                // TODO make smarter, remove types that only have one candidate.
                .filter(it -> {
                    // Making sure there's a one to one mapping.
                    for (final Class<?> pType : it.getParameterTypes())
                        if (cArgTypes.stream().filter(upper(pType)::isAssignableFrom).count() != 1)
                            return false;
                    return true;
                })
                .collect(toList());

        if (constructors.size() != 1)
            return null;

        final Constructor<?> c = constructors.get(0);
        final Class<?>[] pt = c.getParameterTypes();
        final Object[] clone = values.clone();
        assert pt.length == clone.length;

        for0:
        for (int i = 0; i < pt.length; i++) {
            for (final Object oo : clone)
                if (oo == null || upper(pt[i]).isAssignableFrom(upper(oo.getClass()))) {
                    values[i] = oo;
                    continue for0;
                }
            throw new KfgAssertionException("constructor and value mismatch");
        }

        return c;
    }


    private static class Param {
        String name;
        Class<?> type;
        Object value;

        Param(final String name) {
            this.name = name;
        }


        final Double double_() {
            return (Double) this.value;
        }

        final byte[] byteArray() {
            return (byte[]) this.value;
        }

        final boolean typeIs(Object... other) {
            for (final Object o : other)
                if (o == this.type)
                    return true;
            return false;
        }

    }

    private static final class ParamNode extends Param {
        Node node;

        public ParamNode(String name, Node node) {
            super(name);
            this.node = node;
        }

        Class<?>[] getActualTypeArguments() {
            return null;
        }
    }

    private class KonstructMapping extends ConstructMapping {
        @Override
        public Object construct(final Node node) {
            if (Map.class.isAssignableFrom(node.getType()) ||
                    Collection.class.isAssignableFrom(node.getType()) ||
                    typeDefinitions.containsKey(node.getType()))
                return super.construct(node);

            if (node.isTwoStepsConstruction())
                throw new YAMLException("encountered two step node: " + node);

            final MappingNode mnode = (MappingNode) node;
            flattenMapping(mnode);

            final List<ParamNode> consArgs = mnode
                    .getValue()
                    .stream()
                    .map(tuple -> {
                        if (!(tuple.getKeyNode() instanceof ScalarNode))
                            throw new YAMLException(
                                    "Keys must be scalars but found: " + tuple.getKeyNode());
                        final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                        keyNode.setType(String.class);
                        return new ParamNode((String) constructObject(keyNode), tuple.getValueNode());
                    })
                    .peek(t -> {
                        final Tag tag = t.node.getTag();
                        Class<?> tp = null;
                        if (tag == Tag.INT)
                            tp = Integer.class;
                        else if (tag == Tag.FLOAT)
                            tp = Float.class;
                        else if (tag == Tag.STR)
                            tp = String.class;
                        else if (tag == Tag.MAP)
                            tp = Map.class;
                        else if (tag == Tag.SEQ)
                            tp = List.class;
                        else if (tag == Tag.SET)
                            tp = Set.class;
                        else if (tag == Tag.BOOL)
                            tp = Boolean.class;
                        else if (tag == Tag.NULL)
                            tp = Object.class;
                        t.type = tp;
                        if (tp != null)
                            t.node.setType(tp);
                    })
                    .peek(t -> {
                        if (t.node.getNodeId() != NodeId.scalar) {
                            // only if there is no explicit TypeDescription
                            final Class<?>[] args = t.getActualTypeArguments();
                            if (args != null && args.length > 0) {
                                // type safe (generic) collection may contain the proper class
                                if (t.node.getNodeId() == NodeId.sequence) {
                                    ((SequenceNode) t.node).setListType(args[0]);
                                }
                                else if (Set.class.isAssignableFrom(t.node.getType())) {
                                    ((MappingNode) t.node).setOnlyKeyType(args[0]);
                                    t.node.setUseClassConstructor(true);
                                }
                                else if (Map.class.isAssignableFrom(t.node.getType())) {
                                    ((MappingNode) t.node).setTypes(args[0], args[1]);
                                    t.node.setUseClassConstructor(true);
                                }
                            }
                        }
                    })
                    .peek(t -> t.value = constructObject(t.node))
                    .peek(t -> {
                        if (t.value instanceof Double && t.typeIs(Float.TYPE, Float.class))
                            t.value = t.double_().floatValue();

                        else if (t.value instanceof byte[] &&
                                Objects.equals(t.node.getTag(), Tag.BINARY) &&
                                t.typeIs(String.class))
                            t.value = new String(t.byteArray());
                    })
                    .collect(toList());

            final LinkedHashMap<String, ParamNode> byName = new LinkedHashMap<>();
            consArgs.stream()
                    .peek(it -> it.type = upper(it.type))
                    .forEach(it -> byName.put(it.name, it));

            final List<String> names = consArgs
                    .stream()
                    .map(t -> t.name)
                    .collect(toList());

            final Class<?>[] types = consArgs
                    .stream()
                    .map(t -> t.type)
                    .toArray(Class<?>[]::new);

            final Object[] values = consArgs
                    .stream()
                    .map(t -> t.value)
                    .toArray();

            Constructor<?> c0;
            try {
                c0 = findByAnnotation(
                        marker,
                        markerExtractor,
                        node.getType(),
                        byName,
                        names,
                        values);
            }
            catch (YAMLException y) {
                c0 = null;
            }

            if (c0 == null)
                try {
                    c0 = findByParamType(
                            node.getType(),
                            byName.values().stream().map(it -> it.type).collect(toList()),
                            values);
                }
                catch (YAMLException y) {
                    //noinspection ConstantConditions
                    c0 = null;
                }

            if (c0 == null)
                try {
                    c0 = node.getType().getDeclaredConstructor(types);
                }
                catch (NoSuchMethodException e) {
                    // ignore
                }


            if (c0 == null)
                try {
                    final Class<?>[] types2 = consArgs
                            .stream()
                            .map(t -> t.type)
                            .map(ExtYamlSourceByConstructorConstructor::upper)
                            .toArray(Class<?>[]::new);
                    c0 = node.getType().getDeclaredConstructor(types2);
                }
                catch (NoSuchMethodException ex) {
                    c0 = null;
                }

            if (c0 == null)
                throw new KfgTypeException(null, null, null, "no liable constructor found");

            try {
                c0.setAccessible(true);
                return c0.newInstance(values);
            }
            catch (Exception e) {
                throw new YAMLException(e);
            }
        }
    }

    private static Class<?> upper(Class<?> c) {
        if (c == boolean.class)
            return Boolean.class;
        if (c == byte.class)
            return Byte.class;
        if (c == short.class)
            return Short.class;
        if (c == int.class)
            return Integer.class;
        if (c == long.class)
            return Long.class;
        if (c == float.class)
            return Float.class;
        if (c == double.class)
            return Double.class;
        return c;
    }

}
