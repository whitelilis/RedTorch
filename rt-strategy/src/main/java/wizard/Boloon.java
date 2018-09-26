package wizard;

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
	private HashMap<String, Plan> plans;

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
			this.plans.put(contractSetting.getRtSymbol(), new Plan(longIn, shortIn));
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
	    Plan meta = plans.get(tick.getRtSymbol());
		log.info(
				"%s : [ %.2f, %.2f ] %.2f [ %.2f, %.2f ] @ %s",
				tick.getRtSymbol(),
				meta.longIn, meta.longOut, tick.getLastPrice(),  meta.shortOut, meta.shortIn,
				tick.getDateTime()
		);

		if(meta.lastOP != Plan.Signal.NOOP) {// something is still doing
			// todo: how to track the order?  succeed/failed/partly
			log.info(meta.lastOP + "is still doing; omit this tick");
		} else {
			double price = tick.getLastPrice();
			Plan.Signal signal = meta.doWhat(price);
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
		log.info("call onBar");
		savePosition();
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		log.info("call onXMinBar");
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info(String.format("enter order, get status %s:%s:%d:%f:%s",
				order.getRtSymbol(), order.getDirection(), order.getTotalVolume(),
				order.getPrice(), order.getStatus()));
		if(order.getStatus() == RtConstant.STATUS_CANCELLED){
			Plan plan = plans.get(order.getRtSymbol());
			plan.lastOP = Plan.Signal.NOOP;
		}
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		log.info(String.format("enter trade, get status %s:%s:%d:%f",
				trade.getRtSymbol(), trade.getDirection(),
				trade.getVolume(), trade.getPrice()));
		Plan plan = plans.get(trade.getRtSymbol());
		plan.lastOP = Plan.Signal.NOOP;
	}

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {
		
	}
}
