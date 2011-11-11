import org.stringtemplate.v4.ST;


public class STTest {
    public static void main(String[] args) {
        ST hello = new ST("Hello, <name>");
        System.out.println(hello.getAttributes());
        hello.add("name", "World");
        System.out.println(hello.render());
    }
}
