package com.crawler.worker;

import com.crawler.root.Pools;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.*;

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

        @Override
        public void run() {

            while (true) {
                Document doc = Pools.pollDoc();

                if(doc.location().startsWith("http://item")){
                    processDoc(doc);
                }
                Elements links = doc.select("a");
                Pools.pushLinks(root, links);
            }

        }

        private void processDoc(Document doc) {
            Element summaryele = doc.getElementById("summary");

        }
    }


    private final int POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    public void start(String root) {


        ThreadPoolExecutor linkExecutors = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>());

        linkExecutors.prestartAllCoreThreads();

        for (int i = 0; i < 4; i++) {
            linkExecutors.execute(new Work(root));
        }

        ThreadPoolExecutor docExecutors = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>());

        docExecutors.prestartAllCoreThreads();

        for (int i = 0; i < 4; i++) {
            docExecutors.execute(new Work(root));
        }


        mapholder.putIfAbsent(root, linkExecutors);
    }
}
