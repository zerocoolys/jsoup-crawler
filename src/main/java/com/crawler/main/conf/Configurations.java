package com.crawler.main.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.HashedMap;

public class Configurations {

	private static Configurations instance;

	private final String SEP = ".";
	private Properties pro = new Properties();

	private Configurations() {
		init();
	}

	private void init() {

		try {
			pro.load(new FileInputStream("conf/url.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Configurations getInstance() {
		if (instance == null)
			instance = new Configurations();
		return instance;
	}

	public Object get(String site, String field) {
		return pro.get(site + SEP + field);
	}

	public Object get(String key) {
		return pro.get(key);
	}

	public Map<String, Object> getValues(String key) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Iterator<Object> keys = pro.keySet().iterator();
		while (keys.hasNext()) {
			Object proKey = keys.next();

			if (proKey.toString().startsWith(key)) {
				returnMap.put(proKey.toString(), pro.get(proKey));

			}
		}

		return returnMap;
	}
}
