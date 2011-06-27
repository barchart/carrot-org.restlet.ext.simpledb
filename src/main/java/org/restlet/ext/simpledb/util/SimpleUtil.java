package org.restlet.ext.simpledb.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.UpdateCondition;
import com.carrotgarden.utils.json.JSON;

public class SimpleUtil {

	public static String convertProps(Map<String, Object> props)
			throws Exception {

		if (props == null) {
			return null;
		}

		String json = JSON.intoText(props);

		return json;

	}

	public static <T> T convertProps(Map<String, Object> props, Class<T> klaz)
			throws Exception {

		if (props == null) {
			return null;
		}

		ObjectMapper mapper = JSON.getInstance();

		T value = mapper.convertValue(props, klaz);

		return value;

	}

	public static Map<String, Object> convertInstance(Object instance)
			throws Exception {

		if (instance == null) {
			return null;
		}

		ObjectMapper mapper = JSON.getInstance();

		@SuppressWarnings("unchecked")
		Map<String, Object> props = mapper.convertValue(instance, Map.class);

		return props;

	}

	public static Map<String, Object> convertJson(String json) throws Exception {

		if (json == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> props = JSON.fromText(json, Map.class);

		return props;

	}

	public static List<Item> selectItems(AmazonSimpleDB client, String domain,
			String select) throws Exception {

		String expression = "select * from " + name(domain) + " " + select;

		SelectRequest request = new SelectRequest(expression)
				.withConsistentRead(true);

		SelectResult result = client.select(request);

		List<Item> itemList = result.getItems();

		return itemList;

	}

	public static <T> List<T> findItems(AmazonSimpleDB client, String domain,
			String prefix, Class<T> klaz) throws Exception {

		String select = //
		"where itemName() like " + value(prefix + "%") + " limit 2500";

		List<Item> itemList = selectItems(client, domain, select);

		List<T> objectList = new LinkedList<T>();

		for (Item item : itemList) {
			T object = getObject(item, klaz);
			objectList.add(object);
		}

		return objectList;

	}

	public static String getJson(Item item) throws Exception {

		Map<String, Object> props = SimpleUtil.getProps(item);

		String json = convertProps(props);

		return json;

	}

	public static String getJson(AmazonSimpleDB client, String domain,
			String item) throws Exception {

		Map<String, Object> props = getProps(client, domain, item);

		String json = convertProps(props);

		return json;

	}

	public static <T> T getObject(AmazonSimpleDB client, String domain,
			String item, Class<T> klaz) throws Exception {

		Map<String, Object> props = getProps(client, domain, item);

		T value = convertProps(props, klaz);

		return value;

	}

	public static <T> T getObject(Item item, Class<T> klaz) throws Exception {

		List<Attribute> attributes = item.getAttributes();

		Map<String, Object> props = getProps(attributes);

		T object = convertProps(props, klaz);

		return object;

	}

	public static Map<String, Object> getProps(AmazonSimpleDB client,
			String domain, String item) throws Exception {

		GetAttributesRequest request = new GetAttributesRequest(//
				domain, item).withConsistentRead(true);

		GetAttributesResult result = client.getAttributes(request);

		List<Attribute> attributes = result.getAttributes();

		Map<String, Object> props = getProps(attributes);

		return props;

	}

	public static Map<String, Object> getProps(Item item) throws Exception {

		List<Attribute> attributes = item.getAttributes();

		Map<String, Object> props = getProps(attributes);

		return props;

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getProps(List<Attribute> attributes)
			throws Exception {

		if (attributes.isEmpty()) {
			return null;
		}

		Map<String, Object> props = new HashMap<String, Object>();

		for (Attribute attrib : attributes) {

			String key = attrib.getName();
			String value = attrib.getValue();

			Object stored = props.get(key);

			if (stored == null) {
				props.put(key, value);
				continue;
			}

			if (stored instanceof String) {
				List<String> list = new LinkedList<String>();
				list.add((String) stored);
				list.add(value);
				props.put(key, list);
				continue;
			}

			if (stored instanceof List) {
				List<String> list = (List<String>) stored;
				list.add(value);
				continue;
			}

			throw new Exception("unexpected value type : " + stored);

		}

		return props;

	}

	public static void makeDomain(AmazonSimpleDB client, String domain)
			throws Exception {

		ListDomainsResult result = client.listDomains();

		List<String> nameList = result.getDomainNames();

		if (nameList.contains(domain)) {
			return;
		}

		CreateDomainRequest request = new CreateDomainRequest(domain);

		client.createDomain(request);

	}

	// http://docs.amazonwebservices.com/AmazonSimpleDB/2009-04-15/DeveloperGuide/index.html?QuotingRulesSelect.html
	public static String name(String name) {
		return "`" + name + "`";
	}

	public static void putEntry(String key, String value,
			List<ReplaceableAttribute> attributes) {

		ReplaceableAttribute attrib = new ReplaceableAttribute();

		attrib.setName(key);
		attrib.setValue(value);

		attrib.setReplace(true); // XXX

		attributes.add(attrib);

	}

	public static void putJson(String json, AmazonSimpleDB client,
			String domain, String item) throws Exception {

		Map<String, Object> props = convertJson(json);

		putProps(props, client, domain, item);

	}

	public static void putObject(Object instance, AmazonSimpleDB client,
			String domain, String item) throws Exception {

		Map<String, Object> props = convertInstance(instance);

		putProps(props, client, domain, item);

	}

	public static void putProps(Map<String, Object> props,
			AmazonSimpleDB client, String domain, String item) throws Exception {

		if (props == null) {

			List<Attribute> attributes = null;
			UpdateCondition condition = null;

			DeleteAttributesRequest request = new DeleteAttributesRequest(//
					domain, item, attributes, condition);

			client.deleteAttributes(request);

		} else {

			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();

			putProps(props, attributes);

			UpdateCondition condition = null;

			PutAttributesRequest request = new PutAttributesRequest(//
					domain, item, attributes, condition);

			client.putAttributes(request);

		}

	}

	@SuppressWarnings({ "unchecked" })
	public static void putProps(Map<String, Object> props,
			List<ReplaceableAttribute> attributes) throws Exception {

		for (Map.Entry<String, Object> entry : props.entrySet()) {

			String key = entry.getKey();
			Object stored = entry.getValue();

			if (stored instanceof String) {
				String value = (String) stored;
				putEntry(key, value, attributes);
				continue;
			}

			if (stored instanceof List) {
				List<String> list = (List<String>) stored;
				for (String value : list) {
					putEntry(key, value, attributes);
				}
				continue;
			}

			throw new Exception("unexpected value type : " + stored);

		}

	}

	// http://docs.amazonwebservices.com/AmazonSimpleDB/2009-04-15/DeveloperGuide/index.html?QuotingRulesSelect.html
	public static String value(String value) {
		return "'" + value + "'";
	}

}
