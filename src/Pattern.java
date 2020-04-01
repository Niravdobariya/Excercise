public class Pattern {
    public final String p;
    public Node[] lps;

    private class Node {
        int index;
        Node(int n) {
            index = n;
        }
    }

    public Pattern(String pattern) {
        this.p = pattern;
        this.lps = new Node[p.length()];
        initialize();
    }

    public int getIndex(int i) {
        return lps[i].index;
    }

    public Matcher matcher() {
        return new Matcher(this);
    }

    void initialize() {
        Node node = new Node(0);
        lps[0] = node;
        int i = 1;
        int length = 0;
        while (i < p.length()) {
            if(p.charAt(i) == p.charAt(length)) {
                node = new Node(++length);
                lps[i] = node;
                i++;
            } else {
                if(length == 0) {
                    node = new Node(0);
                    lps[i] = node;
                    i++;
                } else {
                    length = lps[length-1].index;
                }
            }
        }
    }
}
