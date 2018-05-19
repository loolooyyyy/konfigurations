package cc.koosha.konfiguration;


/**
 * Similar to java 8's Supplier interface.
 *
 * <p>Represents a supplier of results.
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * @param <T> type of object this supplier supplies.
 */
public interface SupplierX<T> {

    T get();

}
