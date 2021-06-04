class Parameter {
    private int number;
    private int divisor;

    Parameter (int number) {
        int divisor = 1;
        this.number = number / divisor;
    }

    public int sum(int b, int c) {
        return b + c;
    }
}
