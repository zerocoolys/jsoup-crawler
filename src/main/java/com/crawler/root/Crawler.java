package com.crawler.root;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.crawler.main.conf.Configurations;
import com.crawler.worker.WorkerPool;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class Crawler {

	private static Options options = new Options();

	static {
		Option url = new Option("u", true, "url for target");
		url.setRequired(true);
		options.addOption(url);

		options.addOption(new Option("d", true, "the depth"));
	}

	public static void main(String[] args) {
		// TODO 参数设置
		Settings settings = ImmutableSettings.builder()
				.loadFromSource("conf/url.yml").build();
		CommandLine cmd = checkParam(args);

		if (cmd == null) {
			return;
		}
		// Object keyObj = Configurations.getInstance().get("key");
		String keystr = settings.get("key");
		if (keystr == null)
			return;

		String[] keys = keystr.split(",");

		for (final String key : keys) {

			final String url = settings.get("key." + key);

			final Settings localSettings = settings.getByPrefix(key);
			WorkerPool wp = new WorkerPool(url, localSettings);
			wp.start();

			new Thread(new Runnable() {
				public void run() {
					crawl(url);
				}
			}).start();

		}

	}

	private static void crawl(String root) {
		try {
			Document dom = Jsoup.connect(root).get();
			Elements elements = dom.select("a");

			HashSet<String> links = new HashSet<String>(elements.size());
			for (Element element : elements) {
				if (element.attributes().hasKey("href")) {
					String link = element.attributes().get("href");
					if (!link.startsWith("http://")) {
						link = root + link;
					}
					links.add(link);
				}
			}
			Pools.putUrl(links);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static CommandLine checkParam(String[] args) {
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			return cmd;
		} catch (ParseException e) {
			e.printStackTrace();
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("main", options);
		}
		return null;
	}
}
