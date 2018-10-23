package wizard.tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/24
 */
public class Alert {
    public static String urlPrefix = "http://miaotixing.com/trigger?id=tSqDGCC&text=";
    public static void alert(String text) throws IOException {
        URLConnection con = new URL(urlPrefix + URLEncoder.encode(text, "utf8")).openConnection();
        con.connect();
        con.getContentLength();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        alert("order doing more than " + 30 + " ticks");
    }
}
