package com.pq.rpc.api.callback;

/**
 * 回调接口
 *
 * @author pengqi
 * create at 2019/7/5
 */
public interface CallbackInterface {
    /**
     * 回调方法
     * @param result RPC请求返回的结果
     */
    void getInfoFromClient(String result);
}
