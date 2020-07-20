import com.google.gson.Gson;

public class GsonParser {
    private static Gson g;

    public static Object parse(String json, Class<? extends Object> c) {
        if (g == null) {
            g = new Gson();
        }
        return g.fromJson(json, c);
    }
}
