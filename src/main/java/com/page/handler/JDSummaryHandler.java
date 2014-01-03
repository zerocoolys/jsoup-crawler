package com.page.handler;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by wyyousheng on 13-12-18.
 */
public class JDSummaryHandler extends AbstractHandler {


    @Override
    public void handler(Element element) {
        Elements lis = element.getElementsByTag("li");
        for(Element ele : lis){

        }
    }
}
