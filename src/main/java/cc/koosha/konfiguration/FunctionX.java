package cc.koosha.konfiguration;


/**
 * Similar to java 8's Function interface.
 *
 * <p>Represents a function that accepts one argument and produces a result.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface FunctionX<T, R> {

    R apply(T t);

}
