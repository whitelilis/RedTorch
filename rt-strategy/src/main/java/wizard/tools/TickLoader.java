package wizard.tools;

import org.bson.Document;
import org.joda.time.DateTime;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/5
 */
public class TickLoader {
    public static void main(String[] args) throws Exception {
        // todo: read config from rt-core.properties
        String dbName = "redtorch_j_tick_db";
        String aim = "rb1710.SHFE";
        String postfix = "SHFE";
        String toLoadFile = "/Users/liuzhe/Downloads/quan/201704/rb1710.csv";

        MongoDBClient mdDBClient = new MongoDBClient("127.0.0.1", 27017, "", "", dbName);
        BufferedReader br = new BufferedReader(new FileReader(new File(toLoadFile)));
        br.readLine(); // skip first line: header
        for(String line = br.readLine(); line != null; line = br.readLine()){
            String[] parts = line.split(",", -1);
            Tick t = new Tick();
            t.setGatewayID("backTest");
            t.setSymbol(parts[1]);
            t.setRtSymbol(parts[1] + "." + postfix);
            t.setActionTime(parts[2]);
            t.setDateTime(new DateTime(parts[2].substring(0, 10)));
            t.setLastPrice(Double.parseDouble(parts[3]));
            //t.setLastVolume(Integer.parseInt(parts[6]));
            t.setAskPrice1(Double.parseDouble(parts[12]));
            t.setBidPrice1(Double.parseDouble(parts[13]));
            t.setAskVolume1(Integer.parseInt(parts[14]));
            t.setBidVolume1(Integer.parseInt(parts[15]));

            // todo: set lowest/uppest
            t.setLowerLimit(0.0);
            t.setUpperLimit(88888888.0);

            Document d = MongoDBUtil.beanToDocument(t);
            mdDBClient.insert(dbName, aim, d);
        }
    }
}
