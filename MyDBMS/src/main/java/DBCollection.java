import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

public class DBCollection implements Serializable {
    final String filePath;
    final String collectionName;
    final Index index;
    int size;

    public DBCollection(String filePath, String collectionName) {
        this.collectionName = collectionName;
        this.filePath = filePath + collectionName + '/';
        index = new Index(filePath);
    }

    public void insert(String s) {
        String text = null;
        Wikipedia w = new Gson().fromJson(s,Wikipedia.class);
        text = w.getText();

        try {
            String dataPath = filePath + "data";
            File file = new File(dataPath);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                Path path = Paths.get(dataPath);
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(dataPath, true));
            bw.write(s);
            bw.newLine();
            bw.flush();
            bw.close();
            index.insert(StringUtils.parsedWords(text), size);
            size++;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getDocuments(String keyword) {
        Set<Integer> lineNumbers = index.get(keyword);
        ArrayList<String> documents = new ArrayList<>();

        try {
            String dataPath = filePath + "data";
            BufferedReader bf = new BufferedReader(new FileReader(dataPath));
            String line;
            int n = 0;
            while((line = bf.readLine()) != null) {
                if(lineNumbers.contains(n)) {
                    documents.add(line);
                }
                n++;
            }
            bf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }
}
