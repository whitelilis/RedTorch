package xyz.redtorch.strategy.routine.pub.impl.wizard;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/9/13
 */
public class Meta {
    public Signel lastOP = Signel.NOOP;
    public int volume = 0;
    public Signel volType = Signel.NOOP;
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

    public enum Signel {
        NOOP,
        LONG_IN,
        LONG_OUT,
        SHORT_IN,
        SHORT_OUT,
        IMPOSSIABLE
    }

    public Signel doWhat(double newPrice){
        // most time, do nothing
        if(newPrice <= longIn || newPrice >= shortIn){
            return Signel.NOOP;
        } else if (newPrice > longIn){
            return Signel.LONG_IN;
        } else if (newPrice < shortIn){
            return Signel.SHORT_IN;
        } else if (newPrice < longOut){
            return Signel.LONG_OUT;
        } else if (newPrice > shortOut){
            return Signel.SHORT_OUT;
        } else {
            return Signel.IMPOSSIABLE;
        }
    }

    public void updateByPrice(double newPrice, Signel signel){
        if(signel == Signel.LONG_IN){
           longIn = longIn * (1 + profitRate);
           longOut = newPrice * (1 - lossRate);
           volType = Signel.LONG_IN;
           volume = volume + 1;
        }else if(signel == Signel.SHORT_IN){
            shortIn = shortIn * ( 1 - profitRate);
            shortOut = newPrice * ( 1 + lossRate);
            volType = Signel.SHORT_IN;
            volume = volume + 1;
        }else{ // todo : when out, do what?
        }
    }

}
