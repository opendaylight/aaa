package org.opendaylight.aaa.impl.shiro.realm.util;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

public class TokenUtils {
    /**
     * extract the username from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    public static String extractUsername(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        return (String) authenticationToken.getPrincipal();
    }

    /**
     * extract the password from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    public static String extractPassword(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        return new String(upt.getPassword());
    }
}
