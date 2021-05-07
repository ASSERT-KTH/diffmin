import java.util.function.BiFunction;

public class Lambda {
    public static void main(String[] args) {
        BiFunction<String, String, String> lambda = (x, y) -> { return y + " "; };
    }
}
