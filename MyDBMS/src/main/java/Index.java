import java.io.*;
import java.util.*;

public class Index implements Serializable {
    private static final long serialVersionUID = 1234L;

    private final String path;
    Map<String, Set<Integer>> lookup;

    public Index(String path) {
        this.path = path;
        lookup = new HashMap<>();
    }

    private void insert(String keyword, int index) {
        Set<Integer> s = lookup.get(keyword);
        if (s == null) {
            s = new HashSet<>();
            lookup.put(keyword, s);
        }
        s.add(index);
    }

    public void insert(Set<String> keywords, int index) {
        for(String s : keywords) {
            insert(s,index);
        }
    }

    public Set<Integer> get(String keyword) {
        return lookup.get(keyword);
    }

    public void createIndex(String path) {
        try (FileReader fr = new FileReader(path);
             BufferedReader br = new BufferedReader(fr)) {
            String s;
            int lineNumber;
            while((s = br.readLine()) != null) {
                Set<String> words = StringUtils.parsedWords(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

