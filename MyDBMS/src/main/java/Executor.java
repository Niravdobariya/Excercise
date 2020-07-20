import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class Executor {
    static MyDBMS db;
    static String metaFilePath = "/Users/niravdobariya/Desktop/MyDb/Meta/metaData";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File f = new File(metaFilePath);
        String dbPath = "/Users/niravdobariya/Desktop/MyDb/Shards";
        if (!f.exists()) {
            db = new MyDBMS(dbPath, 3);
        } else {
            FileInputStream fis = new FileInputStream(metaFilePath);
            ObjectInputStream in = new ObjectInputStream(fis);
            db = (MyDBMS) in.readObject();
            fis.close();
            in.close();
        }

        String str;
        BufferedReader bf = new BufferedReader(new FileReader("/Users/niravdobariya/Desktop/10M.json"));
        while ((str = bf.readLine()) != null) {
//            System.out.println(str);
            db.insertDocument("Wikipedia", str);
        }

        ArrayList<String> ls = db.query("Wikipedia", " { \"id\" : 864 , \"keyword\" : 864 , \"indexName\" : \"id\"}   ");
//        ArrayList<String> ls1 = db.query("Wikipedia"," { \"id\" : \"2\" , \"keyword\" : \"2\" , \"indexName\" : \"id\"}   ");
//        ArrayList<String> ls2 = db.query("Wikipedia"," { \"id\" : \"3\" , \"keyword\" : \"3\" , \"indexName\" : \"id\"}   ");
        print(ls);
//        print(ls1);
//        print(ls2);
        // {"title":"Andy Warhol","ns":0,"id":864,"revision":{"id":961772665}
        /*
         * 0 34
         * 1 29
         * 2 34
         * */
//        print(ls);
        FileOutputStream fos = new FileOutputStream(metaFilePath);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(db);
        fos.close();
        out.close();
    }

    static void print(Collection<?> e) {
        if (e == null) return;
        for (Object t : e) {
            System.out.println(t);
        }
    }
}
