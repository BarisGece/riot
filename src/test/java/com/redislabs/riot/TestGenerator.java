package com.redislabs.riot;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.redislabs.lettusearch.search.Schema;
import com.redislabs.lettusearch.search.SearchResults;
import com.redislabs.lettusearch.search.field.NumericField;
import com.redislabs.lettusearch.search.field.PhoneticMatcher;
import com.redislabs.lettusearch.search.field.TagField;
import com.redislabs.lettusearch.search.field.TextField;
import com.redislabs.riot.generator.GeneratorReader;

import io.lettuce.core.Range;
import io.lettuce.core.StreamMessage;

public class TestGenerator extends BaseTest {

	@Test
	public void testGenSimple() throws Exception {
		runFile("gen-simple");
		List<String> keys = commands().keys("test:*");
		Assertions.assertEquals(10000, keys.size());
		Map<String, String> simple123 = commands().hgetall("test:123");
		Assertions.assertTrue(simple123.containsKey(GeneratorReader.FIELD_PARTITION));
		Assertions.assertEquals("3", simple123.get(GeneratorReader.FIELD_PARTITIONS));
		Assertions.assertEquals(100, simple123.get("field1").length());
		Assertions.assertEquals(1000, simple123.get("field2").length());
	}

	@Test
	public void testGenFakerHash() throws Exception {
		runFile("gen-faker-hash");
		List<String> keys = commands().keys("person:*");
		Assertions.assertEquals(100, keys.size());
		Map<String, String> person = commands().hgetall(keys.get(0));
		Assertions.assertTrue(person.containsKey("firstName"));
		Assertions.assertTrue(person.containsKey("lastName"));
		Assertions.assertTrue(person.containsKey("address"));
	}

	@Test
	public void testGenFakerScriptProcessorHash() throws Exception {
		runFile("gen-faker-script-processor-hash");
		List<String> keys = commands().keys("person:*");
		Assertions.assertEquals(100, keys.size());
		Map<String, String> person = commands().hgetall(keys.get(0));
		Assertions.assertTrue(person.containsKey("firstName"));
		Assertions.assertTrue(person.containsKey("lastName"));
		Assertions.assertTrue(person.containsKey("address"));
		Assertions.assertEquals(person.get("address"), person.get("address").toUpperCase());
	}

	@Test
	public void testGenFakerSet() throws Exception {
		runFile("gen-faker-set");
		Set<String> names = commands().smembers("got:characters");
		Assertions.assertTrue(names.size() > 10);
		Assertions.assertTrue(names.contains("Lysa Meadows"));
	}

	@Test
	public void testGenFakerZset() throws Exception {
		runFile("gen-faker-zset");
		List<String> keys = commands().keys("leases:*");
		Assertions.assertTrue(keys.size() > 100);
		String key = keys.get(0);
		Assertions.assertTrue(commands().zcard(key) > 0);
	}

	@Test
	public void testGenFakerStream() throws Exception {
		runFile("gen-faker-stream");
		List<StreamMessage<String, String>> messages = commands().xrange("teststream:1", Range.unbounded());
		Assertions.assertTrue(messages.size() > 0);
	}

	@Test
	public void testIndexIntrospection() throws Exception {
		String INDEX = "beerIntrospection";
		String FIELD_ID = "id";
		String FIELD_ABV = "abv";
		String FIELD_NAME = "name";
		String FIELD_STYLE = "style";
		String FIELD_OUNCES = "ounces";
		commands().flushall();
		Schema schema = Schema.builder().field(TagField.builder().name(FIELD_ID).sortable(true).build())
				.field(TextField.builder().name(FIELD_NAME).sortable(true).build())
				.field(TextField.builder().name(FIELD_STYLE).matcher(PhoneticMatcher.English).sortable(true).build())
				.field(NumericField.builder().name(FIELD_ABV).sortable(true).build())
				.field(NumericField.builder().name(FIELD_OUNCES).sortable(true).build()).build();
		commands().create(INDEX, schema);
		runFile("gen-faker-index-introspection");
		SearchResults<String, String> results = commands().search(INDEX, "*");
		Assertions.assertEquals(100, results.getCount());
	}

}
