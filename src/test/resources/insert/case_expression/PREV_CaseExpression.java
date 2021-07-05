class CaseExpression {
    public void chooser(int x) {
        switch(x) {
            case 1:
                break;
            default:
                break;
        }

        int y = switch(x) {
            case 1 -> 0;
            default -> 5;
        };
    }
}
