package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OrderTransactionTableDef extends TableDef {
    
    public static final OrderTransactionTableDef ORDER_TRANSACTION = new OrderTransactionTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");
    public final QueryColumn AMOUNT = new QueryColumn(this, "amount");
    public final QueryColumn TRANSACTION_TIME = new QueryColumn(this, "transaction_time");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn PAYMENT_METHOD = new QueryColumn(this, "payment_method");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, ORDER_ID, AMOUNT, TRANSACTION_TIME, STATUS, 
            PAYMENT_METHOD, REMARK, CREATE_TIME, UPDATE_TIME
    };
    
    public OrderTransactionTableDef() {
        super("order_transactions", "order_transactions");
    }
    
    public static OrderTransactionTableDef getInstance() {
        return ORDER_TRANSACTION;
    }
} 