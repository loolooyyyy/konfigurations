package cc.koosha.konfiguration.guice;

import cc.koosha.konfiguration.*;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


/**
 * Thread-safe, immutable.
 */
// The code is ugly as fuck, I know.
public final class KonfigTypeListener implements TypeListener {

    private final Konfiguration cfg;

    public KonfigTypeListener(final Konfiguration cfg) {

        this.cfg = cfg;
    }

    public <T> void hear(final TypeLiteral<T> literal,
                         final TypeEncounter<T> encounter) {

        if(literal == null)
            throw new NullPointerException("literal");
        if(encounter == null)
            throw new NullPointerException("encounter");
        Class<?> clazz = literal.getRawType();

        while (clazz != null && clazz != Object.class) {
            for (final Field field : clazz.getDeclaredFields())
                if (field.getType().isAssignableFrom(K.class) && field.isAnnotationPresent(Konfig.class))
                    this.processField(field, encounter, field.getAnnotation(Konfig.class));

            clazz = clazz.getSuperclass();
        }
    }

    private <T> void processField(final Field field,
                                  final TypeEncounter<T> encounter,
                                  final Konfig konfig) {

        final Type type = field.getGenericType();

        // Get the T from V<T> declared in the injected instance.
        if (!(type instanceof ParameterizedType))
            throw new KonfigurationException("Generic type declared for config value is unknown");

        final Type[] actual = ((ParameterizedType) type).getActualTypeArguments();
        if (actual.length != 1)
            throw new KonfigurationException(
                    "invalid number of generic types declared, needed 1, got: " + actual.length);

        final Type neededType = actual[0];

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
                                    final Konfig konfig) {

        return this.cfg.custom(konfig.value(), (Class<?>) neededType);
    }

    // Map<String, String>, List<String>...
    private Object injectByComplexType(final Type neededType,
                                       final Konfig konfig) {

        final ParameterizedType kast = (ParameterizedType) neededType;
       final  Type rawType = kast.getRawType();
        final Type[] actual = kast.getActualTypeArguments();

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
            throw new KonfigurationTypeException("unknown type: " + rawType);
        }

        return value;
    }

}
