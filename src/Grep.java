import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Grep {
    
    public static void main(String args[]) throws Exception{
        String pattern = args[0];
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        for (int i = 1; i < args.length; i++) {
            try {
                search(poolExecutor,args[i],pattern);
            } catch (IOException e) {
                System.out.println(e.getCause());
            }
        }
        poolExecutor.shutdown();
        poolExecutor.awaitTermination(10L, TimeUnit.MINUTES);
    }

    //----------------------------Private Methods-----------------------------------//

    public static void search(ThreadPoolExecutor poolExecutor, String path, String regex) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException(path + " : No such File or Directory");
        }
        Pattern pattern = new Pattern(regex);
        searchInDirectory(poolExecutor,file, pattern);
    }

    public static void searchInDirectory(ThreadPoolExecutor poolExecutor,File dir, Pattern pattern) {
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            for (File file : files) {
                searchInDirectory(poolExecutor,file, pattern);
            }
        } else {
            poolExecutor.submit(() -> {
                if (fileSearch(dir, pattern)) {
                    System.out.println(dir.getName() + " contains  " + pattern.p);
                }
            });

        }
    }

    private static boolean fileSearch(File file, Pattern p) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (IOException e) {
            System.out.println("File not found : " + file.getName());
            return false;
        }
        BufferedReader br = new BufferedReader(fr);
        Matcher matcher = p.matcher();
        char[] buff = new char[1000];
        while (true) {

            int size = 0;
            try {
                if ((size = br.read(buff, 0, 1000)) <= 0) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (matcher.search(buff, size)) {
                return true;
            }
        }
        return false;
    }
}
