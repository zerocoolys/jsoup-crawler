package com.crawler.worker;

import com.crawler.root.UrlPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class WorkerPool {

    private static ConcurrentHashMap<String, Executor> mapholder = new ConcurrentHashMap<String, Executor>();

    class Work implements Runnable {
        private String root;

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public Work(String root) {
            this.root = root;
        }

        @Override
        public void run() {
            while (true) {
                String url = UrlPool.pollUrl();
                if (url == null) {
                    try {
                        Thread.sleep(500);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements links = doc.select("a");

                    UrlPool.pushLinks(root, links);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void start(String root) {

        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        for (int i = 0; i < 4; i++) {
            executor.execute(new Work(root));
        }

        mapholder.putIfAbsent(root, executor);
    }
}
