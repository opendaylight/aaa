package org.opendaylight.aaa.persistence.impl.hibernate.search;

import org.opendaylight.aaa.persistence.api.Order;
import org.opendaylight.aaa.persistence.api.Orderable;

import java.util.ArrayList;
import java.util.List;

public class SearchOrderable implements Orderable {

    List<Order> orders = new ArrayList<Order>();

    @Override
    public List<Order> orders() {
        return orders;
    }
}
