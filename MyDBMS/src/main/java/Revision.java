public class Revision {
    public String text;
    public String comment;
    public transient String format;
    public transient String model;

    @Override
    public String toString() {
        return "Revision{" +
                "text='" + text + '\'' +
                ", comment='" + comment + '\'' +
                ", format='" + format + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
