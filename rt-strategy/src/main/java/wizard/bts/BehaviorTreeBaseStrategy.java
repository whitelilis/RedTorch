package wizard.bts;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.StopOrder;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/12
 *
 * Base strategy besed on BehaviorTree, handler every event happened or may happened.
 */
public class BehaviorTreeBaseStrategy extends StrategyAbstract {
    /**
     * 必须使用有参构造方法
     *
     * @param zeusEngineService
     * @param strategySetting
     */
    public BehaviorTreeBaseStrategy(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
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

    }

    @Override
    public void onStartTrading() throws Exception {

    }

    @Override
    public void onStopTrading(boolean isException) throws Exception {

    }

    @Override
    public void onTick(Tick tick) throws Exception {

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
