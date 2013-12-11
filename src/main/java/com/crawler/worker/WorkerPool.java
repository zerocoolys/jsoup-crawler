package com.crawler.worker;

import com.crawler.root.UrlPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class WorkerPool {

    private static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    class Work implements Runnable{

        @Override
        public void run() {
            while(true){
                String url = UrlPool.pollUrl();
                if(url == null){
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

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
