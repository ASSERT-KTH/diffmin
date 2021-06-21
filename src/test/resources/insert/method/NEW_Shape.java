public interface Shape {
    String getName();
    int getPerimeter();
    int getArea();
}

class Square implements Shape {
    private int side;

    public String getName() {
        return "Square";
    }

    public int getPerimeter() {
        return side*4;
    }

    public int getArea() {
        return side*side;
    }
}
