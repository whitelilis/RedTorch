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

	public Blind(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
	    this.plans = new HashMap<>();
	    this.random = new java.util.Random();
		Map<String, String> varMap = strategySetting.getVarMap();
		for(StrategySetting.ContractSetting contractSetting : strategySetting.getContracts()){
			float r = random.nextFloat();
			String rtSymbol = contractSetting.getRtSymbol();
			if(r < 0.5) { // buy long in
				this.plans.put(rtSymbol, new Plan(Float.MIN_VALUE, Float.MIN_VALUE, 0.005f, 0.02f));
			}else{ // sell short out
				this.plans.put(rtSymbol, new Plan(Float.MAX_VALUE, Float.MAX_VALUE, 0.005f, 0.02f));
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
				"type: %s, price: %f, longIn: %f, shortIn: %f,  longOut: %f, shortOut: %f, time : %s",
				"{} : [ {}, {} ] {} [ {}, {} ] @ {}",
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
