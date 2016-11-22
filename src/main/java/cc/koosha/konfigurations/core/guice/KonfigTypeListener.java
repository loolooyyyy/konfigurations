package cc.koosha.konfigurations.core.guice;

import cc.koosha.konfigurations.core.*;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import lombok.NonNull;
import lombok.val;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


// The code is ugly as fuck, I know.
public final class KonfigTypeListener implements TypeListener {

    private final Konfiguration cfg;

    public KonfigTypeListener(final Konfiguration cfg) {

        this.cfg = cfg;
    }

    public <T> void hear(@NonNull final TypeLiteral<T> literal,
                         @NonNull final TypeEncounter<T> encounter) {

        Class<?> clazz = literal.getRawType();

        while (clazz != null && clazz != Object.class) {
            for (val field : clazz.getDeclaredFields())
                if (field.getType().isAssignableFrom(KonfigV.class) && field.isAnnotationPresent(Konfig.class))
                    this.processField(field, encounter, field.getAnnotation(Konfig.class));

            clazz = clazz.getSuperclass();
        }
    }

    private <T> void processField(final Field field,
                                  final TypeEncounter<T> encounter,
                                  final Konfig konfig) {

        val type = field.getGenericType();

        // Get the T from V<T> declared in the injected instance.
        if (!(type instanceof ParameterizedType))
            throw new KonfigurationException("Generic type declared for config value is unknown");

        val actual = ((ParameterizedType) type).getActualTypeArguments();
        if(actual.length != 1)
            throw new KonfigurationException("invalid number of generic types declared, needed 1, got: " +  actual.length);

        val neededType = actual[0];

        // Inject
        encounter.register(new MembersInjector<T>() {
            @Override
            public void injectMembers(final T instance) {
                final Object value;

                if (neededType instanceof Class)
                    value = KonfigTypeListener.this.injectByBaseType(neededType, konfig);
                else if (neededType instanceof ParameterizedType)
                    value = KonfigTypeListener.this.injectByComplexType(neededType, konfig);
                else
                    throw new KonfigurationException("unknown needed type: " + neededType);

                try {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
                catch (final IllegalAccessException e) {
                    throw new KonfigurationException(e);
                }
            }
        });
    }

    // String, Integer, ....
    private Object injectByBaseType(final Type neededType,
                                    final Konfig konfig)  {

        return this.cfg.custom(konfig.value(), (Class<?>) neededType);
    }

    // Map<String, String>, List<String>...
    private Object injectByComplexType(final Type neededType,
                                       final Konfig konfig) {

        val kast = (ParameterizedType) neededType;
        val rawType = kast.getRawType();
        val actual = kast.getActualTypeArguments();

        final Object value;

        if (List.class.equals(rawType)) {
            if (!actual[0].equals(String.class))
                throw new KonfigurationException("only string list supported");

            final Class<?> tp;
            try {
                tp = Class.forName(actual[0].toString());
            }
            catch (final ClassNotFoundException e) {
                throw new KonfigurationException(e);
            }

            value = cfg.list(konfig.value(), tp);
        }
        else if (Map.class.equals(rawType)) {
            if (!actual[0].equals(String.class))
                throw new KonfigurationException("only <String, ?> maps supported");

            final Class<?> tp;
            try {
                tp = Class.forName(actual[1].toString());
            }
            catch (final ClassNotFoundException e) {
                throw new KonfigurationException(e);
            }

            value = cfg.map(konfig.value(), tp);
        }
        else {
            throw new KonfigurationBadTypeException("unknown type: " + rawType);
        }

        return value;
    }

}
