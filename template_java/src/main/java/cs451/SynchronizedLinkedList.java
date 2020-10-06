package cs451;

import java.util.LinkedList;

public class SynchronizedLinkedList<T> {
    private LinkedList<T> list;

    public SynchronizedLinkedList() {
        this.list = new LinkedList<>();
    }

    public synchronized void add(T p) {
        this.list.addLast(p);
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return this.list.isEmpty();
    }

    public synchronized T removeFirst() {
        while (this.list.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.list.removeFirst();
    }
    
}
