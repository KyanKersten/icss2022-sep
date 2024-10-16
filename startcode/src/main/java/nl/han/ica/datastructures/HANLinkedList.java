package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList {
    private ListNode<T> headerNode;
    private int size;

    public HANLinkedList(){
        this.headerNode = null;
        size = 0;
    }

    @Override
    public void addFirst(Object value) {
        ListNode node = new ListNode(value);
        node.next = this.headerNode;
        this.headerNode = node;
        this.size++;
    }

    @Override
    public void clear() {
        this.headerNode = null;
        this.size = 0;
    }

    @Override
    public void insert(int index, Object value) {
        if (index < 0 || index > this.size){
            System.out.println("index out of bounds");
        }

        ListNode node = new ListNode(value);
        if (index == 0){
            addFirst(value);
            return;
        }

        ListNode current = this.headerNode;
        for (int i = 1; i < index; i++){
            current = current.next;
        }
        node.next = current.next;
        current.next = node;
        this.size++;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= this.size){
            System.out.println("Exceeded the size of the list");
        }
        if (pos == 0){
            removeFirst();
            return;
        }

        ListNode current = this.headerNode;
        ListNode prevNode = null;
        for (int i = 0; i < pos; i++){
            prevNode = current;
            current = current.next;
        }
        prevNode.next = current.next;
        this.size--;
    }

    @Override
    public Object get(int pos) {
       if (pos < 0 || pos >= this.size){
           System.out.println("List is empty");
           return null;
       };
       ListNode node = this.headerNode;
       for(int i = 0; i < pos; i++){
           node = node.next;
       }
       return node.element;
    }

    @Override
    public void removeFirst() {
        if (this.headerNode == null){
            System.out.println("List is empty");
            return;
        }
        this.headerNode = this.headerNode.next;
        this.size--;
    }

    @Override
    public Object getFirst() {
        if (this.headerNode == null){
            System.out.println("List is empty");
            return null;
        }
        return headerNode.element;
    }

    @Override
    public int getSize() {
        return size;
    }
}
