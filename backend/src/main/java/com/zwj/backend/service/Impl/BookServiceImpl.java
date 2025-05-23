package com.zwj.backend.service.Impl;

import com.mybatisflex.core.row.Db;
import com.zwj.backend.entity.Book;
import com.zwj.backend.entity.BookTag;
import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.Tag;
import com.zwj.backend.mapper.BookMapper;
import com.zwj.backend.mapper.BookTagMapper;
import com.zwj.backend.mapper.TagMapper;
import com.zwj.backend.service.BookService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zwj.backend.entity.table.BookTableDef.BOOK;
import static com.zwj.backend.entity.table.TagTableDef.TAG;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookTagMapper bookTagMapper;
    private final TagMapper tagMapper;

    public BookServiceImpl(BookMapper bookMapper, BookTagMapper bookTagMapper, TagMapper tagMapper) {
        this.bookMapper = bookMapper;
        this.bookTagMapper = bookTagMapper;
        this.tagMapper = tagMapper;
    }

    // 加载图书信息
    @Override
    public Result<Book> getBookById(Long id) {
        Book book = bookMapper.selectOneById(id);
        if (book != null) {
            book.setTag(getTagsByBookId(id));
            return Result.success(book);
        } else {
            return Result.error(404, "书籍不存在！");
        }
    }

    // 加载图书tag
    @Override
    public List<Tag> getTagsByBookId(Long bookId) {
        List<BookTag> bookTags = bookTagMapper.selectListByQuery(
                QueryWrapper.create().where("bid = ?", bookId));
        if (bookTags == null || bookTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = bookTags.stream().map(BookTag::getTid).collect(Collectors.toList());

        QueryWrapper query = QueryWrapper.create();
        if (!tagIds.isEmpty()) {
            query.where(Tag::getId).in(tagIds);
        }

        return tagMapper.selectListByQuery(query);
    }


    // 添加带排序功能的searchBooks方法实现
    // 添加带价格区间筛选的searchBooks方法实现
    @Override
    public Result<Page<Book>> searchBooks(int pageNum, int pageSize, String keyword, String sortBy, String sortOrder, Double minPrice, Double maxPrice) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        //查询在售书籍
        queryWrapper.and(BOOK.STATUS.eq(1));
        
        // 添加关键词筛选
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.where(BOOK.TITLE.like("%" + keyword + "%")
                    .or(BOOK.WRITER.like("%" + keyword + "%"))
                    .or(BOOK.ISBN.like("%" + keyword + "%")));
        }
        
        // 添加价格区间筛选
        if (minPrice != null && maxPrice != null) {
            queryWrapper.and(BOOK.PRICE.between(minPrice, maxPrice));
        } else if (minPrice != null) {
            queryWrapper.and(BOOK.PRICE.ge(minPrice));
        } else if (maxPrice != null) {
            queryWrapper.and(BOOK.PRICE.le(maxPrice));
        }
        
        // 添加排序
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);
            
            // 根据sortBy字段添加排序
            switch (sortBy) {
                case "id":
                    queryWrapper.orderBy(BOOK.ID, isAsc);
                    break;
                case "title":
                    queryWrapper.orderBy(BOOK.TITLE, isAsc);
                    break;
                case "writer":
                    queryWrapper.orderBy(BOOK.WRITER, isAsc);
                    break;
                case "price":
                    queryWrapper.orderBy(BOOK.PRICE, isAsc);
                    break;
                case "cost":
                    queryWrapper.orderBy(BOOK.COST, isAsc);
                    break;
                case "stock":
                    queryWrapper.orderBy(BOOK.STOCK, isAsc);
                    break;
                case "isbn":
                    queryWrapper.orderBy(BOOK.ISBN, isAsc);
                    break;
                case "sales":
                    queryWrapper.orderBy(BOOK.SALES, isAsc);
                    break;
                default:
                    queryWrapper.orderBy(BOOK.ID, true);
                    break;
            }
        } else {
            // 默认按id排序
            queryWrapper.orderBy(BOOK.ID, true);
        }
        
        // 执行分页查询
        Page<Book> bookPage = bookMapper.paginate(pageNum, pageSize, queryWrapper);
        
        // 为每本书加载标签信息
        for (Book book : bookPage.getRecords()) {
            book.setTag(getTagsByBookId(book.getId()));
        }
        
        return Result.success(bookPage);
    }

    //管理员查找图书接口
    @Override
    public Result<Page<Book>> adminSearchBooks(int pageNum, int pageSize, String keyword, String sortBy, String sortOrder, Double minPrice, Double maxPrice) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.and(BOOK.STATUS.eq(1).or(BOOK.STATUS.eq(0)));

        // 添加关键词筛选
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.where(BOOK.TITLE.like("%" + keyword + "%")
                    .or(BOOK.WRITER.like("%" + keyword + "%"))
                    .or(BOOK.ISBN.like("%" + keyword + "%")));
        }

        // 添加价格区间筛选
        if (minPrice != null && maxPrice != null) {
            queryWrapper.and(BOOK.PRICE.between(minPrice, maxPrice));
        } else if (minPrice != null) {
            queryWrapper.and(BOOK.PRICE.ge(minPrice));
        } else if (maxPrice != null) {
            queryWrapper.and(BOOK.PRICE.le(maxPrice));
        }

        // 添加排序
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);

            // 根据sortBy字段添加排序
            switch (sortBy) {
                case "id":
                    queryWrapper.orderBy(BOOK.ID, isAsc);
                    break;
                case "title":
                    queryWrapper.orderBy(BOOK.TITLE, isAsc);
                    break;
                case "writer":
                    queryWrapper.orderBy(BOOK.WRITER, isAsc);
                    break;
                case "price":
                    queryWrapper.orderBy(BOOK.PRICE, isAsc);
                    break;
                case "cost":
                    queryWrapper.orderBy(BOOK.COST, isAsc);
                    break;
                case "stock":
                    queryWrapper.orderBy(BOOK.STOCK, isAsc);
                    break;
                case "isbn":
                    queryWrapper.orderBy(BOOK.ISBN, isAsc);
                    break;
                case "sales":
                    queryWrapper.orderBy(BOOK.SALES, isAsc);
                    break;
                default:
                    queryWrapper.orderBy(BOOK.ID, true);
                    break;
            }
        } else {
            // 默认按id排序
            queryWrapper.orderBy(BOOK.ID, true);
        }

        // 执行分页查询
        Page<Book> bookPage = bookMapper.paginate(pageNum, pageSize, queryWrapper);

        // 为每本书加载标签信息
        for (Book book : bookPage.getRecords()) {
            book.setTag(getTagsByBookId(book.getId()));
        }

        return Result.success(bookPage);
    }

    //添加图书
    @Override
    @Transactional
    public Result<Void> createBook(Book book) {
        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数

        // 先保存图书，获取生成的ID
        Db.tx(() -> {
            affectedRows1.set(bookMapper.insert(book));
            return affectedRows1.get() > 0;
        });

        // 获取插入后的ID
        Long bookId = (bookMapper.selectOneByQuery(QueryWrapper.create()
                .where(BOOK.TITLE.eq(book.getTitle())))).getId();

        System.out.println(book.getTag() == null);

        if (affectedRows1.get() > 0 && book.getTag() != null && !book.getTag().isEmpty()) {
            // 保存标签关联
            setBookTag(bookId, book);
            return Result.success(null);
        } else if (affectedRows1.get() > 0) {
            return Result.success(null);
        } else {
            return Result.error(400,"添加失败");
        }
    }

    //更新图书信息
    @Transactional
    @Override
    public Result<Void> updateBook(Long id, Book newBook) {
        Book book = bookMapper.selectOneById(id);
        if (book != null) {
            book.setTag(getTagsByBookId(id));
        } else {
            return Result.error(400,"书本不存在");
        }

        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数

        copyNonNullProperties(newBook, book);

        Db.tx(() -> {
            newBook.setId(id);
            affectedRows1.set(bookMapper.update(newBook));
            return affectedRows1.get() > 0;
        });

        if (affectedRows1.get() > 0 && newBook.getTag() != null) {
            bookTagMapper.deleteByQuery(
                    QueryWrapper.create().where("bid = ?", id));

            setBookTag(id, book);
            return Result.success(null);
        } else if (affectedRows1.get() > 0) {
            return Result.success(null);
        } else {
            return Result.error(400,"更新失败");
        }
    }

    //图书设置标签关系
    private void setBookTag(Long id, Book book) {
        for (Tag tag : book.getTag()) {
            Tag existingTag = null;

            // 按名称查找现有标签
            if (tag.getName() != null && !tag.getName().isEmpty()) {
                existingTag = tagMapper.selectOneByQuery(QueryWrapper.create()
                        .where(TAG.NAME.eq(tag.getName())));
            }

            if (existingTag == null) {
                if (tag.getName() == null || tag.getName().isEmpty()) {
                    continue;
                }
                tagMapper.insert(tag);
                existingTag = tag;
            }
            Long tagId = (tagMapper.selectOneByQuery(QueryWrapper.create()
                    .where(TAG.NAME.eq(existingTag.getName())))).getId();
            // 插入中间表关联
            BookTag bookTag = new BookTag();
            bookTag.setBid(id);
            bookTag.setTid(tagId);
            bookTagMapper.insert(bookTag);
        }
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        // 高并发场景使用悲观锁
        Book book = bookMapper.selectForUpdate(id);
        
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        
        if (book.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        book.setStock(book.getStock() - quantity);
        
        // 更新销量
        if (book.getSales() == 0) {
            book.setSales(quantity);
        } else {
            book.setSales(book.getSales() + quantity);
        }
        
        bookMapper.update(book);
    }

    private void copyNonNullProperties(Book src, Book target) {
        if (src.getTitle() != null) target.setTitle(src.getTitle());
        if (src.getWriter() != null) target.setWriter(src.getWriter());
        if (src.getIsbn() != null) target.setIsbn(src.getIsbn());
        if (src.getCover() != null) target.setCover(src.getCover());
        if (src.getPrice() != null) target.setPrice(src.getPrice());
        if (src.getCost() != null) target.setCost(src.getCost());
        if (src.getStock() != null) target.setStock(src.getStock());
        if (src.getTag() != null) target.setTag(src.getTag());
    }
    
    @Override
    @Transactional
    public Result<Void> toggleBookStatus(Long id) {
        Book book = bookMapper.selectOneById(id);
        if (book == null) {
            return Result.error(404, "书籍不存在！");
        }
        
        // 切换状态
        book.setStatus(!book.getStatus());
        
        AtomicInteger affectedRows = new AtomicInteger();
        Db.tx(() -> {
            affectedRows.set(bookMapper.update(book));
            return affectedRows.get() > 0;
        });
        
        if (affectedRows.get() > 0) {
            return Result.success(null);
        } else {
            return Result.error(400, "状态切换失败");
        }
    }
}
