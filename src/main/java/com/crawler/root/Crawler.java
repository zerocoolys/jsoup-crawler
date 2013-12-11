package com.crawler.root;

import com.crawler.worker.WorkerPool;
import org.apache.commons.cli.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

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

        CommandLine cmd = checkParam(args);

        if (cmd == null) {
            return;
        }

        String url = cmd.getOptionValue("u");

        WorkerPool wp = new WorkerPool();
        wp.start(url);

        crawl(url);

    }


    private static void crawl(String root) {
        try {
            Document dom = Jsoup.connect(root).get();
            Elements elements = dom.select("a");

            UrlPool.pushLinks(root, elements);
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
