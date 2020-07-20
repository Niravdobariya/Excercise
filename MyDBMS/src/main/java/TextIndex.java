import java.util.*;

public class TextIndex implements Index<String> {

    private final String indexFilePath;
    private Map<String, Set<Integer>> lookup;

    public TextIndex(String indexFilePath) {
        this.indexFilePath = indexFilePath;
        this.lookup = new HashMap<>();
    }


    @Override
    public void insert(String text, Set<Integer> value) {
        Set<String> keywords = StringUtils.parsedWords(text);
        if (keywords == null) return;
        for (String keyword : keywords) {
            Set<Integer> s = lookup.get(keyword);
            if (s == null) {
                s = new HashSet<>();
                lookup.put(keyword, s);
            }
            s.addAll(value);
        }
    }

    @Override
    public Set<Integer> get(String key) {
        return lookup.get(key);
    }
}
