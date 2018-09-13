package xyz.redtorch.strategy.routine.pub.impl.wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.StopOrder;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

import java.util.HashSet;

/**
 * @author sun0x00@gmail.com
 */
public class Boloon extends StrategyAbstract{
	private static final Logger log = LoggerFactory.getLogger(Boloon.class);
	private Meta meta;
	private String symbol;
	private HashSet<String> symbols;

	public Boloon(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
		float longIn = Float.valueOf(strategySetting.getVarMap().get("longIn"));
		float shortIn = Float.valueOf(strategySetting.getVarMap().get("shortIn"));
		this.meta = new Meta(longIn, shortIn);
		for(StrategySetting.ContractSetting i : strategySetting.getContracts()){
			this.symbol = i.getRtSymbol();
			// todo : now only consider 1 aim
			return;
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
		log.info(String.format(
				"type: %s, price: %f, longIn: %f, shortIn: %f,  longOut: %f, shortOut: %f, time : %s",
				tick.getRtSymbol(), tick.getLastPrice(),
				meta.longIn, meta.shortIn, meta.longOut, meta.shortOut,
				tick.getDateTime()
		));

		if(meta.lastOP != Meta.Signel.NOOP) {// something is still doing
			// todo: how to track the order?  succeed/failed/partly
			log.info(meta.lastOP + "is still doing; omit this tick");
		} else {
			double price = tick.getLastPrice();
			Meta.Signel signel = meta.doWhat(price);
			meta.lastOP = signel;
			switch (signel) {
				case NOOP:
					return;
				case IMPOSSIABLE:
					log.info("WTF");
					log.info(tick.toString());
					break;
				case LONG_IN:
					buy(this.symbol, 1, tick.getUpperLimit(), tick.getGatewayID());
					break;
				case SHORT_IN:
					sellShort(this.symbol, 1, tick.getLowerLimit(), tick.getGatewayID());
					break;
				case LONG_OUT:
					sell(this.symbol, meta.volume, tick.getLowerLimit(), tick.getGatewayID());
					break;
				case SHORT_OUT:
					buyToCover(this.symbol, meta.volume, tick.getUpperLimit(), tick.getGatewayID());
					break;
			}
			meta.updateByPrice(price, signel);
		}
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		// todo: update somethis
		log.info(String.format("VVV %s %s %d %f",
				symbol, meta.volType, meta.volume,
				meta.volType == Meta.Signel.LONG_IN ? meta.longOut : meta.shortOut
				));
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info(String.format("enter order, get status %s:%s:%d:%f:%s",
				order.getRtSymbol(), order.getDirection(), order.getTotalVolume(),
				order.getPrice(), order.getStatus()));
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		log.info(String.format("enter trade, get status %s:%s:%d:%f",
				trade.getRtSymbol(), trade.getDirection(),
				trade.getVolume(), trade.getPrice()));
		this.meta.lastOP = Meta.Signel.NOOP;
	}

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {
		
	}

}
