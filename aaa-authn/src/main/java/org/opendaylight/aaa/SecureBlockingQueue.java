/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.opendaylight.aaa.api.Authentication;

/**
 * A {@link BlockingQueue} decorator with injected security context.
 *
 * @author liemmn
 *
 * @param <T>
 *            queue element type
 */
public class SecureBlockingQueue<T> implements BlockingQueue<T> {
    private final BlockingQueue<SecureData<T>> queue;

    /**
     * Constructor.
     *
     * @param queue
     *            blocking queue implementation to use
     */
    public SecureBlockingQueue(BlockingQueue<SecureData<T>> queue) {
        this.queue = queue;
    }

    @Override
    public T remove() {
        return setAuth(queue.remove());
    }

    @Override
    public T poll() {
        return setAuth(queue.poll());
    }

    @Override
    public T element() {
        return setAuth(queue.element());
    }

    @Override
    public T peek() {
        return setAuth(queue.peek());
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<SecureData<T>> it = queue.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next().data;
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return toData().toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        return toData().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return toData().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return queue.addAll(fromData(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(fromData(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(fromData(c));
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean add(T e) {
        return queue.add(new SecureData<>(e));
    }

    @Override
    public boolean offer(T e) {
        return queue.offer(new SecureData<>(e));
    }

    @Override
    public void put(T e) throws InterruptedException {
        queue.put(new SecureData<T>(e));
    }

    @Override
    public boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(new SecureData<>(e), timeout, unit);
    }

    @Override
    public T take() throws InterruptedException {
        return setAuth(queue.take());
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return setAuth(queue.poll(timeout, unit));
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        Iterator<SecureData<T>> it = queue.iterator();
        while (it.hasNext()) {
            SecureData<T> sd = it.next();
            if (sd.data.equals(o)) {
                return queue.remove(sd);
            }
        }
        return false;
    }

    @Override
    public boolean contains(Object o) {
        Iterator<SecureData<T>> it = queue.iterator();
        while (it.hasNext()) {
            SecureData<T> sd = it.next();
            if (sd.data.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        Collection<SecureData<T>> sd = new ArrayList<>();
        int n = queue.drainTo(sd);
        c.addAll(toData(sd));
        return n;
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        Collection<SecureData<T>> sd = new ArrayList<>();
        int n = queue.drainTo(sd, maxElements);
        c.addAll(toData(sd));
        return n;
    }

    // Rehydrate security context
    private T setAuth(SecureData<T> i) {
        AuthenticationManager.instance().set(i.auth);
        return i.data;
    }

    // Construct secure data collection from a plain old data collection
    @SuppressWarnings("unchecked")
    private Collection<SecureData<T>> fromData(Collection<?> c) {
        Collection<SecureData<T>> sd = new ArrayList<>(c.size());
        for (Object d : c) {
            sd.add((SecureData<T>) new SecureData<>(d));
        }
        return sd;
    }

    // Extract the data portion out from the secure data
    @SuppressWarnings("unchecked")
    private Collection<T> toData() {
        return toData(Arrays.<SecureData<T>> asList(queue.toArray(new SecureData[0])));
    }

    // Extract the data portion out from the secure data
    private Collection<T> toData(Collection<SecureData<T>> secureData) {
        Collection<T> data = new ArrayList<>(secureData.size());
        Iterator<SecureData<T>> it = secureData.iterator();
        while (it.hasNext()) {
            data.add(it.next().data);
        }
        return data;
    }

    // Inject security context
    public static final class SecureData<T> {
        private final T data;
        private final Authentication auth;

        private SecureData(T data) {
            this.data = data;
            this.auth = AuthenticationManager.instance().get();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            return (o instanceof SecureData) ? data.equals(((SecureData) o).data) : false;
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }
}
