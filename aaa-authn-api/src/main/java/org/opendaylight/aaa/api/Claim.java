package org.opendaylight.aaa.api;

import java.util.Set;

/**
 * A claim typically provided by an identity provider after validating the
 * needed identity and credentials.
 *
 * @author liemmn
 *
 */
public interface Claim {
    /**
     * Get the user id.
     *
     * @return unique user id
     */
    String userId();

    /**
     * Get the user name.
     *
     * @return user name
     */
    String userName();

    /**
     * Get the tenant id.
     *
     * @return unique tenant id
     */
    String tenantId();

    /**
     * Get the tenant name.
     *
     * @return tenant name
     */
    String tenantName();

    /**
     * Get a set of user roles.
     *
     * @return set of user roles
     */
    Set<String> roles();

}
