package wizard.blind;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Trade;

import java.util.ArrayList;

import static wizard.blind.Plan.Signal.IMPOSSIABLE;
import static wizard.blind.Plan.Signal.NOOP;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/9/13
 */
public class Plan {
    private static final Logger log = LoggerFactory.getLogger(Plan.class);
    public static final String savePrefix = "wizard_plan_";
    public static final double useMin = 0.88;
    public static final double useMax = 88888888.88;
    public int todayVolume = 0;
    public int yestodayVolume = 0;
    public String lastInDate = null;
    public double profitRate = 0.03F;
    public double lossRate = 0.01F;
    public double inPrice;
    public double outPrice;
    public Signal direction = Signal.IMPOSSIABLE;
    public ArrayList<Double> tradePrices = new ArrayList<>();
    public OrderManager orderManager = new OrderManager();

    public Plan(double inPrice, double outPrice){
        this.inPrice = inPrice;
        this.outPrice = outPrice;
    }
    public Plan(Signal direction, double inPrice, double lossRate, double profitRate){
        this.lossRate = lossRate;
        this.profitRate = profitRate;
        this.inPrice = inPrice;
        this.direction = direction;
        if(direction == Signal.LONG_IN) {
            this.outPrice = inPrice * (1 - lossRate);
        }else if(direction == Signal.SHORT_IN){
            this.outPrice = inPrice * (1 + lossRate);
        }else{
            log.error("direction not wanted :", direction);
            System.exit(0);
        }
    }

    public static Plan buyPlan(float lossRate, float profitRate){
        return new Plan(Signal.LONG_IN, useMin, lossRate, profitRate);
    }

    public static Plan sellPlan(float lossRate, float profitRate){
        return new Plan(Signal.SHORT_IN, useMax, lossRate, profitRate);
    }

    public static String saveKey(String rtSymbol){
        return savePrefix + rtSymbol;
    }

    // todo toJson
    public String toJson() {
        return JSONObject.toJSONString(this);
    }

    // todo fromJson
    public static Plan fromJson(String json){
        return JSONObject.parseObject(json, Plan.class);
    }


    public void updateVolume(Trade openTrade){
        String tradeDate = openTrade.getDateTime().toString(RtConstant.D_FORMAT);
        if(lastInDate == null || lastInDate.equals(tradeDate)) {// first complete order, or the same day
            todayVolume += openTrade.getVolume();
        }else{
            yestodayVolume += todayVolume;
            todayVolume = openTrade.getVolume();
        }
        lastInDate = tradeDate;
    }

    public boolean validate(){
        switch (direction){
            case LONG_IN:
                return inPrice > outPrice;
            case SHORT_IN:
                return inPrice < outPrice;
            default:
                return false;
        }
    }

    public enum Signal {
        NOOP,
        LONG_IN,
        LONG_OUT,
        SHORT_IN,
        SHORT_OUT,
        IMPOSSIABLE
    }

    public Signal doWhat(double newPrice){
        if(! validate()){
            log.error("555555555555555 invlidate plan.");
            return IMPOSSIABLE;
        }else {
            switch (direction) {
                case LONG_IN:
                    if (newPrice > inPrice) {
                        return Signal.LONG_IN;
                    } else if (newPrice < outPrice) {
                        return Signal.LONG_OUT;
                    } else { // between, maybe update outPrice
                        double maybeBig = newPrice * (1 - lossRate);
                        if (maybeBig > outPrice) {
                            outPrice = maybeBig;
                        } else {
                            // skip
                        }
                        return NOOP;
                    }
                case SHORT_IN:
                    if (newPrice < inPrice) {
                        return Signal.SHORT_IN;
                    } else if (newPrice > outPrice) {
                        return Signal.SHORT_OUT;
                    } else { // between, maybe update outPrice
                        double maybeSmall = newPrice * (1 + lossRate);
                        if (maybeSmall < outPrice) {
                            outPrice = maybeSmall;
                        } else {
                            // skip
                        }
                        return NOOP;
                    }
                default:
                    log.error("unexepct got a direction : {}.", direction);
                    System.exit(0);
                    return IMPOSSIABLE;
            }
        }
    }
}
