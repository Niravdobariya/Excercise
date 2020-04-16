import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Grep7 {

    static BufferedWriter bw;
    static AtomicLong timeIO;
    static AtomicLong searchOp;

    public static void main(String args[]) throws Exception {

        timeIO = new AtomicLong();
        searchOp = new AtomicLong();
        String pattern = args[0];
        long time = System.currentTimeMillis();

        bw = new BufferedWriter(new FileWriter("/Users/niravdobariya/Desktop/output.txt"));
        ExecutorService poolExecutor = new MyThreadPoolExecutor(8);
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
                } catch (IOException e) {
                    System.out.println(dir.getName() + " : Exception caught during reading File");
                }
            });

        }
    }
    private static boolean fileSearch(File file, Pattern p) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        FileChannel in = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        char[] text = new char[8192];
        int cnt = 0;
        boolean flag = true;
        Matcher m = p.matcher();
        while(in.read(buffer) > 0)
        {
            buffer.flip();
            for (int i = 0; i < buffer.limit(); i++)
            {
                text[i] = (char) buffer.get();
            }
            cnt += m.search(text);
            buffer.clear();
        }
        System.out.println(file.getPath() + " : " + cnt);
        if(cnt>0)flag =true;
        in.close();
        fis.close();
        return flag;
    }

}
