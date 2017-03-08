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
    public boolean remove(Object object) {
        Iterator<SecureData<T>> it = queue.iterator();
        while (it.hasNext()) {
            SecureData<T> sd = it.next();
            if (sd.data.equals(object)) {
                return queue.remove(sd);
            }
        }
        return false;
    }


    @Override
    public T poll() {
        return setAuth(queue.poll());
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return setAuth(queue.poll(timeout, unit));
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
    public <T> T[] toArray(T[] array) {
        return toData().toArray(array);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return toData().containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return queue.addAll(fromData(collection));
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return queue.removeAll(fromData(collection));
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return queue.retainAll(fromData(collection));
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean add(T element) {
        return queue.add(new SecureData<>(element));
    }

    @Override
    public boolean offer(T element) {
        return queue.offer(new SecureData<>(element));
    }

    @Override
    public boolean offer(T element, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(new SecureData<>(element), timeout, unit);
    }

    @Override
    public void put(T element) throws InterruptedException {
        queue.put(new SecureData<>(element));
    }

    @Override
    public T take() throws InterruptedException {
        return setAuth(queue.take());
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean contains(Object object) {
        Iterator<SecureData<T>> it = queue.iterator();
        while (it.hasNext()) {
            SecureData<T> sd = it.next();
            if (sd.data.equals(object)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int drainTo(Collection<? super T> collection) {
        Collection<SecureData<T>> sd = new ArrayList<>();
        int number = queue.drainTo(sd);
        collection.addAll(toData(sd));
        return number;
    }

    @Override
    public int drainTo(Collection<? super T> collection, int maxElements) {
        Collection<SecureData<T>> sd = new ArrayList<>();
        int number = queue.drainTo(sd, maxElements);
        collection.addAll(toData(sd));
        return number;
    }

    // Rehydrate security context
    private T setAuth(SecureData<T> secureData) {
        AuthenticationManager.instance().set(secureData.auth);
        return secureData.data;
    }

    // Construct secure data collection from a plain old data collection
    @SuppressWarnings("unchecked")
    private Collection<SecureData<T>> fromData(Collection<?> collection) {
        Collection<SecureData<T>> sd = new ArrayList<>(collection.size());
        for (Object d : collection) {
            sd.add((SecureData<T>) new SecureData<>(d));
        }
        return sd;
    }

    // Extract the data portion out from the secure data
    @SuppressWarnings("unchecked")
    private Collection<T> toData() {
        return toData(Arrays.<SecureData<T>>asList(queue.toArray(new SecureData[0])));
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
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            return object instanceof SecureData ? data.equals(((SecureData) object).data) : false;
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }
}
