package collections.concurrent.lockfree;

import collections.interfaces.List;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * An implementation of {@link List}, by lock-free.
 * Concurrent additional implementation of {@link collections.LinkedList}.
 *
 * Lock-free implementation using pointer(hashCode here) of Node.
 *
 * See some test on concurrent.lockfree.LinkedListTest
 *
 * @param <T>
 */
public class LinkedList<T extends Comparable<? super T>> implements List<T> {
    private Node<T> head;
    private Node<T> last;
    private int counter;

    /**
     * Node extends from {@link List.Node}, next as {@link AtomicMarkableReference}
     *
     * @param <F>
     */
    class Node<F extends Comparable<? super F>> extends List.Node<F> {
        int key;
        F item;
        AtomicMarkableReference<Node<F>> next;

        public Node(F item) {
            super(item);
            this.item = item;
            if (item != null) this.key = item.hashCode();
            next = new AtomicMarkableReference<>(null, false);
        }
    }

    /**
     * Window is some atomic state of node and pre-node at some point of time.
     *
     * @param <F>
     */
    class Window<F extends Comparable<? super F>> {
        Node<F> pre, cur;
        Window (Node<F> pre, Node<F> cur) {
            this.pre = pre;
            this.cur = cur;
        }
    }

    /**
     * Get window at some point of time.
     *
     * @param key hashCode of item
     * @return window at now on
     */
    Window<T> find(int key) {
        Node<T> pre;
        Node<T> cur;
        Node<T> suc;
        boolean[] marked = {false};
        boolean snip;
        retry: while (true) {
            pre = head;
            cur = pre.next.getReference();
            while (true) {
                suc = cur.next.get(marked);
                while (marked[0]) {
                    snip = pre.next.compareAndSet(cur, suc, false, false);
                    if (!snip) continue retry;
                    cur = suc;
                    suc = cur.next.get(marked);
                }

                if (cur.key >= key)
                    return new Window<>(pre, cur);
                pre = cur;
                cur = suc;
            }
        }
    }

    public LinkedList() {
        head = new Node<>(null);
        head.key = Integer.MIN_VALUE;
        last = new Node<>(null);
        last.key = Integer.MAX_VALUE;
        head.next.set(last, false);
        last.next.set(null, false);
        counter = 0;
    }

    /**
     *
     * @param item to insert
     * @return
     */
    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window<T> window = find(key);
            Node<T> pre = window.pre;
            Node<T> cur = window.cur;

            if (cur.key == key) return false;
            else {
                Node<T> node = new Node<>(item);
                node.next = new AtomicMarkableReference<>(cur, false);
                if (pre.next.compareAndSet(cur, node, false, false))
                    return true;
            }
        }
    }

    /**
     *
     * @param item to delete
     * @return
     */
    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        boolean snip;
        while (true) {
            Window<T> window = find(key);
            Node<T> pre = window.pre;
            Node<T> cur = window.cur;
            if (cur.key != key) return false;
            else {
                Node<T> suc = cur.next.getReference();
                snip = cur.next.attemptMark(suc, true);
                if (!snip) continue;
                pre.next.compareAndSet(cur, suc, false, false);
                return true;
            }
        }
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }

    @Override
    public T getFirst() {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }

    @Override
    public T getLast() {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }

    /**
     *
     * @param item to assert
     * @return
     */
    @Override
    public boolean contains(T item) {
        boolean[] marked = {false};
        int key = item.hashCode();
        Node<T> cur = head;
        while (cur.key < key) {
            cur = cur.next.getReference();
            Node suc = cur.next.get(marked);
        }
        return (cur.key == key && !marked[0]);
    }

    @Override
    public int indexOf(T item) {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }

    @Override
    public int size() {
        return counter;
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException("Not support in lockfree.LinkedList");
    }
}
