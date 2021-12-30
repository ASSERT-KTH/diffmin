interface INothing {
    public void doNothing();
}

class Annotation implements INothing {
    @Deprecated
    public void doNothing() { }
}
