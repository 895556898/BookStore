package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OrderAddressTableDef extends TableDef {
    
    public static final OrderAddressTableDef ORDER_ADDRESS = new OrderAddressTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");
    public final QueryColumn RECEIVER = new QueryColumn(this, "receiver");
    public final QueryColumn PHONE = new QueryColumn(this, "phone");
    public final QueryColumn PROVINCE = new QueryColumn(this, "province");
    public final QueryColumn CITY = new QueryColumn(this, "city");
    public final QueryColumn DISTRICT = new QueryColumn(this, "district");
    public final QueryColumn DETAIL_ADDRESS = new QueryColumn(this, "detail_address");
    public final QueryColumn ZIP_CODE = new QueryColumn(this, "zip_code");
    public final QueryColumn IS_DEFAULT = new QueryColumn(this, "is_default");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, ORDER_ID, RECEIVER, PHONE, PROVINCE, CITY, DISTRICT, 
            DETAIL_ADDRESS, ZIP_CODE, IS_DEFAULT, CREATE_TIME, UPDATE_TIME
    };
    
    public OrderAddressTableDef() {
        super("order_addresses", "order_addresses");
    }
    
    public static OrderAddressTableDef getInstance() {
        return ORDER_ADDRESS;
    }
} 