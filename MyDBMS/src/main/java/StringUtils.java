import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    public static Set<String> parsedWords(String s) {
        System.out.println(s);
        if (s == null) {
            return null;
        }
        Set<String> st = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    st.add(sb.toString());
                    sb.replace(0, sb.length(), "");
                }
            }
        }
        if (sb.length() > 0) {
            st.add(sb.toString());
        }
        return st;
    }
}
