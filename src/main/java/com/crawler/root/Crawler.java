package com.crawler.root;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by wyyousheng on 13-12-11.
 */
public class Crawler {
    public static void main(String[] args) {
        // TODO 参数设置

        if (!checkParam(args)) {
            return;
        }

        String url = args[0];
        crawl(url);
    }

    private static void crawl(String root) {
        try {
            Document dom = Jsoup.connect(root).get();
            Elements elements = dom.select("a");

            for (Element element : elements) {
                if (element.attributes().hasKey("href")) {
                    String link = element.attributes().get("href");
                    if (!link.startsWith("http://")) {
                        link = root + link;
                    }
                    UrlPool.putUrl(link);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean checkParam(String[] args) {
        return true;
    }
}
