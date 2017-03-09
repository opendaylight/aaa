/*
 * Copyright (c) 2016, 2017 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.aaa.idpmapping.RuleProcessor.ProcessResult;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@Ignore
@PrepareForTest(RuleProcessor.class)
@RunWith(PowerMockRunner.class)
public class RuleProcessorTest {

    @Mock
    private RuleProcessor ruleProcess;

    @Before
    public void setUp() {
        ruleProcess = PowerMockito.mock(RuleProcessor.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void testJoin() {
        List<Object> list = new ArrayList<>();
        list.add("str1");
        list.add("str2");
        list.add("str3");
        assertEquals("str1/str2/str3", RuleProcessor.join(list, "/"));
    }

    @Test
    public void testSubstituteVariables() {
        Map<String, Object> namespace = new HashMap<String, Object>() {
            {
                put("foo1", new HashMap<String, String>() {
                    {
                        put("0", "1");
                    }
                });
            }
        };
        String str = "foo1[0]";
        String subVariable = ruleProcess.substituteVariables(str, namespace);
        assertNotNull(subVariable);
        assertEquals(subVariable, str);
    }

    @Test
    public void testGetMapping() {
        Map<String, Object> namespace = new HashMap<String, Object>() {
            {
                put("foo1", new HashMap<String, String>() {
                    {
                        put("0", "1");
                    }
                });
            }
        };
        final Map<String, Object> item = new HashMap<String, Object>() {
            {
                put("str", "val");
            }
        };
        Map<String, Object> rules = new HashMap<String, Object>() {
            {
                put("mapping", item);
                put("mapping_name", "mapping");
            }
        };
        Map<String, Object> mapping = ruleProcess.getMapping(namespace, rules);
        assertNotNull(mapping);
        assertTrue(mapping.containsKey("str"));
        assertEquals("val", mapping.get("str"));
    }

    @Test
    public void testProcess() throws Exception {
        List<Map<String, Object>> internalRules = new ArrayList<>();
        Map<String, Object> internalRule = new HashMap<String, Object>() {
            {
                put("Name", "Admin");
                put("statement_blocks", "user");
            }
        };
        internalRules.add(internalRule);
        MemberModifier.field(RuleProcessor.class, "rules").set(ruleProcess, internalRules);
        PowerMockito.suppress(MemberMatcher.method(RuleProcessor.class, "processRule", Map.class,
                Map.class));
        PowerMockito.when(ruleProcess, "processRule", any(Map.class), any(Map.class)).thenReturn(
                ProcessResult.RULE_SUCCESS);
        PowerMockito.suppress(MemberMatcher.method(RuleProcessor.class, "getMapping", Map.class,
                Map.class));
        Map<String, Object> mapping = new HashMap<String, Object>() {
            {
                put("Name", "Admin");
            }
        };
        when(ruleProcess.getMapping(any(Map.class), any(Map.class))).thenReturn(mapping);
        String json = " {\"rules\":[" + "{\"Name\":\"user\", \"Id\":1},"
                + "{\"Name\":\"Admin\", \"Id\":2}]} ";
        Whitebox.invokeMethod(ruleProcess, "process", json);
        verify(ruleProcess, times(3)).getMapping(any(Map.class), any(Map.class));
    }
}
