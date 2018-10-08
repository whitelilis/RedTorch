package wizard.saver;

import org.bson.Document;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.StopOrder;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/8
 */
public class Saver extends StrategyAbstract {

    public String dbName = "redtorch_j_tick_db";
    public MongoDBClient mdDBClient;
    /**
     * 必须使用有参构造方法
     *
     * @param zeusEngineService
     * @param strategySetting
     */
    public Saver(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
        super(zeusEngineService, strategySetting);
    }

    @Override
    public void onBar(Bar bar) throws Exception {

    }

    @Override
    public void onXMinBar(Bar bar) throws Exception {

    }

    @Override
    public void onInit() throws Exception {
        this.mdDBClient = new MongoDBClient("127.0.0.1", 27017, "", "", this.dbName);
    }

    @Override
    public void onStartTrading() throws Exception {

    }

    @Override
    public void onStopTrading(boolean isException) throws Exception {

    }

    @Override
    public void onTick(Tick tick) throws Exception {
        Document d =   MongoDBUtil.beanToDocument(tick);
        String aim = "rb1710.SHFE";
        mdDBClient.insert(dbName, aim, d);
    }

    @Override
    public void onOrder(Order order) throws Exception {

    }

    @Override
    public void onTrade(Trade trade) throws Exception {

    }

    @Override
    public void onStopOrder(StopOrder StopOrder) throws Exception {

    }
}
