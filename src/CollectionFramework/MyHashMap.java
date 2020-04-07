import java.util.*;
import java.util.function.Consumer;

public class MyHashMap<K, V> implements Map<K, V> {

    final static int INITIAL_CAPACITY = 10;
    final static float DEFAULT_LOAD_FACTOR = 0.75F;
    final static int MAXIMUM_CAPACITY = 1 << 30;

    private class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }
    }

    private int capacity;
    private float loadFactor;
    private Node<K, V>[] bucket;
    private int size = 0;
    int modCount = 0;

    MyHashMap() {
        capacity = INITIAL_CAPACITY;
        loadFactor = DEFAULT_LOAD_FACTOR;
        bucket = (Node<K, V>[]) new Node[capacity];
    }

    static final int hash(Object key) {
        return (key == null) ? 0 : key.hashCode();
    }

    final V putNode(Node<K, V> node) {
        int id = node.hash % capacity;
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
        size++;
        modCount++;
        if (size > capacity * loadFactor) {
            reHash();
        }
        return node.value;
    }

    final Node<K, V> getNode(Object key) {
        if (bucket == null) {
            return null;
        }
        int id = hash(key) % capacity;
        Node<K, V> cur = bucket[id];
        while (cur != null) {
            if (key.equals(cur.key)) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    final Node<K, V> removeNode(Object key) {
        int id = hash(key) % capacity;
        if (bucket == null) return null;
        Node<K, V> cur = bucket[id];
        if (cur.key.equals(key)) {
            modCount++;
            bucket[id] = bucket[id].next;
            return cur;
        }
        while (cur.next != null) {
            if (cur.next.key.equals(key)) {
                modCount++;
                V ret = cur.next.value;
                cur.next = cur.next.next;
                size--;
                return cur;
            }
        }
        return null;
    }

    private void reHash() {
        int oldCapacity = capacity;
        capacity *= 2;
        if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        }
        Node<K, V>[] oldBucket = bucket;
        bucket = (Node<K, V>[]) new Node[capacity];
        Node<K, V> cur;

        for (int id = 0; id < oldCapacity; id++) {
            cur = oldBucket[id];
            while (cur != null) {
                putNode(new Node(cur.hash, cur.key, cur.value, null));
                cur = cur.next;
            }
        }

    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return (size == 0 ? true : false);
    }

    @Override
    public boolean containsKey(Object key) {
        return (getNode(key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        Node<K, V>[] b = bucket;
        for (int i = 0; i < capacity; i++) {
            Node<K, V> cur = b[i];
            while (cur != null) {
                if (cur.value.equals(value)) {
                    return true;
                }
                cur = cur.next;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Node<K, V> v;
        if ((v = getNode(key)) == null) {
            return null;
        }
        return v.value;
    }

    @Override
    public V put(K key, V value) {
        return putNode(new Node<K, V>(hash(key), key, value, null));
    }

    @Override
    public V remove(Object key) {
        Node<K, V> node = removeNode(key);
        if (node == null) {
            return null;
        }
        return node.value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Set<? extends Entry<? extends K, ? extends V>> entries = m.entrySet();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        size = 0;
        capacity = INITIAL_CAPACITY;
        bucket = (Node<K, V>[]) new Node[capacity];
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() { return new ValueCollection(); }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof Map) {
            Map<K, V> map = (Map<K, V>) o;
            Set<Map.Entry<K, V>> entries = map.entrySet();
            for(Map.Entry<K, V> entry : entries) {
                if(get(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // TODO
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    //-------------------------------Collections------------------------------//

    abstract class HashCollection {

        public int size() { return size; }

        public abstract Iterator iterator();

        public boolean isEmpty() { return size == 0; }

        public Object[] toArray() { Object[] ret = new Object[size];
            Iterator itr = iterator();
            for (int i = 0; i < size(); i++) {
                if (itr.hasNext()) {
                    ret[i] = itr.next();
                }
            }
            return ret;

        }

        //TODO
        public <T> T[] toArray(T[] a) { return null; }

        public abstract boolean contains(Object o);

        public boolean containsAll(Collection<?> c) {
            for(Object o : c) {
                if(!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        //TODO
        public boolean retainAll(Collection<?> c) {
            Iterator itr = iterator();
            while(itr.hasNext()) {
                if(!c.contains(itr.next())) {
                    itr.remove();
                }
            }
            return true;
        }

        public abstract boolean remove(Object o);

        public boolean removeAll(Collection<?> c) {
            for(Object o : c) {
                if(remove(o)) {
                    return false;
                }
            }
            return true;
        }

        public void clear() { MyHashMap.this.clear(); }

    }

    final class KeySet extends HashCollection implements Set<K> {

        public Iterator<K> iterator() { return new KeySetIterator(); }
        public boolean contains(Object o) { return containsKey(o); }
        public boolean add(K k) { return false; }
        public boolean addAll(Collection<? extends K> c) { return false; }

        public boolean remove(Object o) {
            if (o instanceof Map.Entry)
                return MyHashMap.this.remove(o) != null;
            return false;
        }

        public void forEach(Consumer<? super K> action) {
            Iterator<K>itr = iterator();
            while(itr.hasNext()) {
                K k = itr.next();
                action.accept(k);
            }
        }

    }

    final class ValueCollection extends HashCollection implements Collection<V> {

        public Iterator<V> iterator() { return new ValuesIterator(); }
        public boolean contains(Object o) { return containsValue(o); }
        public boolean add(V v) { return false; }
        public boolean addAll(Collection<? extends V> c) { return false; }
        public boolean remove(Object o) { return false; }

        public void forEach(Consumer<? super V> action) {
            for(V value : this) {
                action.accept(value);
            }
        }


    }

    final class EntrySet extends HashCollection implements Set<Map.Entry<K,V>> {

        public Iterator<Entry<K, V>> iterator() { return new EntrySetIterator(); }
        public boolean add(Entry<K, V> kvEntry) { return false; }
        public boolean addAll(Collection<? extends Entry<K, V>> c) { return false; }
        public boolean contains(Object o) {
            if(o instanceof Map.Entry) {
                Map.Entry<K, V> entry = (Entry<K, V>) o;
                if(get(entry.getKey()) == entry.getValue()) {
                    return true;
                }
            }
            return false;
        }

        public boolean remove(Object o) {
            if (o instanceof Map.Entry)
                return removeNode(o) != null;
            return false;
        }

        public void forEach(Consumer<? super Entry<K, V>> action) {
            for(Map.Entry<K,V> entry : this) {
                action.accept(entry);
            }
        }
    }

    //-------------------------------Iterators-------------------------------//

    abstract class HashIterator {
        Node<K, V> current;
        Node<K, V> next;
        int expectedModCount;
        int index;

        HashIterator() {
            expectedModCount = modCount;
            index = -1;
            next = null;
            current = null;
            while(++index < capacity) {
                if(bucket[index] != null) {
                    next = bucket[index];
                    break;
                }
            }
        }

        void findNext() {
            checkModificationCondition();
            if (next == null) {
                throw new IllegalStateException();
            }
            current = next;
            if (next.next != null) {
                next = next.next;
            } else {
                while (++index < capacity && bucket[index] == null) {
                }
                if (index == capacity) {
                    next = null;
                } else {
                    next = bucket[index];
                }
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public void remove() {
            checkModificationCondition();
            removeNode(current.getKey());
            expectedModCount = modCount;
        }

        private void checkModificationCondition() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    class EntrySetIterator extends HashIterator implements Iterator<Map.Entry<K, V>> {
        @Override
        public Map.Entry<K, V> next() {
            findNext();
            return current;
        }
    }

    class KeySetIterator extends HashIterator implements Iterator<K> {
        @Override
        public K next() {
            findNext();
            return current.getKey();
        }
    }

    class ValuesIterator extends HashIterator implements Iterator<V> {
        @Override
        public V next() {
            findNext();
            return current.getValue();
        }
    }

}
