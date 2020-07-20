import java.io.Serializable;
import java.util.*;

public class Shard implements Serializable {

    final String pathPrefix;
    final String shardName;
    Map<String, DBCollection> collections;

    public Shard(String path, String shardName) {
        collections = new HashMap<>();
        pathPrefix = path + '/' + shardName + '/';
        this.shardName = shardName;
    }

    public void insertIntoCollection(String collectionName, String document) {
        DBCollection collection = collections.get(collectionName);
        if (collection == null) {
            collection = new DBCollection(pathPrefix, collectionName);
            collections.put(collectionName, collection);
        }
        collection.insert(document);
    }

    public ArrayList<String> getDocument(String collectionName, Object keyword, String indexName) {
        System.out.println("keyword = " + keyword);
        DBCollection collection = collections.get(collectionName);
        ArrayList<String> ls = collection.getDocumentsByIndex(keyword, indexName);
        return ls;
    }

}
