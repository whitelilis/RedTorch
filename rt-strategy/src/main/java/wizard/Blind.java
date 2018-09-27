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
import java.util.Random;

/**
 * @author whitelilis@gmail.com
 */
public class Blind extends StrategyAbstract{
	private static final Logger log = LoggerFactory.getLogger(Blind.class);
	private HashMap<String, Plan> plans;
	private Random random ;

	public static final float lossRate = 0.005f;
	public static final float profitRate = 0.02f;

	public Blind(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
	    this.plans = new HashMap<>();
	    this.random = new java.util.Random();
		Map<String, String> varMap = strategySetting.getVarMap();
		for(StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()){
			String rtSymbol = contractSetting.getRtSymbol();
			String planKey = Plan.saveKey(rtSymbol);
			if(varMap.containsKey(planKey)) {// already traded before
				this.plans.put(rtSymbol, Plan.fromJson(varMap.get(planKey)));
			} else {
				float r = random.nextFloat();
				if (r < 0.5) { // buy long in
					this.plans.put(rtSymbol, Plan.buyPlan(lossRate, profitRate));
				} else { // sell short out
					this.plans.put(rtSymbol, Plan.sellPlan(lossRate, profitRate));
				}
			}
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
	    Plan plan = plans.get(tick.getRtSymbol());
		log.info(String.format(
				"%s : [ %.2f, %.2f ] %.2f [ %.2f, %.2f ] @ %s",
				tick.getRtSymbol(),
				plan.longIn, plan.longOut, tick.getLastPrice(),  plan.shortOut, plan.shortIn,
				tick.getDateTime()
		));

		if(plan.lastOP != Plan.Signal.NOOP) {// something is still doing
			// todo: how to track the order?  succeed/failed/partly
			log.info("{} {}  is still doing; omit this tick", tick.getRtSymbol(), plan.lastOP);
		} else {
			double price = tick.getLastPrice();
			Plan.Signal signal = plan.doWhat(price);
			plan.updateByPrice(price, signal);
			plan.lastOP = signal;
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
					sellShort(tick.getRtSymbol(), 1, tick.getLowerLimit(), tick.getGatewayID());
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
		// todo: save current plans
		log.info("call onXMinBar");
		String symbol = bar.getRtSymbol();
		Plan plan = plans.get(symbol);
		String key = Plan.saveKey(symbol);
		strategySetting.getVarMap().put(key, plan.toJson());
		saveStrategySetting();
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info(String.format("enter order, get status %s:%s:%d:%f:%s",
				order.getRtSymbol(), order.getDirection(), order.getTotalVolume(),
				order.getPrice(), order.getStatus()));
		Plan plan = plans.get(order.getRtSymbol());

		switch (order.getStatus()){
			case RtConstant.STATUS_CANCELLED:
				plan.lastOP = Plan.Signal.NOOP;
				break;
			case RtConstant.STATUS_ALLTRADED:
				switch (plan.lastOP){
					case SHORT_OUT:
						log.warn("sell -> buy");
						plans.put(order.getRtSymbol(), Plan.buyPlan(lossRate, profitRate));
						break;
					case LONG_OUT:
						log.warn("buy -> sell");
						plans.put(order.getRtSymbol(), Plan.sellPlan(lossRate, profitRate));
						break;
					default:
						// todo: buy/sell or other complete
						log.warn("{} complete, ignored", plan.lastOP);
				}
				break;
			case RtConstant.STATUS_PARTTRADED:
				// todo: partly traded
				log.error("TODO TODO");
				break;
			default:
				// todo: what happened?
				log.error("MMP MMP");
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
