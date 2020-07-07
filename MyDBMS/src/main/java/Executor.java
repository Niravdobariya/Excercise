import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class Executor {
    static MyDBMS db;
    static String metaFilePath = "/Users/niravdobariya/Desktop/MyDb/Meta/metaData";
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File f = new File(metaFilePath);
        String dbPath = "/Users/niravdobariya/Desktop/MyDb/Shards";
        if(!f.exists()) {
            db = new MyDBMS(dbPath, 3);
        } else {
            FileInputStream fis = new FileInputStream(metaFilePath);
            ObjectInputStream in = new ObjectInputStream(fis);
            db = (MyDBMS) in.readObject();
            fis.close();
            in.close();
        }

        String str;
        BufferedReader bf  =  new BufferedReader(new FileReader("/Users/niravdobariya/Desktop/enwiki.json"));
        while((str = bf.readLine()) != null) {
            db.insertDocument("Wikipedia",str);
        }

        ArrayList<String> ls = db.query("Wikipedia"," { \"keyword\" : \"form\"} ");
        FileOutputStream fos = new FileOutputStream(metaFilePath);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(db);
        fos.close();
        out.close();
    }

    static void print(Collection<?> e) {
        for(Object t : e){
            System.out.println(t);
        }
    }
}
