package wizard.blind;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

/**
 * @author whitelilis@gmail.com
 */
public class Blind extends StrategyAbstract{
	private static final Logger log = LoggerFactory.getLogger(Blind.class);
	public HashMap<String, Plan> plans;
	private Random random ;

	public float lossRate;
	public float profitRate;

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
			if(false && varMap.containsKey(planKey)) {// already traded before
				this.plans.put(rtSymbol, Plan.fromJson(varMap.get(planKey)));
			} else {
				float r = Float.parseFloat(varMap.getOrDefault("fakeRand",  "" + random.nextFloat()));
				lossRate = Float.parseFloat(varMap.getOrDefault("lossRate", "0.007f"));
				float times = Float.parseFloat(varMap.getOrDefault("proTimes", "4F"));
				profitRate = lossRate * times;

				System.err.println("random get =========== " + r);
				System.err.println("loss : " + lossRate + ", profit : " + profitRate);

				// force sell
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
		log.error("STOP TRADING");
	}

	@Override
	public void onTick(Tick tick) throws Exception {
	    Plan plan = plans.get(tick.getRtSymbol());
		log.debug(String.format(
				"%s : %s  %.2f [ %.2f, %.2f ] @ %s",
				tick.getRtSymbol(), plan.direction,
				tick.getLastPrice(),  plan.inPrice, plan.outPrice,
				tick.getDateTime()
		));

		if(! plan.orderManager.idle()) {// something is still doing
			log.info("{} {}  is still doing; omit this tick", tick.getRtSymbol(), plan.orderManager.getOrders());
		} else {
			double price = tick.getLastPrice();
			Plan.Signal signal = plan.doWhat(price);
			String today = tick.getDateTime().toString(RtConstant.D_FORMAT);
			String orderId = null;

			// todo : ugly start
			double	longInPrice = tick.getUpperLimit();
			double	shortInPrice = tick.getLowPrice();

			switch (signal) {
				case LONG_IN:
					log.warn("will long in on {} @ {}", tick.getLastPrice(), tick.getActionTime());
					orderId = buy(tick.getRtSymbol(), 1, longInPrice, tick.getGatewayID());
					plan.orderManager.addOrder(orderId, null);
					break;
				case SHORT_IN:
					log.warn("will short in on {} @ {}", tick.getLastPrice(), tick.getActionTime());
					orderId = sellShort(tick.getRtSymbol(), 1, shortInPrice, tick.getGatewayID());
					plan.orderManager.addOrder(orderId, null);
					break;
				// todo: only shangHai consider today/yestoday ??
				case LONG_OUT:
					log.warn("will long out on {} @ {}", tick.getLastPrice(), tick.getActionTime());
					if(today.equals(plan.lastInDate)){
						if(plan.todayVolume > 0) {
							log.warn("will long out today {}", plan.todayVolume);
							orderId = sellTd(tick.getRtSymbol(), plan.todayVolume, shortInPrice, tick.getGatewayID());
							plan.orderManager.addOrder(orderId, null);
						}else{
							// skip today
						}
						if(plan.yesterdayVolume > 0) {
							log.warn("will long out yestoday {}", plan.yesterdayVolume);
							orderId = sellYd(tick.getRtSymbol(), plan.yesterdayVolume, shortInPrice, tick.getGatewayID());
							plan.orderManager.addOrder(orderId, null);
						}else{
							// no one ??? long out what???
						}
					}else{
						log.warn("will long out all yestoday {}", plan.todayVolume + plan.yesterdayVolume);
						orderId = sellYd(tick.getRtSymbol(), plan.yesterdayVolume + plan.todayVolume, shortInPrice, tick.getGatewayID());
						plan.orderManager.addOrder(orderId, null);
					}
					break;
				case SHORT_OUT:
					log.warn("will short out on {} @ {}", tick.getLastPrice(), tick.getActionTime());
					if(today.equals(plan.lastInDate)){
						if(plan.todayVolume > 0) {
							log.warn("will short out today {}", plan.todayVolume);
							orderId = buyToCoverTd(tick.getRtSymbol(), plan.todayVolume, longInPrice, tick.getGatewayID());
							plan.orderManager.addOrder(orderId, null);
						}else{
							// skip today
						}
						if(plan.yesterdayVolume > 0) {
							log.warn("will short out yestoday {}", plan.yesterdayVolume);
							orderId = buyToCoverYd(tick.getRtSymbol(), plan.yesterdayVolume, longInPrice, tick.getGatewayID());
							plan.orderManager.addOrder(orderId, null);
						}else{
							// no one ??? long out what???
						}
					}else{
						log.warn("will short out all yestoday {}", plan.todayVolume + plan.yesterdayVolume);
						orderId = buyToCoverYd(tick.getRtSymbol(), plan.yesterdayVolume + plan.todayVolume, longInPrice, tick.getGatewayID());
						plan.orderManager.addOrder(orderId, null);
					}
					break;
				case NOOP:
					break;
				default:
					log.error("get signal {} @ tick.", signal);
					System.exit(0);
			}
		}
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		// todo: update something
		log.debug("call onBar");
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		// todo: save current plans
		log.debug("call onXMinBar");
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info(String.format("enter order, get status %s:%s:%d:%f:%s",
				order.getRtSymbol(), order.getDirection(), order.getTotalVolume(),
				order.getPrice(), order.getStatus()));
		OrderManager orderManager = plans.get(order.getRtSymbol()).orderManager;
		orderManager.handleOrder(this, order);
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		log.info(String.format("enter trade, get status %s:%s:%d:%f",
				trade.getRtSymbol(), trade.getDirection(),
				trade.getVolume(), trade.getPrice()));
		OrderManager orderManager = plans.get(trade.getRtSymbol()).orderManager;
		orderManager.handleTrade(this, trade);
	}

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {

	}
}
