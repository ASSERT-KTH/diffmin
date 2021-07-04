class CaseExpression {
    public void chooser(int x) {
        switch(x) {
            case 1,2:
                break;
            case 10:
                break;
        }

        int y = switch(x) {
            case 1,3 -> 0;
            default -> 5;
        };
    }
}
