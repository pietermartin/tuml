package org.nakeuml.tinker.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.tuml.concretetest.God;
import org.tuml.embeddedtest.REASON;
import org.tuml.runtime.test.BaseLocalDbTest;

import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;

public class JsonTest extends BaseLocalDbTest {

	@Test
	public void testEmbeddedManiesToJson() throws JsonParseException, JsonMappingException, IOException {
		db.startTransaction();
		God god = new God(true);
		god.addToEmbeddedString("embeddedString1");
		god.addToEmbeddedString("embeddedString2");
		god.addToEmbeddedString("embeddedString3");
		db.stopTransaction(Conclusion.SUCCESS);
		Assert.assertEquals(4, countVertices());
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(god.toJson());
		@SuppressWarnings("unchecked")
		
		Map<String,Object> jsonObject = mapper.readValue(god.toJson(), Map.class);
		Assert.assertFalse(jsonObject.isEmpty());
		Assert.assertSame(ArrayList.class, jsonObject.get("embeddedString").getClass());
		boolean foundembeddedString1 = false;
		boolean foundembeddedString2 = false;
		boolean foundembeddedString3 = false;
		List<String> embeddeds = (List<String>)jsonObject.get("embeddedString");
		for (String s : embeddeds) {
			if (s.equals("embeddedString1")) {
				foundembeddedString1 = true;
			}
			if (s.equals("embeddedString2")) {
				foundembeddedString2 = true;
			}
			if (s.equals("embeddedString3")) {
				foundembeddedString3 = true;
			}
		}
		Assert.assertTrue(foundembeddedString1 && foundembeddedString2 && foundembeddedString3);
	}
	
	@Test
	public void testEmbeddedManiesFromJson() throws JsonParseException, JsonMappingException, IOException {
		db.startTransaction();
		God god = new God(true);
		god.addToEmbeddedString("embeddedString1");
		god.addToEmbeddedString("embeddedString2");
		god.addToEmbeddedString("embeddedString3");
		god.addToEmbeddedInteger(1);
		god.addToEmbeddedInteger(2);
		god.addToEmbeddedInteger(3);
		db.stopTransaction(Conclusion.SUCCESS);
		Assert.assertEquals(7, countVertices());
		
		String json = god.toJson();
		God godtest = new God(true);
		godtest.fromJson(json);
		Assert.assertEquals(god.getEmbeddedString().size(), godtest.getEmbeddedString().size());
		Assert.assertEquals(god.getEmbeddedInteger().size(), godtest.getEmbeddedInteger().size());
		boolean foundembeddedString1 = false;
		boolean foundembeddedString2 = false;
		boolean foundembeddedString3 = false;
		for (String s : godtest.getEmbeddedString()) {
			if (s.equals("embeddedString1")) {
				foundembeddedString1 = true;
			}
			if (s.equals("embeddedString2")) {
				foundembeddedString2 = true;
			}
			if (s.equals("embeddedString3")) {
				foundembeddedString3 = true;
			}
		}
		Assert.assertTrue(foundembeddedString1 && foundembeddedString2 && foundembeddedString3);
		boolean foundembeddedInteger1 = false;
		boolean foundembeddedInteger2 = false;
		boolean foundembeddedInteger3 = false;
		for (Integer i : godtest.getEmbeddedInteger()) {
			if (i.equals(1)) {
				foundembeddedInteger1 = true;
			}
			if (i.equals(2)) {
				foundembeddedInteger2 = true;
			}
			if (i.equals(3)) {
				foundembeddedInteger3 = true;
			}
		}
		Assert.assertTrue(foundembeddedInteger1 && foundembeddedInteger2 && foundembeddedInteger3);

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJsonToFromWithNulls() throws JsonParseException, JsonMappingException, IOException {
		db.startTransaction();
		God g1 = new God(true);
		g1.setName("g1");
		g1.setReason(REASON.BAD);
		God g2 = new God(true);
		g2.setName("g2");
		g2.setReason(null);
		db.stopTransaction(Conclusion.SUCCESS);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> jsonMap = objectMapper.readValue(g1.toJson(), Map.class);
		Assert.assertEquals("BAD", jsonMap.get("reason"));

		jsonMap = objectMapper.readValue(g2.toJson(), Map.class);
		Assert.assertEquals("null", jsonMap.get("reason"));
		
		db.startTransaction();
		God god1FromJson = new God(true);
		god1FromJson.fromJson(g1.toJson());
		God god2FromJson = new God(true);
		god2FromJson.fromJson(g2.toJson());
		db.stopTransaction(Conclusion.SUCCESS);
		
		Assert.assertEquals(REASON.BAD, god1FromJson.getReason());
		Assert.assertEquals("g1", god1FromJson.getName());
		Assert.assertNull(god2FromJson.getReason());
		Assert.assertEquals("g2", god2FromJson.getName());
		
	}
}