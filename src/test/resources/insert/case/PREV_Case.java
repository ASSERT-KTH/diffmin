class Case {
    public void func() {
        int x = 0;
        switch (x % 2) {
            case 1:
                System.out.println("Odd");
                break;
            default:
                System.out.println("Fraction");
                break;
        }

        switch (x % 3) { }

        int y = switch (x) {
            case 1 -> 10;
            default -> 30;
        };
    }
}
