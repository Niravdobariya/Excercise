import java.io.*;
import java.util.*;

public class FileIO {

    public static ArrayList<String> fetchLines(String path, Collection<Integer> lineNumbers) {
        if (lineNumbers == null) return null;
        ArrayList<String> documents = new ArrayList<>();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(path));
            String s;
            int n = 0;
            while ((s = bf.readLine()) != null) {
                if (lineNumbers.contains(n)) {
                    documents.add(s);
                }
                n++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Integer i : lineNumbers) {
            System.out.println(i);
        }
        return documents;
    }

}
