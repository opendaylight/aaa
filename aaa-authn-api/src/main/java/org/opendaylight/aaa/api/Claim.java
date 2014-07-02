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
     * Get the user id. User IDs are system-created.
     *
     * @return unique user id
     */
    String userId();

    /**
     * Get the user name. User names are externally created.
     *
     * @return unique user name
     */
    String user();

    /**
     * Get the fully-qualified domain name. Domain names are externally created.
     *
     * @return unique domain name
     */
    String domain();

    /**
     * Get a set of user roles. Roles are externally created.
     *
     * @return set of user roles
     */
    Set<String> roles();

}
