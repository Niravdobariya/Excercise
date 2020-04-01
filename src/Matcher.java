import java.util.ArrayList;
import java.util.List;

public class Matcher {
    final Pattern pattern;
    int curInd = 0;

    public Matcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean search(char[] text, int size) {
        int i = 0;
        int j = curInd;
        int cnt = 0 ;
        List<Integer> indexes = new ArrayList<>();
        while(i < size) {
            cnt++;
            char c = text[i];
            char p = pattern.p.charAt(j);
            if(c == p) {
                i++;
                j++;
                if(j == pattern.p.length()) {
                    indexes.add(i-j);
                    j = pattern.getIndex(j-1);
                }
            } else {

                if (j == 0) {
                    i++;
                } else {
                    j = pattern.getIndex(j-1);
                }
            }
        }
        curInd = j;
        return indexes.size()>0;
    }
}
