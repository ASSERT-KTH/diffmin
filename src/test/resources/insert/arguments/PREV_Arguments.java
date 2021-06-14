class Arguments {
    private int x;
    private int y;

    public void add() {
        Addition add = new Addition(x, y);
    }
}

class Addition {
    Addition(int... numbers) {
        System.out.println("Sum all numbers");
    }
}
