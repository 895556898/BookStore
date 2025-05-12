package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CartItemTableDef extends TableDef {
    
    public static final CartItemTableDef CART_ITEM = new CartItemTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");
    public final QueryColumn BOOK_ID = new QueryColumn(this, "book_id");
    public final QueryColumn QUANTITY = new QueryColumn(this, "quantity");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, USER_ID, BOOK_ID, QUANTITY, CREATE_TIME, UPDATE_TIME
    };
    
    public CartItemTableDef() {
        super("cart_items", "cart_items");
    }
    
    public static CartItemTableDef getInstance() {
        return CART_ITEM;
    }
} 