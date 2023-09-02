class Invocation {
    int a = 1;
    void checkForSign() {
        if (a > 0) {
            System.out.println("x");
        } else if (a < 0) {
            System.out.println("y");
        } else {
            System.out.println("z");
        }
    }
}
