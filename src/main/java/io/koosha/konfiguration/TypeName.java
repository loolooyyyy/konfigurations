package io.koosha.konfiguration;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * To create consistent error messages, name of types are taken from here.
 */
enum TypeName {

    BOOL(Boolean.class),
    CHAR(Character.class),

    BYTE(Byte.class),
    SHORT(Short.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),

    STRING(String.class),

    LIST(List.class, null),
    MAP(Map.class, null),
    SET(Set.class, null),

    CUSTOM(null, null),
    ;

    private final Class<?> primitiveClass;
    private final Class<?> containerClass;


    TypeName(final Class<?> primitiveClass) {
        this.primitiveClass = primitiveClass;
        this.containerClass = null;
    }

    TypeName(final Class<?> containerClass, @SuppressWarnings("unused") final Void ignore) {
        this.primitiveClass = null;
        this.containerClass = containerClass;
    }


    public boolean isContainer() {
        return this.containerClass != null;
    }

    public boolean isPrimitive() {
        return this.primitiveClass != null;
    }


    @SuppressWarnings("unused")
    public Class<?> primitiveClass() {
        if (!this.isPrimitive())
            throw new IllegalStateException("is not a primitive");
        return primitiveClass;
    }

    @SuppressWarnings("unused")
    public Class<?> containerClass() {
        if (!this.isContainer())
            throw new IllegalStateException("is not a container");
        return containerClass;
    }


    public boolean isMap() {
        return this == MAP;
    }

    public boolean isSet() {
        return this == SET;
    }

    public boolean isList() {
        return this == LIST;
    }


    public boolean isBool() {
        return this == BOOL;
    }

    public boolean isChar() {
        return this == CHAR;
    }

    public boolean isString() {
        return this == STRING;
    }


    public boolean isByte() {
        return this == BYTE;
    }

    public boolean isShort() {
        return this == SHORT;
    }

    public boolean isInt() {
        return this == INT;
    }

    public boolean isLong() {
        return this == LONG;
    }

    public boolean isFloat() {
        return this == FLOAT;
    }

    public boolean isDouble() {
        return this == DOUBLE;
    }


    @SuppressWarnings("unused")
    public boolean isCustom() {
        return this == CUSTOM;
    }

}
