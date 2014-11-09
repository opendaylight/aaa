package org.opendaylight.aaa.persistence.impl.hibernate;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.persistence.api.Criteria;
import org.opendaylight.aaa.persistence.api.ObjectStore;
import org.opendaylight.aaa.persistence.api.Order;
import org.opendaylight.aaa.persistence.api.Page;
import org.opendaylight.aaa.persistence.api.Predicate;
import org.opendaylight.aaa.persistence.api.Restriction;
import org.opendaylight.aaa.persistence.impl.hibernate.entity.User;
import org.opendaylight.aaa.persistence.impl.hibernate.search.SearchPagable;
import org.opendaylight.aaa.persistence.impl.hibernate.search.SearchQueryable;

import org.junit.Test;

/**
 * Requires local MySQL Instance
 * (See hibernate.cfg.xml for configuration details).
 */
public class HibernateObjectStoreTest {

    @Test
    public void testWalkthruHibernateObjectStore {
        // Note: Declare dependency on  Persistence Service (OSGI) in real system.
        HibernatePersistence persistence = new HibernatePersistence();

        ObjectStore<User, Integer> userStore = persistence.newObjectStore(User.class);


        // ------------------------------


        // Save:

        // Save 3 Users
        System.out.println("Starting Number of Records: "+ userStore.count());

        for(int i=1 ; i<=3 ; i++){
            User newUser = new User();
            newUser.setName("Mark " + i);
            newUser.setId(i);

            if((i % 2) == 1) {
                newUser.setDescription("WORK");
            } else {
                newUser.setDescription("HOME");
            }

            userStore.save(newUser);

        }
        System.out.println("Number of Records After Adding 3 Users: "+ userStore.count());


        // Save user at specific ID(1).
        User newUser = new User();
        newUser.setName("Marc");
        newUser.setDescription("MOBILE");
        userStore.save(101, newUser);
        System.out.println("Number of Records After Adding 1 Specific Id User: "+ userStore.count());


        // Save List of Users
        List<User> users = new ArrayList<User>();
        for(int i=11 ; i<=13 ; i++){
            newUser = new User();
            newUser.setName("Mark " + i);
            newUser.setId(i);

            if((i % 2) == 1) {
                newUser.setDescription("WORK");
            } else {
                newUser.setDescription("HOME");
            }

            users.add(newUser);
        }
        userStore.save(users);
        System.out.println("Number of Records After Adding 3 Users (List): "+ userStore.count());


        // -------------------------------


        // Find (Paging):

        System.out.println("Number of Records Before Paging Find: "+ userStore.count());
        SearchPagable<User, Integer> pageable = new SearchPagable<User, Integer>();
        pageable.setLimit(3);
        pageable.setMarker(1);

        Page<User, Integer> resultPage = userStore.findAll(pageable);
        Iterator<User> iter = resultPage.content();
        while(iter.hasNext()) {
            System.out.println("(Page 1): Find Page (id:1, limit:3) (Paging): "+iter.next().getId());
        }
        System.out.println("(Page 2): Previous Marker (Paging): " + resultPage.previousMarker());
        System.out.println("(Page 2): Next Marker (Paging): "+resultPage.nextMarker());



        pageable.setLimit(3);
        pageable.setMarker(resultPage.nextMarker());

        resultPage = userStore.findAll(pageable);
        iter = resultPage.content();
        while(iter.hasNext()) {
            System.out.println("(Page 2): Find Page (id:1, limit:3) (Paging): "+iter.next().getId());
        }
        System.out.println("(Page 3): Previous Marker (Paging): " + resultPage.previousMarker());
        System.out.println("(Page 3): Next Marker (Paging): "+resultPage.nextMarker());



        pageable.setLimit(2);
        pageable.setMarker(resultPage.nextMarker());

        resultPage = userStore.findAll(pageable);
        iter = resultPage.content();
        while(iter.hasNext()) {
            System.out.println("(Page 3): Find Page (id:1, limit:3) (Paging): "+iter.next().getId());
        }
        System.out.println("(Page 4): Previous Marker (Paging): " + resultPage.previousMarker());
        System.out.println("(Page 4): Next Marker (Paging): "+resultPage.nextMarker());


        // ------------------------------


        // Find (Paging + Criteria):

        System.out.println("Number of Records Before Paging+Criteria Find: "+ userStore.count());
        pageable = new SearchPagable<User, Integer>();
        pageable.setLimit(2);
        pageable.setMarker(1);

        Map<String, Restriction> restrictions = new HashMap<String, Restriction>();
        Restriction restrictDesc = new Restriction();
        restrictDesc.setPredicate(Predicate.EQ);
        restrictDesc.setValues("WORK");
        restrictions.put("description", restrictDesc);

        Criteria searchRestrictions = new SearchQueryable(restrictions, null);


        resultPage = userStore.findAll(pageable, searchRestrictions);
        iter = resultPage.content();
        while(iter.hasNext()) {
            System.out.println("(Page/Criteria 1): Find Page/Critieria: "+iter.next().getId());
        }
        System.out.println("(Page/Criteria 2): Previous Marker (Paging/Critiera): " + resultPage.previousMarker());
        System.out.println("(Page/Criteria 2): Next Marker (Paging/Critieria): "+resultPage.nextMarker());



        pageable.setLimit(2);
        pageable.setMarker(resultPage.nextMarker());

        resultPage = userStore.findAll(pageable, searchRestrictions);
        iter = resultPage.content();
        while(iter.hasNext()) {
            System.out.println("(Page/Criteria 2): Find Page/Critieria: "+iter.next().getId());
        }
        System.out.println("(Page/Criteria 3): Previous Marker (Paging/Critiera): " + resultPage.previousMarker());
        System.out.println("(Page/Criteria 3): Next Marker (Paging/Critieria): "+resultPage.nextMarker());



        // ------------------------------


        // Find:

        System.out.println("Find User id=100:"+userStore.findById(100));
        System.out.println("Find User id=101:"+userStore.findById(101));

        List<Integer> findIds = new ArrayList<Integer>(Arrays.asList(1,2,3));
        System.out.println("Find User ids=[1,2,3]:"+userStore.findById(findIds).size());

        findIds = new ArrayList<Integer>(Arrays.asList(101,102));
        System.out.println("Find User ids=[101,102]:"+userStore.findById(findIds).size());

        List<User> userList = new ArrayList<User>();
        for(User user : userStore.findAll()) {
            userList.add(user);
        }
        System.out.println("Find ALL Users:"+userList.size());


        // ------------------------------


        // Find (Criteria):

        restrictions = new HashMap<String, Restriction>();
        Restriction restrictID = new Restriction();
        restrictID.setPredicate(Predicate.GTE);
        restrictID.setValues(new Integer[] {12});
        restrictions.put("id", restrictID);

        List<Order> orders = new ArrayList<Order>();
        Order order = new Order();
        order.setDirection(Order.Direction.DESC);
        order.setAttributeName("name");
        orders.add(order);

        searchRestrictions = new SearchQueryable(restrictions, orders);
        System.out.println("Count All (Criteria: GTE {12}: "+userStore.count(searchRestrictions));

        userList = new ArrayList<User>();
        for(User user : userStore.findAll(searchRestrictions)) {
            userList.add(user);
            System.out.println("Find ALL Users (Criteria: GTE {12}: -- Found Id:"+user.getId()+" - "+user.getName());
        }


        // -------------------------------


        // Count:

        userStore.delete(13);
        System.out.println("Count All (After Delete ID: 13) (Criteria: GTE {12}: "+userStore.count(searchRestrictions));

        User user12 = userStore.findById(12);
        userStore.delete(user12);
        System.out.println("Find User id=12 (After Its Deleted): "+userStore.findById(12));


        List<Integer> deleteIds = new ArrayList<Integer>(Arrays.asList(1,2,3));
        userStore.delete(deleteIds);
        System.out.println("Find User ids=[1,2,3] (After Bulk Delete): "+userStore.findById(deleteIds).size());


        // -------------------------------


        // Delete (Criteria):

        restrictions = new HashMap<String, Restriction>();
        Restriction restrictName = new Restriction();
        restrictName.setPredicate(Predicate.EQ);
        restrictName.setValues("Marc");
        restrictions.put("name", restrictName);

        searchRestrictions = new SearchQueryable(restrictions, null);
        userStore.deleteAll(searchRestrictions);
        System.out.println("Find User Marc/id=101 (After Its Deleted): "+userStore.exists(101));


        // -------------------------------


        // Delete (All):

        System.out.println("Table Size Before Final DeleteAll: " + userStore.count());
        userStore.deleteAll();
        System.out.println("Table Size After Final DeleteAll: " + userStore.count());


        System.exit(0);
    }
}
