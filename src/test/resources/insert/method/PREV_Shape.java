public interface Shape {
    String getName();
    int getArea();
}

class Square implements Shape {
    private int side;

    public String getName() {
        return "Square";
    }

    public int getArea() {
        return side*side;
    }
}
