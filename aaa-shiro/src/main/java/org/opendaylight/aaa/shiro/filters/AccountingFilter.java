package org.opendaylight.aaa.shiro.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * AccountingFilter is responsible for reporting <code>HttpServletRequest</code>
 * information about incoming packets.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class AccountingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AccountingFilter.class);
    private static final String HOST_HEADER = "Host";

    private static String hostname;

    /**
     * If enabled, this checks the "Host" header against the FQDN for mismatch.
     */
    private static boolean ensureFQDN = true;

    @Override
    public void destroy() {
        LOG.info("Destroying AccountingFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (ensureFQDN) {
            doHostnameMismatchCheck(request);
        }
        chain.doFilter(request, response);
    }

    private static void doHostnameMismatchCheck(final ServletRequest request) {
        try {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final Map<String, String> headerMap = extractHeadersAsStrings(httpRequest);

            // contains the host and the port
            final String hostAndPort = headerMap.get(HOST_HEADER);
            if (hostAndPort != null) {
                final String host = hostAndPort.split(":")[0];
                if (!host.equalsIgnoreCase(hostname)) {
                    LOG.debug("Mismatch between request \"Host\" header and ODL server Hostname."
                            + "  Request \"Host\" header=\"{}\" and ODL server Hostname=\"{}\"",
                            host, hostname);
                }
            } else {
                LOG.debug("Request received without the \"Host\" header set.");
            }

        } catch(ClassCastException e) {
            LOG.debug("Unable to decode the request as an HttpServletRequest."
                    + "  Unable to check whether the incoming request used FQDN.", e);
        }
    }

    private static Map<String, String> extractHeadersAsStrings(final HttpServletRequest request) {
        final Map<String, String> headerMap = Maps.newHashMap();

        final Enumeration<?> headerNames = request.getHeaderNames();
        String headerName;
        String headerValue;
        while (headerNames.hasMoreElements()) {
            try {
                headerName = (String) headerNames.nextElement();
                headerValue = request.getHeader(headerName);
                headerMap.put(headerName, headerValue);
            } catch(ClassCastException e) {
                LOG.error("Unable to decode header as String", e);
            }
        }

        return headerMap;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
        LOG.info("Intializing AccountingFilter");
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            LOG.error("Unable to determine the local hostname", e);
            ensureFQDN = false;
        }
    }
}
