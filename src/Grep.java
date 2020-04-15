import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class Grep {

    static BufferedWriter bw;
    public static void main(String args[]) throws Exception {


        String pattern = args[0];
        long time = System.currentTimeMillis();

        bw = new BufferedWriter(new FileWriter("/Users/niravdobariya/Desktop/output.txt"));
        ExecutorService poolExecutor = Executors.newFixedThreadPool(2);
        for (int i = 1; i < args.length; i++) {
            try {
                search(poolExecutor, args[i], pattern);
            } catch (IOException e) {
                System.out.println(e.getCause());
            }
        }
        poolExecutor.shutdown();
        poolExecutor.awaitTermination(10L, TimeUnit.MINUTES);
        System.out.println(System.currentTimeMillis() - time);
        bw.flush();
        bw.close();
    }

    //----------------------------Private Methods-----------------------------------//

    private static void search(ExecutorService poolExecutor, String path, String regex) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException(path + " : No such File or Directory");
        }
        Pattern pattern = new Pattern(regex);
        int cnt = 0;
        searchInDirectory(cnt, poolExecutor, file, pattern);
    }

    private static void searchInDirectory(int cnt, ExecutorService poolExecutor, File dir, Pattern pattern) {
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            for (File file : files) {
                searchInDirectory(cnt + 1, poolExecutor, file, pattern);
            }
        } else {
            poolExecutor.execute(() -> {
                try {
                    if (fileSearch(dir, pattern)) {
                        //                    System.out.println(dir.getName() + " contains  " + pattern.p);
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.out.println(dir.getPath() + " : Exception caught during reading File");
                }
            });

        }
    }

    private static boolean fileSearch(File file, Pattern p) throws IOException, InterruptedException, ExecutionException {
        int poolSize = 5;
        ExecutorService poolExecutor = Executors.newFixedThreadPool(poolSize);
        long chunkSize = 8192;
        long cnt = (file.length() + chunkSize - 1) / chunkSize;
        int taskAtTime = 102400;
        int matchedCount = 0;
        long start = 0;
        long end = Long.min(chunkSize, file.length());
        List<Callable<char[]>> tasks = new ArrayList<>();
        Matcher m = p.matcher();
        boolean flag = false;

        while (cnt > 0) {
            int bound = (int) Long.min(cnt, taskAtTime);

            List<Future<char[]>> futures = new ArrayList<>();
            for (int i = 0; i < bound; i++) {
                futures.add(poolExecutor.submit(createTask(file, start, end)));
                start = end;
                end = Long.min(end + chunkSize, file.length());
            }
            for (Future<char[]> future : futures) {
                char[] s = future.get();
                if (s == null) {
                    continue;
                }
                matchedCount += m.search(s);
            }
            cnt -= bound;
        }
        System.out.println(file.getPath() + " : " + matchedCount);
        poolExecutor.shutdown();
        if(matchedCount > 0) flag = true;
        return flag;
    }

    private static char[] processPart(File file, long start, long end) throws IOException {

        RandomAccessFile rFile = null;
        FileChannel fc = null;
        try {

            rFile = new RandomAccessFile(file.getPath(),"r");
            rFile.seek(start);
            fc = rFile.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(8192);
            if(start > end) return null;
            int size = (int) (end - start);
            int i = 0;

            if (fc.read(bb) != -1) {

                bb.flip();
                char[] ret = new char[size];
                while(bb.hasRemaining()) {
                    ret[i++] = (char) bb.get();
                }
                bb.clear();
                return ret;
            }
            return null;
        } finally {
            if(fc != null)
                fc.close();
            if(rFile != null)
                rFile.close();
        }
    }

    private static Callable<char[]> createTask(File file, long start, long end) {
        return new Callable<char[]>() {
            @Override
            public char[] call() throws Exception {
                return processPart(file, start, end);
            }
        };
    }
}
