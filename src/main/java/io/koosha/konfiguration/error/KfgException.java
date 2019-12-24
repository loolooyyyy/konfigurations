package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.String.format;

@Accessors(fluent = true)
@Getter
@ThreadSafe
public class KfgException extends RuntimeException {

    @Nullable
    private final String source;

    @Nullable
    private final Q<?> neededType;

    @Nullable
    private final String actualValue;

    @Nullable
    private final String say;

    public KfgException(@Nullable final String source,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue,
                        @Nullable final String message,
                        @Nullable final Throwable cause) {
        super(message, cause);
        this.source = source;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
        this.say = message;
    }

    public KfgException(@Nullable final String source,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue,
                        @Nullable String message) {
        super(message);
        this.source = source;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
        this.say = message;
    }

    public KfgException(@Nullable final String source,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue) {
        this.source = source;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
        this.say = null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return format("%s[s=%s/%s, want=%s, have=%s]",
                this.getClass().getName(), // %s[
                this.source(), // s=%s
                this.say == null ? "" : this.say, // /%s
                this.neededType(), // want=%s
                this.actualValue()); // have=%s
    }


    public final boolean hasSource() {
        return this.source() != null;
    }

    public final boolean hasNeededType() {
        return this.neededType() != null;
    }

    public final boolean hasActualValue() {
        return this.actualValue() != null;
    }


    static String msgOf(final Throwable t) {
        return t == null
               ? "[null exception]->[null exception]"
               : format("[throwable::%s]->[%s]", t.getClass().getName(), t.getMessage());
    }

    static String toStringOf(final Object value) {
        if (value instanceof Q)
            return value.toString();
        String representationC;
        try {
            representationC = value == null ? "null" : value.getClass().getName();
        }
        catch (Throwable t) {
            representationC = "[" + "value.getClass().getName()" + "]=>" + msgOf(t);
        }

        String representationV;
        try {
            representationV = Objects.toString(value);
        }
        catch (Throwable t) {
            representationV = "[" + "Objects.toString(value)" + "]=>" + msgOf(t);
        }

        return format("[%s]->%s", representationC, representationV);
    }

}
