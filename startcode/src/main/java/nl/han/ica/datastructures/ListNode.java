package nl.han.ica.datastructures;

public class ListNode <T>{
    public T element;
    public ListNode<T> next;

    public ListNode(Object value){
        this.element = (T) value;
    }
}
