package cc.koosha.konfigurations.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.Annotation;

import static java.lang.String.format;


@SuppressWarnings("ClassExplicitlyAnnotation")
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class KonfigImpl implements Konfig {

    private static final long serialVersionUID = 0;

    @NonNull
    @Getter
    private final String value;

    @Getter
    private final Class<? extends Annotation> annotationType = Konfig.class;


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
