class LiteralWhileParameter {
    public void main(String[] args) {
        int i = 0;
        while(i<10) {
            System.out.println(i);
            i = i + 2;
        }
        assert i == 10;
    }
}
