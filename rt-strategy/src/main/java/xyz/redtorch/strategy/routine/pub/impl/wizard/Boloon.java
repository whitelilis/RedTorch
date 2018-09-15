package xyz.redtorch.strategy.routine.pub.impl.wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.StopOrder;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

import java.util.HashMap;
import java.util.Map;

/**
 * @author whitelilis@gmail.com
 */
public class Boloon extends StrategyAbstract{
	private static final Logger log = LoggerFactory.getLogger(Boloon.class);
	private HashMap<String, Meta> plans;

	public Boloon(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
	    this.plans = new HashMap<>();
		Map<String, String> varMap = strategySetting.getVarMap();
		for(StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()){
			String symbol = contractSetting.getAlias();
			float longIn = Float.valueOf(varMap.get(symbol + "LongIn"));
			float shortIn = Float.valueOf(strategySetting.getVarMap().get(symbol + "ShortIn"));
			this.plans.put(contractSetting.getRtSymbol(), new Meta(longIn, shortIn));
		}
	}

	@Override
	public void onStartTrading() throws Exception {
		
	}

	@Override
	public void onStopTrading(boolean isException) throws Exception {
		
	}

	@Override
	public void onTick(Tick tick) throws Exception {
	    Meta meta = plans.get(tick.getRtSymbol());
		log.info(String.format(
				"type: %s, price: %f, longIn: %f, shortIn: %f,  longOut: %f, shortOut: %f, time : %s",
				tick.getRtSymbol(), tick.getLastPrice(),
				meta.longIn, meta.shortIn, meta.longOut, meta.shortOut,
				tick.getDateTime()
		));

		if(meta.lastOP != Meta.Signal.NOOP) {// something is still doing
			// todo: how to track the order?  succeed/failed/partly
			log.info(meta.lastOP + "is still doing; omit this tick");
		} else {
			double price = tick.getLastPrice();
			Meta.Signal signal = meta.doWhat(price);
			meta.updateByPrice(price, signal);
			meta.lastOP = signal;
			switch (signal) {
				case NOOP:
					return;
				case IMPOSSIABLE:
					log.info("WTF");
					log.info(tick.toString());
					break;
				case LONG_IN:
					buy(tick.getRtSymbol(), 1, tick.getUpperLimit(), tick.getGatewayID());
					break;
				case SHORT_IN:
					sellShort(tick.getRtSymbol(), 1, tick.getUpperLimit(), tick.getGatewayID());
					break;
				case LONG_OUT:
					sellByPosition(tick.getRtSymbol(), tick.getLowerLimit());
					break;
				case SHORT_OUT:
					buyToCoverTdByPosition(tick.getRtSymbol(), tick.getUpperLimit());
					break;
			}
		}
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		// todo: update somethis
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		savePosition();
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info(String.format("enter order, get status %s:%s:%d:%f:%s",
				order.getRtSymbol(), order.getDirection(), order.getTotalVolume(),
				order.getPrice(), order.getStatus()));
		if(order.getStatus() == RtConstant.STATUS_CANCELLED){
			Meta meta = plans.get(order.getRtSymbol());
			meta.lastOP = Meta.Signal.NOOP;
		}
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		log.info(String.format("enter trade, get status %s:%s:%d:%f",
				trade.getRtSymbol(), trade.getDirection(),
				trade.getVolume(), trade.getPrice()));
		Meta meta = plans.get(trade.getRtSymbol());
		meta.lastOP = Meta.Signal.NOOP;
	}

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {
		
	}
}
