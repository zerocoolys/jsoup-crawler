package com.crawler.root;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class UrlPool {

    private static BlockingQueue<String> urlpool = new LinkedBlockingQueue<String>(500);

    public static void putUrl(String url) {
        try {
            System.out.println("++++ " + url);
            urlpool.put(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String pollUrl() {
        try {
            String url = urlpool.take();
            System.out.println("---- " + url);

            return url;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
