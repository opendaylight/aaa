/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Implementation of a Radius AuthorizingRealm.
 *
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class RadiusRealm extends AuthorizingRealm {

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
    // TODO use JRadius to extract Authorization Info
    return null;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken arg0)
      throws AuthenticationException {
    // TODO use JRadius to extract Authentication Info
    return null;
  }

}
