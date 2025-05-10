package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BookTagTableDef extends TableDef {
    public static final BookTagTableDef BOOKTAG = new BookTagTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn BID = new QueryColumn(this, "bid");
    public final QueryColumn TID = new QueryColumn(this, "tid");

    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, BID, TID
    };

    public BookTagTableDef(){
        super("book_tag","book_tag");
    }
    public static BookTagTableDef getInstance(){
        return BOOKTAG;
    }
}
