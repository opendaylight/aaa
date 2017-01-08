package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by ryan on 1/8/17.
 */
public class CustomFilter extends AuthorizationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomFilter.class);


    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

        LOG.error("mappedValue=" + mappedValue);

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        LOG.error("reqUI={}",httpServletRequest.getRequestURI());

        String header = httpServletRequest.getHeader("Authorization");
        String decode = new String(header);
        LOG.error("decoded={}", decode);



        return true;
    }
}
