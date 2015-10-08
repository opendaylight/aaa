package org.opendaylight.aaa.idpmapping;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class RuleProcessorTest {

    @Mock private RuleProcessor ruleProcess;

    @Test
    public void testJoin() {
        List<Object> list = new ArrayList<Object>();
        list.add("str1");
        list.add("str2");
        list.add("str3");
        assertEquals("str1/str2/str3", RuleProcessor.join(list, "/"));
    }

    @Test
    public void testSubstituteVariables() {
        Map<String, Object> namespace = new HashMap<String, Object>() {{
            put("foo1", new HashMap<String, String>(){{put("0", "1"); }});
        }};
        String str = "$foo1[0]";
        assertNotNull(ruleProcess.substituteVariables(str, namespace));
    }

    @Test
    public void testGetMapping() {
        Map<String, Object> namespace = new HashMap<String, Object>() {{
            put("foo1", new HashMap<String, String>(){{put("0", "1"); }});
        }};
        final Map<String, Object> item = new HashMap<String, Object>(){{put("str", "val"); }};
        Map<String, Object> rules = new HashMap<String, Object>() {{
            put("mapping", item);
            put("mapping_name", "mapping");
        }};
        assertNotNull(ruleProcess.getMapping(namespace, rules));
    }

    @Test
    public void testProcess() {
        String json =  " {\"rules\":[" +
                "{\"Name\":\"user\", \"Id\":1},"+
                "{\"Name\":\"Admin\", \"Id\":2}]} ";
        assertNotNull(ruleProcess.process(json));
    }

}
