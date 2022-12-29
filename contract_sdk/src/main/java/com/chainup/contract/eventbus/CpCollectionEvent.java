package com.chainup.contract.eventbus;

/**
 * k线图自选相关操作
 */
public class CpCollectionEvent {
    public static final String TYPE_REQUEST = "request"; //请求
    public static final String TYPE_ADD_DEL = "add_del"; //新增或者删除自选

    public String type;
    public int operationType;
    public String symbol;

    public CpCollectionEvent(String type) {
        this.type = type;
    }

    public CpCollectionEvent(String type, int operationType, String symbol) {
        this.type = type;
        this.operationType = operationType;
        this.symbol = symbol;
    }
}
