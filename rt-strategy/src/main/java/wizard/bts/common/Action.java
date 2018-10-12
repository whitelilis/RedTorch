package wizard.bts.common;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/12
 */
public interface Action {
    enum Status{
        SUCCESS,
        FAILED,
        PARTLY_SUCCESS,
        RUNNING,
        UNDEFINE
    }

    Status status = Status.UNDEFINE;

    //void act();
}
