import java.io.*;
import java.util.*;

public interface Index<K> extends Serializable {

    Set<Integer> get(K key);

    void insert(K key, Set<Integer> value);

    default void insertAll(Collection<K> keys, Set<Integer> value) {
        for (K key : keys) {
            insert(key, value);
        }
    }
}

