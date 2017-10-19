package cc.koosha.konfiguration.guice;

import cc.koosha.konfiguration.Konfig;
import lombok.NonNull;

import java.lang.annotation.Annotation;

import static java.lang.String.format;


/**
 * Thread-safe, immutable.
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public final class KonfigImpl implements Konfig {

    private static final long serialVersionUID = 0;

    private final String value;

    private final Class<? extends Annotation> annotationType = Konfig.class;

    public KonfigImpl(@NonNull final String value) {

        this.value = value;
    }

    public static Konfig annon(final String value) {

        return new KonfigImpl(value);
    }


    public String value() {

        return this.value;
    }

    public Class<? extends Annotation> annotationType() {

        return this.annotationType;
    }


    @Override
    public int hashCode() {

        // This is specified in java.lang.Annotation.
        int h = 0;
        h += (127 * "value".hashCode()) ^ value.hashCode();

        return h;
    }

    public boolean equals(final Object o) {

        if (o == this)
            return true;

        if (!(o instanceof Konfig))
            return false;

        final Konfig other = (Konfig) o;

        return this.value.equals(other.value());
    }

    @Override
    public String toString() {

        return format("@%s(value=%s)",
                      Konfig.class.getName(),
                      this.value
        );
    }

}
