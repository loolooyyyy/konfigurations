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

    BOOLEAN(Boolean.class),
    INT(Integer.class),
    LONG(Long.class),
    STRING(String.class),

    LIST(List.class),
    MAP(Map.class),

    CUSTOM(null),

    ;

    private final Class<?> representingClass;

    public KonfigDataType valueOf(final Class<?> klass) {

        if(Objects.equals(klass, Boolean.class))
            return BOOLEAN;
        else if(Objects.equals(klass, Integer.class))
            return INT;
        else if(Objects.equals(klass, Long.class))
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
