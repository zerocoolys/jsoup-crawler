package com.crawler.root;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class Pools {

    private static BlockingQueue<String> urlpool = new LinkedBlockingQueue<String>(5000);

    public static void putUrl(HashSet<String> urls) {
        try {
            for (String url : urls) {
                System.out.println("++++ " + url);
                urlpool.put(url);
            }
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

    public static void pushLinks(String root, Elements elements) {
        HashSet<String> links = new HashSet<>(elements.size());
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


    }
}
