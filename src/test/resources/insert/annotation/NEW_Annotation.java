interface INothing {
    public void doNothing();
}

class Annotation implements INothing {
    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public void doNothing() { }
}
