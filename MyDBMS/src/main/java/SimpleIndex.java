import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleIndex<K> implements Index<K> {

    private final String path;
    private Map<K, Set<Integer>> lookup;

    public SimpleIndex(String path) {
        this.path = path;
        lookup = new HashMap<>();
    }

    @Override
    public void insert(K key, Set<Integer> value) {
        System.out.print(key + " " + value.size() + " ");
        for (int i : value) {
            System.out.println(i);
        }
        lookup.put(key, value);
    }

    @Override
    public Set<Integer> get(K key) {
        System.out.println(key);
        return lookup.get(key);
    }

}
