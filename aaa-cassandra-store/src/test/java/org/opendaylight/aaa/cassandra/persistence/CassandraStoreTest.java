package org.opendaylight.aaa.cassandra.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;

/**
 * Created by root on 11/29/15.
 */
public class CassandraStoreTest {
    private CassandraStore store = new CassandraStore();
    private static final boolean USE_REAL_CASSANDRA = false;
    @Test
    public void test() throws Exception{
        if(USE_REAL_CASSANDRA) {
            DomainStore domainStore = store.getDomainStore();
            Domain domain = new Domain();
            domain.setName("sdn");
            domain.setEnabled(true);
            domain.setDescription("SDN Domain");
            Domain d = domainStore.createElement(domain);
            Assert.assertNotNull(d);
            d = domainStore.getElement("sdn");
            Assert.assertNotNull(d);
            Domains domains = domainStore.getCollection();
            Assert.assertNotNull(domains);
            Assert.assertTrue(domains.getDomains().size()==1);
            Assert.assertNotNull(domains.getDomains().get(0).getDescription());
            domainStore.dbClean();
        }
    }
}
