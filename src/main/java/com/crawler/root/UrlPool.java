package com.crawler.root;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        String url = urlpool.poll();
        System.out.println("---- " + url);

        return url;
    }

    public static void pushLinks(String root, Elements elements) {
        for (Element element : elements) {
            if (element.attributes().hasKey("href")) {
                String link = element.attributes().get("href");
                if (!link.startsWith("http://")) {
                    link = root + link;
                }
                UrlPool.putUrl(link);
            }
        }

    }
}
