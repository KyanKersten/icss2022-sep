package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack {
    private int size;
    private HANLinkedList<T> stack;
    private int top;

    public HANStack(){
        size = 3;
        stack = new HANLinkedList<>();
        top = 0;
    }

    @Override
    public void push(Object value) {
        if (top == size) {
            size = size * 2 + 1;
        }

        stack.insert(top++, value);
    }

    @Override
    public Object pop() {
        if (top == 0) {
            return null;
        }

        T poppedElement = (T) stack.get(--top);
        stack.delete(top);
        return poppedElement;
    }

    @Override
    public Object peek() {
        if (top == 0) {
            return null;
        }
        return stack.get(top - 1);
    }
}
