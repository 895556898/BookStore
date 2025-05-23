package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BookTableDef extends TableDef {

    public static final BookTableDef BOOK = new BookTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn WRITER = new QueryColumn(this, "writer");
    public final QueryColumn ISBN = new QueryColumn(this, "isbn");
    public final QueryColumn COVER = new QueryColumn(this, "cover");
    public final QueryColumn PRICE = new QueryColumn(this, "price");
    public final QueryColumn COST = new QueryColumn(this, "cost");
    public final QueryColumn STOCK = new QueryColumn(this, "stock");
    public final QueryColumn CREATED_BY = new QueryColumn(this, "created_by");
    public final QueryColumn SALES = new QueryColumn(this,"sales");
    public final QueryColumn STATUS = new QueryColumn(this,"status");

    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, TITLE, WRITER, ISBN, COVER, PRICE, COST, STOCK,
            CREATED_BY, SALES, STATUS
    };

    public BookTableDef() {
        super("book","book");
    }

    public static BookTableDef getInstance() {
        return BOOK;
    }
}