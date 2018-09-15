package xyz.redtorch.strategy.routine.pub.impl.wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/9/13
 */
public class Meta {
    private static final Logger log = LoggerFactory.getLogger(Meta.class);
    public Signal lastOP = Signal.NOOP;
    public int volume = 0;
    public Signal volType = Signal.NOOP;
    public double profitRate = 0.03F;
    public double lossRate = 0.01F;
    public double longIn = Float.MAX_VALUE;
    public double longOut = longIn * (1 - lossRate);
    public double shortIn = Float.MIN_VALUE;
    public double shortOut = shortIn * (1 + lossRate);

    public Meta(float longIn, float shortIn){
        this.longIn = longIn;
        this.shortIn = shortIn;
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
        // most time, do nothing
        if(newPrice <= longIn && newPrice >= shortIn){
            return Signal.NOOP;
        } else if (newPrice > longIn){
            return Signal.LONG_IN;
        } else if (newPrice < shortIn){
            return Signal.SHORT_IN;
        } else if (newPrice < longOut){
            return Signal.LONG_OUT;
        } else if (newPrice > shortOut){
            return Signal.SHORT_OUT;
        } else {
            log.error("WWW impossible happens when price is {}", newPrice);
            return Signal.IMPOSSIABLE;
        }
    }

    public void updateByPrice(double newPrice, Signal signel){
        if(signel == Signal.LONG_IN){
           longIn = longIn * (1 + profitRate);
           longOut = newPrice * (1 - lossRate);
           volType = Signal.LONG_IN;
           volume = volume + 1;
        }else if(signel == Signal.SHORT_IN){
            shortIn = shortIn * ( 1 - profitRate);
            shortOut = newPrice * ( 1 + lossRate);
            volType = Signal.SHORT_IN;
            volume = volume + 1;
        }else if(signel == Signal.NOOP){
            // when no op, maybe update loss price
            double maybeBig = newPrice * (1 - lossRate);
            double maybeSmall = newPrice * (1 + lossRate);
            if(maybeBig > longOut){
                log.info("update logout {} ==> {}", longOut, maybeBig);
                longOut = maybeBig;
            }
            if(maybeSmall < shortOut){
                log.info("update shortOut {} ==> {}", shortOut, maybeSmall);
                shortOut = maybeSmall;
            }
        }else{ // todo : when out, do what?
        }
    }

}
