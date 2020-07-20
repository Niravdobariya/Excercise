import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class DBCollection implements Serializable {
    final String filePath;
    final String collectionName;
    final String dataPath;
    Map<String, Index<String>> indexes;
    Map<Integer, Long> position;
    int size;

    public DBCollection(String filePath, String collectionName) {
        this.collectionName = collectionName;
        this.filePath = filePath + collectionName + '/';
        this.dataPath = this.filePath + "data";
        position = new HashMap<>();
        indexes = new HashMap<>();
        SimpleIndex<String> si = new SimpleIndex<>(this.filePath);
        TextIndex ti = new TextIndex(this.filePath);
        indexes.put("id", si);
        indexes.put("title", ti);
    }

    public void insert(String s) {
        writeToFile(s);
        Map<String, String> wikiJson = (Map<String, String>) GsonParser.parse(s, Map.class);
        for (Map.Entry entry : wikiJson.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        for (Map.Entry<String, Index<String>> entry : indexes.entrySet()) {
            Index<String> index = entry.getValue();
//            System.out.println(entry.getKey() +  " " + wikiJson.get(entry.getKey()));
            index.insert(wikiJson.get(entry.getKey()), Collections.singleton(size));
        }
        size++;
    }

    public ArrayList<String> getDocumentsByIndex(Object keyword, String indexName) {
        //        System.out.println(keyword + " " + indexName);
        Index index = indexes.get(indexName);
        Set<Integer> lineNumbers = index.get(keyword);
        if (lineNumbers != null) {
            for (int i : lineNumbers) {
                System.out.println(i);
            }
        }
        return readFromFile(lineNumbers);
    }

    //-----------------------------private methods-----------------------------//

    private void writeToFile(String data) {
        try {
            File file = new File(dataPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                Path path = Paths.get(dataPath);
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(dataPath, true));
            position.put(size, file.length());
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> readFromFile(Set<Integer> lineNumbers) {
        if (lineNumbers == null) return null;
        ArrayList<String> documents = new ArrayList<>();
        try {
            RandomAccessFile rf = new RandomAccessFile(dataPath, "r");

            for (int lineNumber : lineNumbers) {
                rf.seek(position.get(lineNumber));
                FileChannel fc = rf.getChannel();
                String s = rf.readLine();
                documents.add(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }


}
