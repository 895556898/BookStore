package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OrderTableDef extends TableDef {
    
    public static final OrderTableDef ORDER = new OrderTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn PAYMENT_METHOD = new QueryColumn(this, "payment_method");
    public final QueryColumn PAYMENT_TIME = new QueryColumn(this, "payment_time");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, USER_ID, TOTAL_AMOUNT, STATUS, REMARK, PAYMENT_METHOD, PAYMENT_TIME, CREATE_TIME, UPDATE_TIME
    };
    
    public OrderTableDef() {
        super("orders", "orders");
    }
    
    public static OrderTableDef getInstance() {
        return ORDER;
    }
} 