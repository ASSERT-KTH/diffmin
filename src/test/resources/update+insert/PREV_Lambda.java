import java.util.function.Function;

public class Lambda {
    public static void main(String[] args) {
        Function<String, String> lambda = (y) -> { return y + " "; };
    }
}
