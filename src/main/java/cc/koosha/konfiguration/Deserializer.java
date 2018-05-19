package cc.koosha.konfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface Deserializer<F> {

    <T> T custom(F from, Class<T> type) throws IOException;

    <T> List<T> list(F from, Class<T> type) throws IOException;

    <T> Map<String, T> map(F from, Class<T> type) throws IOException;

    <T> Set<T> set(F from, Class<T> type) throws IOException;

}
