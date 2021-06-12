class Arguments {
    private int x;
    private int y;
    private int z;

    public void add() {
        Addition add = new Addition(x, y, z);
    }
}

class Addition {
    Addition(int... numbers) {
        System.out.println("Sum all numbers");
    }
}
