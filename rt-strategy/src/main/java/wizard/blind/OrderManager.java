package wizard.blind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Trade;

import java.util.TreeMap;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 * Maybe I need a FSM !!!!
 * @author: whitelilis@gmail.com on 18/10/6
 */
public class OrderManager {
	private static final Logger log = LoggerFactory.getLogger(Blind.class);
    public TreeMap<String, Order> orders = new TreeMap<>();
    public boolean idle(){
        return orders.isEmpty();
    }


	public void handleTrade(Blind blind, Trade trade){
		Plan plan = blind.plans.get(trade.getRtSymbol());
		if (trade.getOffset().equals(RtConstant.OFFSET_CLOSE) ||
				trade.getOffset().equals(RtConstant.OFFSET_CLOSETODAY) ||
				trade.getOffset().equals(RtConstant.OFFSET_CLOSEYESTERDAY)) {
			if (trade.getDirection().equals(RtConstant.DIRECTION_LONG)) { // close with long, short over
				double profit = 0;
				double lastPrice = trade.getPrice();
				for(double one : plan.tradePrices){
					profit += one - lastPrice;
				}
				log.warn("OVER {} {} ->@ {} ==> {}", plan.direction, plan.tradePrices.toArray(), trade.getPrice(), profit);
				log.warn("sell -> buy");
				blind.plans.clear();
				blind.plans.put(trade.getRtSymbol(), Plan.buyPlan(blind.lossRate, blind.profitRate));
			} else if (trade.getDirection().equals(RtConstant.DIRECTION_SHORT)) { // close with short, long over
				double profit = 0;
				double lastPrice = trade.getPrice();
				for(double one : plan.tradePrices){
					profit += lastPrice - one;
				}
				log.warn("OVER {} {} ->@ {} ==> {}", plan.direction, plan.tradePrices.toArray(), trade.getPrice(), profit);
				log.warn("buy -> sell");
				blind.plans.clear();
				blind.plans.put(trade.getRtSymbol(), Plan.sellPlan(blind.lossRate, blind.profitRate));
			} else {
				log.error("what kind last order complete {} {} {}",
						trade.getOrderID(), trade.getDirection(), trade.getOffset());
			}
		}else if(trade.getOffset().equals(RtConstant.OFFSET_OPEN)){
			plan.tradePrices.add(trade.getPrice());
			plan.updateVolume(trade);

			if(trade.getDirection().equals(RtConstant.DIRECTION_LONG)){
				double bigger = trade.getPrice() * (1 - plan.lossRate);
				log.info(String.format("update longOut %.2f ==> %.2f @ %s", plan.outPrice, bigger, trade.getTradeTime()));
				plan.outPrice = bigger;
				plan.inPrice = trade.getPrice() * (1 + plan.profitRate);
			}else if(trade.getDirection().equals(RtConstant.DIRECTION_SHORT)){
				double smaller = trade.getPrice() * (1 + plan.lossRate);
				log.info(String.format("update shortOut %.2f ==> %.2f @ %s", plan.outPrice, smaller, trade.getTradeTime()));
				plan.outPrice = smaller;
				plan.inPrice = trade.getPrice() * (1 - plan.profitRate);
			}else {
				log.error("open complete, but direction {} not considered.", trade.getDirection());
			}
		}else{
			log.error("666666666666666666   ");
		}
	}

	public void handleOrder(Blind blind, Order order){
		String usingId = order.getRtOrderID();
		if(order.getStatus().equals(RtConstant.STATUS_CANCELLED)) {
			// todo: order need retry
			orders.remove(usingId);
		}else if(order.getStatus().equals(RtConstant.STATUS_ALLTRADED)) {
			orders.remove(usingId);
		}else{
			log.error("order {} {}, what to do ?", order.getOrderID(), order.getStatus());
		}
	}

	public String getOrders(){
		return orders.toString();
	}

	public void addOrder(String orderId, Order order){
		orders.put(orderId, order);
	}
}
