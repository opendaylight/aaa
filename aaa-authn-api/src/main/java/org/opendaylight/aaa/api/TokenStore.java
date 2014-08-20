package org.opendaylight.aaa.api;

/**
 * A datastore for auth tokens.
 *
 * @author liemmn
 *
 */
public interface TokenStore {
    void put(String token, Authentication auth);

    Authentication get(String token);

    boolean delete(String token);

    long tokenExpiration();
}
