package CollectionFramework;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MyHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    final int INITIAL_CAPACITY = 1 << 4;
    final float DEFAULT_LOAD_FACTOR = 0.75f;
    final int MAXIMUM_CAPACITY = 1 << 30;
    int size;
    float loadFactor;
    int threshold;
    int capacity = 1 << 4;
    int modCount;

    private class Node<K, V> implements Map.Entry<K, V> {
        int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.key = key;
            this.value = val;
            this.hash = hash;
            this.next = next;

        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            this.value = value;
            return this.value;
        }

        @Override
        public int hashCode() {
            return (key.hashCode() ^ value.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || !(obj instanceof Map.Entry)) return false;
            Node<K, V> e = (Node<K, V>) obj;
            if ((e.getKey() == key) && (e.getValue() == value)) return true;
            return false;
        }

        public String toString() {
            return key + " = " + value;
        }


    }

    public MyHashMap(int initialCapacity, float loadFactor) {
        this.loadFactor = loadFactor;
        this.threshold = bucketOfSize(initialCapacity);
        capacity = initialCapacity;
        bucket = (Node<K, V>[]) new Node[initialCapacity];
    }

    public MyHashMap(int initialCapacity) {
        this.threshold = bucketOfSize(initialCapacity);
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        bucket = (Node<K, V>[]) new Node[initialCapacity];
        capacity = initialCapacity;
    }

    public MyHashMap() {
        bucket = (Node<K, V>[]) new Node[INITIAL_CAPACITY];
        this.threshold = bucketOfSize(INITIAL_CAPACITY);
    }

    private Node<K, V>[] bucket;
    private Set<Map.Entry<K, V>> entrySet;
    private Set<K> keys;
    private Collection<V> values;

    static final int hash(Object key) {
        return (key == null) ? 0 : key.hashCode();
    }

    final int bucketOfSize(int n) {
        int ret = 1;
        while (ret < n) {
            ret = ret << 1;
        }
        return (ret <= 0) ? 1 : (ret < MAXIMUM_CAPACITY) ? ret : MAXIMUM_CAPACITY;
    }

    final V putNode(Node<K, V> node) {
        int id = node.key.hashCode();
        id = id % capacity;
        Node<K, V> cur = bucket[id];
        if (cur == null) {
            bucket[id] = node;
            return node.value;
        }
        while (cur.next != null) {
            if (cur.key.equals(node.key)) {
                cur.value = node.value;
                return null;
            }
            cur = cur.next;
        }
        if (cur.key.equals(node.key)) {
            cur.value = node.value;
            return null;
        }

        cur.next = node;
        modCount++;
        if (++size > threshold) {
            resize();
        }
        return node.getValue();
    }

    final Node<K, V> getNode(K key) {
        int id = hash(key) % capacity;

        Node<K, V> cur = bucket[id];

        while (cur != null) {
            if (key.equals(cur.getKey())) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    final Node<K, V> removeNode(int hash, K key, V value, boolean matchValue) {
        int id = hash % capacity;
        if (bucket == null) return null;
        Node<K, V> cur = bucket[id];
        if (cur.key.equals(key)) {
            bucket[id] = bucket[id].next;
            return cur;
        }
        while (cur.next != null) {
            if (cur.next.key.equals(key)) {
                boolean flag = true;
                if (matchValue) {
                    if (!cur.next.value.equals(value)) flag = false;
                }
                if (flag) {
                    V ret = cur.next.value;
                    cur.next = cur.next.next;
                    size--;
                    return cur;
                }
                return null;
            }
        }
        return null;
    }

    final void resize() {
        int oldCapacity = capacity;
        capacity *= 2;
        if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        }

        threshold = bucketOfSize(capacity);
        Node<K, V>[] oldBucket = bucket;
        bucket = (Node<K, V>[]) new Node[capacity];
        Node<K, V> cur;

        for (int id = 0; id < oldCapacity; id++) {
            cur = oldBucket[id];
            while (cur != null) {
                putNode(new Node(hash(cur.getKey()), cur.getKey(), cur.getValue(), null));
                cur = cur.next;
            }
        }
    }

    final void putMapEntries(Map<? extends K, ? extends V> m) {
        int s = m.size();
        if (s > 0) {
            if (bucket == null) {
                float expectedCapacity = (float) s / loadFactor;
                int t = (expectedCapacity < (float) MAXIMUM_CAPACITY) ? (int) expectedCapacity : MAXIMUM_CAPACITY;
                if (t > threshold) {
                    threshold = bucketOfSize(t);
                }
            } else if (threshold < s) {
                resize();
            }
            for (Map.Entry<? extends K, ? extends V> mapEntry : m.entrySet()) {
                K key = mapEntry.getKey();
                V value = mapEntry.getValue();
                putNode(new Node<>(hash(key), key, value, null));
            }
        }

    }

    /* To view the bucket */
    final public void display() {
        for (int i = 0; i < capacity; i++) {
            Node<K, V> e = bucket[i];
            System.out.print(i + " :");
            while (e != null) {
                System.out.print(" ( " + e.toString() + " )");
                e = e.next;
            }
            System.out.println();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return (size == 0);
    }

    @Override
    public V get(Object key) {
        K k = (K) key;
        Node<K, V> v;

        if ((v = getNode(k)) == null) {
            return null;
        }
        return v.getValue();
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }


    @Override
    public V put(K key, V value) {
        return putNode(new Node<K, V>(hash(key), key, value, null));
    }


    @Override
    public V remove(Object key) {
        Node<K, V> e;
        return ((e = removeNode(hash(key), (K) key, null, false))) == null ? null : e.value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m);
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

}

