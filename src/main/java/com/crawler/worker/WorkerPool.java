package com.crawler.worker;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import com.crawler.root.Pools;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class WorkerPool {

	private static ConcurrentHashMap<String, Executor> mapholder = new ConcurrentHashMap<String, Executor>();

	private String url;

	private Settings settings;

	private Client client;

	private final int BUCKET_COUNT = 200;

	private String key;

	public WorkerPool(String key, String url, Settings settings) {
		this.key = key;
		this.url = url;
		this.settings = settings;

		initClient();
	}

	private void initClient() {
		client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"10.28.174.196", 9300));
	}

	class Work implements Runnable {
		private String url;

		public String getRoot() {
			return url;
		}

		public void setRoot(String root) {
			this.url = root;
		}

		public Work(String url) {
			this.url = url;
		}

		public void run() {
			while (true) {
				String url = Pools.pollUrl();
				try {
					Document doc = Jsoup.connect(url).get();
					Pools.putDoc(doc);
				} catch (IOException e) {
					System.err.println("eeee " + url);
				}

			}
		}
	}

	class DocWorker implements Runnable {

		private String root;

		public DocWorker(String root) {
			this.root = root;
		}

		public void run() {

			while (true) {
				try {
					Document doc = Pools.pollDoc();

					Map<String, String> maps = settings.getAsMap();

					IndexRequestBuilder requestBuilder = client.prepareIndex();
					requestBuilder.setIndex("sites").setType(key);

					Map<String, Object> source = new HashMap<String, Object>(
							maps.size());
					XPath xpath = XPathFactory.newInstance().newXPath();
					InputSource htmlis = new InputSource(new StringReader(
							doc.html()));
					for (String fieldName : maps.keySet()) {
						String value = xpath.evaluate(settings.get(fieldName),
								htmlis);
						System.out.println(value);

						source.put(fieldName, value);
					}
					requestBuilder.setSource(source);

					pushIndex(requestBuilder.request());

					Elements links = doc.select("a");
					Pools.pushLinks(root, links);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
			}
		}

		private BulkRequestBuilder bulkRequestBuilder = new BulkRequestBuilder(
				client);

		private void pushIndex(IndexRequest request) {
			bulkRequestBuilder.add(request);
			if (bulkRequestBuilder.numberOfActions() == 5) {
				try {
					bulkRequestBuilder.execute();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					bulkRequestBuilder.request().requests().clear();
				}

			}
		}
	}

	private final int POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

	public void start() {

		ThreadPoolExecutor linkExecutors = new ThreadPoolExecutor(POOL_SIZE,
				POOL_SIZE, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>());

		linkExecutors.prestartAllCoreThreads();

		for (int i = 0; i < 4; i++) {
			linkExecutors.execute(new Work(url));
		}

		ThreadPoolExecutor docExecutors = new ThreadPoolExecutor(POOL_SIZE,
				POOL_SIZE, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>());

		docExecutors.prestartAllCoreThreads();

		for (int i = 0; i < 4; i++) {
			docExecutors.execute(new DocWorker(url));
		}

		mapholder.putIfAbsent(url, linkExecutors);
	}
}
