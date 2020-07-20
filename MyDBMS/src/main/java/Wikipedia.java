public class Wikipedia {
    public String title;
    public String id;
    public Revision revision;

    @Override
    public String toString() {
        return "Wikipedia{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", revision=" + revision +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return revision.text;
    }

}
