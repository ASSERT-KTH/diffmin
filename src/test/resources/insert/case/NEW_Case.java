class Case {
    public void func() {
        int x = 0;
        switch (x % 2) {
            case 1:
                System.out.println("Odd");
                break;
            case 0:
                System.out.println("Even");
                break;
            default:
                System.out.println("Fraction");
                break;
        }

        switch (x % 3) {
            default:
                System.out.println("Checking for divisibility by 3");
                break;
        }

        int y = switch (x) {
            case 1 -> 10;
            case 2 -> 20;
            default -> 30;
        };
    }
}
