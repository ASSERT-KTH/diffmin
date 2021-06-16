class Invocation {
    int a = 1;
    void checkForSign() {
        if (a > 0) {
            System.out.println("y");
        } else if (a < 0) {
            System.out.println("z");
        } else {
            System.out.println("x");
        }
    }
}
