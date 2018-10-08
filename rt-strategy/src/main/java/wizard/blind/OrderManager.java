package wizard.blind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Order;

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

	public void handleOrder(Blind blind, Order order){
		Plan plan = blind.plans.get(order.getRtSymbol());
		if(order.getStatus().equals(RtConstant.STATUS_CANCELLED)) {
			// todo: order need retry
			orders.remove(order.getOrderID());
		}else if(order.getStatus().equals(RtConstant.STATUS_ALLTRADED)) {
			orders.remove(order.getOrderID());
			if (orders.isEmpty()) {// the last order
				if (order.getOffset().equals(RtConstant.OFFSET_CLOSE) ||
						order.getOffset().equals(RtConstant.OFFSET_CLOSETODAY) ||
						order.getOffset().equals(RtConstant.OFFSET_CLOSEYESTERDAY)) {
					if (order.getDirection().equals(RtConstant.DIRECTION_LONG)) {
						log.warn("OVER {} ->@ {}", plan.tradePrices.toArray(), order.getPrice());
						log.warn("sell -> buy");
						blind.plans.clear();
						blind.plans.put(order.getRtSymbol(), Plan.buyPlan(Blind.lossRate, Blind.profitRate));
					} else if (order.getDirection().equals(RtConstant.DIRECTION_SHORT)) {
						log.warn("OVER {} ->@ {}", plan.tradePrices.toArray(), order.getPrice());
						log.warn("buy -> sell");
						blind.plans.clear();
						blind.plans.put(order.getRtSymbol(), Plan.sellPlan(Blind.lossRate, Blind.profitRate));
					} else {
						log.error("what kind last order complete {} {} {}",
								order.getOrderID(), order.getDirection(), order.getOffset());
					}
				}else if(order.getOffset().equals(RtConstant.OFFSET_OPEN)){
					if(order.getDirection().equals(RtConstant.DIRECTION_LONG)){
						double bigger = order.getPrice() * (1 - plan.lossRate);
						log.info(String.format("update longOut %.2f ==> %.2f @ %s", plan.outPrice, bigger, order.getUpdateTime()));
						plan.outPrice = bigger;
						plan.inPrice = order.getPrice() * (1 + plan.profitRate);
					}else if(order.getDirection().equals(RtConstant.DIRECTION_SHORT)){
						double smaller = order.getPrice() * (1 + plan.lossRate);
						log.info(String.format("update shortOut %.2f ==> %.2f @ %s", plan.outPrice, smaller, order.getUpdateTime()));
						plan.outPrice = smaller;
						plan.inPrice = order.getPrice() * (1 - plan.profitRate);
					}else {
						log.error("open complete, but direction {} not considered.", order.getDirection());
					}
				}else{
					log.error("666666666666666666   ");
				}
			} else {// some order still doing
				log.error("some order still doing {}", getOrders());
			}
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
