package com.hongweiyi.bench;

import com.google.common.util.concurrent.AbstractFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author hongweiyi
 * @since 2014-Jun-14
 */
public class ResponseFuture<T>  extends AbstractFuture<T> {

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        if (timeout <= 0) {
            timeout = 3000;
            unit = TimeUnit.MILLISECONDS;
        }
        return super.get(timeout, unit);
    }

    public void handleResponse(T response) {
        this.set(response);
    }
}
