package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OrderItemTableDef extends TableDef {
    
    public static final OrderItemTableDef ORDER_ITEM = new OrderItemTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");
    public final QueryColumn BOOK_ID = new QueryColumn(this, "book_id");
    public final QueryColumn QUANTITY = new QueryColumn(this, "quantity");
    public final QueryColumn PRICE = new QueryColumn(this, "price");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, ORDER_ID, BOOK_ID, QUANTITY, PRICE, CREATE_TIME, UPDATE_TIME
    };
    
    public OrderItemTableDef() {
        super("order_items", "order_items");
    }
    
    public static OrderItemTableDef getInstance() {
        return ORDER_ITEM;
    }
} 