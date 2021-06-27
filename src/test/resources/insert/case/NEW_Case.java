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
    }
}
