package com.zwj.backend.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// 自动生成的表定义类，用于MyBatis-Flex查询
public class BookTableDef extends TableDef {

    // 表名
    public static final BookTableDef BOOK = new BookTableDef();

    // 字段定义
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

    // 所有字段 (为了能够使用selectAll等方法)
    public final QueryColumn[] ALL_COLUMNS = new QueryColumn[] {
            ID, TITLE, WRITER, ISBN, COVER, PRICE, COST, STOCK,
            CREATED_BY, SALES, STATUS
    };

    // 构造方法，指定表名
    public BookTableDef() {
        super("book","book");
    }

    // 获取默认实例
    public static BookTableDef getInstance() {
        return BOOK;
    }
}