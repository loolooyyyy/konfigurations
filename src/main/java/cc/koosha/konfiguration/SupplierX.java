package cc.koosha.konfiguration;


/**
 * Similar to java 8's Supplier interface.
 *
 * @param <T> type of object this supplier supplies.
 */
public interface SupplierX<T> {

    T get();

}
