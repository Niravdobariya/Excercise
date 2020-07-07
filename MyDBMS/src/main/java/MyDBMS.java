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
        shards =  new HashMap<>();
        this.noOfShards = noOfShards;
    }

    public ArrayList<String> query(String collectionName, String query) {
        QueryBean q = getShardKey(query);
        Integer shardKey = q.id;
        String keyword = q.keyword;
        Gson g = new Gson();

//        System.out.println(shardKey + " " + keyword);

        if(shardKey == null) {
            ArrayList<String> result = broadcastQuery(collectionName, keyword);
            return result;
        }
        Shard shard = shards.get(shardKey % noOfShards);
        return shard.getDocument(collectionName, keyword);
    }

    public void insertDocument(String collectionName, String document)  {
        QueryBean q = getShardKey(document);
        Integer shardKey = (q.id) % noOfShards;
        Shard shard = shards.get(shardKey);
        if(shard == null) {
            shard = new Shard(path, shardKey.toString());
            shards.put(shardKey,shard);
        }
        shard.insertIntoCollection(collectionName,document);
    }

    private QueryBean getShardKey(String document) {
        Gson g = new Gson();
        return g.fromJson(document, QueryBean.class);
    }

    private ArrayList<String> broadcastQuery(String collectionName, String keyword) {
        ArrayList<String> result = new ArrayList<>();
        if(poolExecutor == null || poolExecutor.isShutdown()) {
            poolExecutor = Executors.newFixedThreadPool(3);
        }
        Collection<Shard> values = shards.values();
        ArrayList<Future<ArrayList<String>>> tasks =  new ArrayList<>();
        for(Shard s : values) {
            Future task = poolExecutor.submit(new Callable<ArrayList<String>>() {
                @Override public ArrayList<String> call() {
                    return s.getDocument(collectionName, keyword);
                }
            });
            tasks.add(task);
        }

        poolExecutor.shutdown();
        try {
            poolExecutor.awaitTermination(5,TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(Future<ArrayList<String>> task : tasks) {
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
