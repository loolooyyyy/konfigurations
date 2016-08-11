package cc.koosha.konfigurations.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum KonfigDataType {

    BOOLEAN(boolean.class),
    INT(int.class),
    LONG(long.class),
    STRING(String.class),

    LIST(List.class),
    MAP(Map.class),

    CUSTOM(null),

    ;

    private final Class<?> representingClass;

    public KonfigDataType valueOf(final Class<?> klass) {

        if(Objects.equals(klass, boolean.class))
            return BOOLEAN;
        else if(Objects.equals(klass, int.class))
            return INT;
        else if(Objects.equals(klass, long.class))
            return LONG;
        else if(Objects.equals(klass, String.class))
            return STRING;
        else if(Objects.equals(klass, List.class))
            return LIST;
        else if(Objects.equals(klass, Map.class))
            return MAP;
        else
            return CUSTOM;
    }

}
