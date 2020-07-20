

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

public class MyDBMS implements Serializable {

    final String path;
    transient ExecutorService poolExecutor;
    HashMap<Integer, Shard> shards;
    final int noOfShards;

    public MyDBMS(String path, int noOfShards) {
        this.path = path;
        shards = new HashMap<>();
        this.noOfShards = noOfShards;
    }

    public ArrayList<String> query(String collectionName, String query) {
        Map<String, Object> queryMap = (Map) GsonParser.parse(query, Map.class);
        Double val = (Double) queryMap.get("id");
        Integer shardKey = val.intValue();

        if (shardKey == null) {
            return broadcastQuery(collectionName, queryMap.get("keyword"), (String) queryMap.get("indexName"));
        }
        Shard shard = shards.get(shardKey % noOfShards);
        return shard.getDocument(collectionName, queryMap.get("keyword"), (String) queryMap.get("indexName"));
    }

    public void insertDocument(String collectionName, String document) {
        Map<String, Object> documentMap = (Map) GsonParser.parse(document, Map.class);
        Double val = (Double) documentMap.get("id");
        Integer shardKey = val.intValue();

        Shard shard = shards.get(shardKey);
        if (shard == null) {
            shard = new Shard(path, shardKey.toString());
            shards.put(shardKey % noOfShards, shard);
        }
        shard.insertIntoCollection(collectionName, document);
    }

    private ArrayList<String> broadcastQuery(String collectionName, Object keyword, String indexName) {
        ArrayList<String> result = new ArrayList<>();
        if (poolExecutor == null || poolExecutor.isShutdown()) {
            poolExecutor = Executors.newFixedThreadPool(3);
        }
        Collection<Shard> values = shards.values();
        ArrayList<Future<ArrayList<String>>> tasks = new ArrayList<>();
        for (Shard s : values) {
            Future task = poolExecutor.submit(new Callable<ArrayList<String>>() {
                @Override
                public ArrayList<String> call() {
                    return s.getDocument(collectionName, keyword, indexName);
                }
            });
            tasks.add(task);
        }

        poolExecutor.shutdown();
        try {
            poolExecutor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Future<ArrayList<String>> task : tasks) {
            try {
                ArrayList<String> ls = task.get();
                System.out.println(ls.size() + " " + task);
                result.addAll(ls);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


}
