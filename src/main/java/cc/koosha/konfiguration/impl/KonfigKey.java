package cc.koosha.konfiguration.impl;

import lombok.Value;
import lombok.experimental.Accessors;


/**
 * Thread-safe and immutable.
 */
@Value
@Accessors(fluent = true)
final class KonfigKey {

    private final String name;
    private final Class<?> dt;
    private final Class<?> el;

    @SuppressWarnings("SimplifiableIfStatement")
    boolean isSame(final KonfigKey that) {

        if(that == null)
            return false;

        if(that.dt != this.dt)
            return false;

        if(this.el == that.el)
            return true;

        return that.el != null && that.el.isAssignableFrom(this.el);
    }

}
