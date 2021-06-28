public abstract class Modifier {
    public static int x;
    protected static int y;
    private static int z;

    public final void doNothing() {
        final int x = 10;
    }
}
