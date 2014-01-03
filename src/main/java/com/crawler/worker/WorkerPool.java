package com.crawler.worker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.settings.Settings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.crawler.main.conf.Configurations;
import com.crawler.root.Pools;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class WorkerPool {

	private static ConcurrentHashMap<String, Executor> mapholder = new ConcurrentHashMap<String, Executor>();

	private String url;

	private Settings settings;

	public WorkerPool(String url, Settings settings) {
		this.url = url;
		this.settings = settings;
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
				Document doc = Pools.pollDoc();
				
				Map<String,String> maps = settings.getAsMap();
				
				for(String fieldName : maps.keySet()){
					
				}
				
				Elements links = doc.select("a");
				Pools.pushLinks(root, links);
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
