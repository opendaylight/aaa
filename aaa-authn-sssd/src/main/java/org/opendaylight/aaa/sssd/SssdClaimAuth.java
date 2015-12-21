/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sssd;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import org.apache.felix.dm.Component;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.idpmapping.RuleProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SSSD {@link ClaimAuth} implementation.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */
public class SssdClaimAuth implements ClaimAuth {
    private static final Logger LOG = LoggerFactory.getLogger(SssdClaimAuth.class);

    private static final String DEFAULT_MAPPING_RULES_PATHNAME = "etc/idp_mapping_rules.json";
    private JsonGeneratorFactory generatorFactory = null;
    private RuleProcessor ruleProcessor = null;

    // Called by DM when all required dependencies are satisfied.
    void init(Component c) {
        LOG.info("Initializing SSSD Plugin");
        Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        generatorFactory = Json.createGeneratorFactory(properties);

        String mappingRulesFile = DEFAULT_MAPPING_RULES_PATHNAME;
        if (mappingRulesFile == null || mappingRulesFile.isEmpty()) {
            LOG.warn("mapping rules file is not configured, " + "SssdClaimAuth will be disabled");
            return;
        }

        Path mappingRulesPath = Paths.get(mappingRulesFile);

        if (!Files.exists(mappingRulesPath)) {
            LOG.warn(String.format("mapping rules file (%s) "
                    + "does not exist, SssdClaimAuth will be disabled", mappingRulesFile));
            return;
        }

        try {
            ruleProcessor = new RuleProcessor(mappingRulesPath, null);
        } catch (Exception e) {
            LOG.error(String.format("mapping rules file (%s) "
                    + "could not be loaded, SssdClaimAuth will be disabled. " + "error = %s",
                    mappingRulesFile, e));
        }
    }

    /**
     * Transform a Map of assertions into a {@link Claim} via a set of mapping
     * rules.
     *
     * A set of mapping rules have been previously loaded. the incoming
     * assertion is converted to a JSON document and presented to the
     * {@link RuleProcessor}. If the RuleProcessor can successfully transform
     * the assertion given the site specific set of rules it will return a Map
     * of values which will then be used to build a {@link Claim}. The rule
     * should return one or more of the following which will be used to populate
     * the Claim.
     *
     * <dl>
     * <dt>ClientId</dt>
     * <dd>A string.
     *
     * @see org.opendaylight.aaa.api.Claim#clientId() </dd>
     *
     *      <dt>UserId</dt> <dd>A string.
     * @see org.opendaylight.aaa.api.Claim#userId() </dd>
     *
     *      <dt>User</dt> <dd>A string.
     * @see org.opendaylight.aaa.api.Claim#user() </dd>
     *
     *      <dt>Domain</dt> <dd>A string.
     * @see org.opendaylight.aaa.api.Claim#domain() </dd>
     *
     *      <dt>Roles</dt> <dd>An array of strings.
     * @see org.opendaylight.aaa.api.Claim#roles() </dd>
     *
     *      </dl>
     *
     * @param assertion
     *            A Map of name/value assertions provided by an external IdP
     * @return A {@link Claim} if successful, null otherwise.
     */

    @Override
    public Claim transform(Map<String, Object> assertion) {
        String assertionJson;
        Map<String, Object> mapped;
        assertionJson = claimToJson(assertion);

        if (ruleProcessor == null) {
            LOG.debug("ruleProcessor not configured");
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("assertionJson=\n{}", assertionJson);
        }

        mapped = ruleProcessor.process(assertionJson);
        if (mapped == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("RuleProcessor returned null");
            }
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RuleProcessor returned: {}", mapped);
        }

        ClaimBuilder cb = new ClaimBuilder();
        if (mapped.containsKey("ClientId")) {
            cb.setClientId((String) mapped.get("ClientId"));
        }
        if (mapped.containsKey("UserId")) {
            cb.setUserId((String) mapped.get("UserId"));
        }
        if (mapped.containsKey("User")) {
            cb.setUser((String) mapped.get("User"));
        }
        if (mapped.containsKey("Domain")) {
            cb.setDomain((String) mapped.get("Domain"));
        }
        if (mapped.containsKey("Roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) mapped.get("roles");
            for (String role : roles) {
                cb.addRole(role);
            }
        }
        Claim claim = cb.build();

        if (LOG.isDebugEnabled()) {
            LOG.debug("returns claim = {}", claim.toString());
        }

        return claim;
    }

    /**
     * Convert a Claim Map into a JSON object.
     *
     * Given a Map of name/value pairs convert it into a JSON object and return
     * it as a string. This is not a general purpose routine used to convert any
     * Map into JSON because a claim has the restriction that each value must be
     * a scalar and those scalars are restricted to the following types:
     *
     * <ul>
     * <li>String</li>
     * <li>Integer</li>
     * <li>Long</li>
     * <li>Double</li>
     * <li>Boolean</li>
     * <li>null</li>
     * </ul>
     *
     * See also {@link ClaimAuth}.
     *
     * @param claim
     *            The Map containing assertion claims to be converted into a
     *            JSON assertion document.
     * @return A string formatted as a JSON object.
     */

    public String claimToJson(Map<String, Object> claim) {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = generatorFactory.createGenerator(stringWriter);

        generator.writeStartObject();
        for (Map.Entry<String, Object> entry : claim.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                generator.write(name, (String) value);
            } else if (value instanceof Integer) {
                generator.write(name, ((Integer) value).intValue());
            } else if (value instanceof Long) {
                generator.write(name, ((Long) value).longValue());
            } else if (value instanceof Double) {
                generator.write(name, ((Double) value).doubleValue());
            } else if (value instanceof Boolean) {
                generator.write(name, ((Boolean) value).booleanValue());
            } else if (value == null) {
                generator.write(name, JsonValue.NULL);
            } else {
                LOG.warn(String.format("ignoring claim unsupported value type "
                        + "entry %s has type %s", name, value.getClass().getSimpleName()));
            }
        }
        generator.writeEnd();
        generator.close();
        return stringWriter.toString();
    }
}
