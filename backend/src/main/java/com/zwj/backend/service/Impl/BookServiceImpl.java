package com.zwj.backend.service.Impl;

import com.mybatisflex.core.row.Db;
import com.zwj.backend.common.StatusCode;
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
import static com.zwj.backend.entity.table.BookTagTableDef.BOOKTAG;
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

    // @Override
    // public Page<Book> getBooksByPage(int pageNum, int pageSize) {
    // return bookMapper.paginate(pageNum, pageSize);
    // }

    // 关键字查询图书
    @Override
    public Result<Page<Book>> searchBooks(int pageNum, int pageSize, String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.where(BOOK.TITLE.like("%" + keyword + "%")
                    .or(BOOK.WRITER.like("%" + keyword + "%")));
        }
        queryWrapper.orderBy(BOOK.ID.asc());
        // MyBatis-Flex分页查询
        Page<Book> bookPage = bookMapper.paginate(pageNum, pageSize, queryWrapper);
        // 为每本书加载标签信息
        for (Book book : bookPage.getRecords()) {
            book.setTag(getTagsByBookId(book.getId()));
        }
        return Result.success(bookPage);
    }

    // 标签查询图书
    @Override
    public Result<Page<Book>> searchBookByTagIds(int pageNum, int pageSize, List<Long> tids) {
        List<Long> bookIdList = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        if (tids == null || tids.isEmpty()) {
            return searchBooks(pageNum, pageSize, ""); // 空标签，返回全部图书分页
        }

        System.out.println(tids);

        for (long tagId : tids) {
            List<BookTag> bookTagList = bookTagMapper.selectListByQuery(QueryWrapper.create()
                    .where(BOOKTAG.TID.eq(tagId)));

            for (BookTag test1 : bookTagList) {
                Long bookId = test1.getBid();
                if (seen.add(bookId)) {
                    bookIdList.add(bookId);
                }
            }
        }

        System.out.println(bookIdList);

        QueryWrapper queryWrapper = QueryWrapper.create();
        if (!bookIdList.isEmpty()) {
           queryWrapper.where(BOOK.ID.in(bookIdList));
        }
        queryWrapper.orderBy(BOOK.ID.asc());
        Page<Book> bookPage = bookMapper.paginate(pageNum, pageSize, queryWrapper);

        for (Book book : bookPage.getRecords()) {
            book.setTag(getTagsByBookId(book.getId()));
        }

        return Result.success(bookPage);
    }

    //添加图书
    @Override
    @Transactional
    public StatusCode createBook(Book book) {
        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数

        // 先保存图书，获取生成的ID
        Db.tx(() -> {
            affectedRows1.set(bookMapper.insert(book));
            return affectedRows1.get() > 0;
        });

        // 获取插入后的ID
        Long bookId = (bookMapper.selectOneByQuery(QueryWrapper.create()
                .where(BOOK.TITLE.eq(book.getTitle())))).getId();

        if (affectedRows1.get() > 0 && book.getTag() != null && !book.getTag().isEmpty()) {
            // 保存标签关联
            setBookTag(bookId, book);
            return StatusCode.BOOK_ADD_SUCCESS;
        } else if (affectedRows1.get() > 0 && book.getTag() == null) {
            return StatusCode.BOOK_ADD_SUCCESS;
        } else {
            return StatusCode.BOOK_ADD_FAIL;
        }
    }

    //更新图书信息
    @Transactional
    @Override
    public StatusCode updateBook(Long id, Book newBook) {
        Book book = bookMapper.selectOneById(id);
        if (book != null) {
            book.setTag(getTagsByBookId(id));
        } else {
            return StatusCode.BOOK_UPDATE_FAIL;
        }

        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数

        copyNonNullProperties(newBook, book);

        Db.tx(() -> {
            newBook.setId(id);
            affectedRows1.set(bookMapper.update(newBook));
            return affectedRows1.get() > 0;
        });

        if (affectedRows1.get() > 0 && newBook.getTag() != null) {
            // 删除旧关联，使用正确的字段名
            bookTagMapper.deleteByQuery(
                    QueryWrapper.create().where("bid = ?", id));

            setBookTag(id, book);
            return StatusCode.BOOK_UPDATE_SUCCESS;
        } else if (affectedRows1.get() > 0) {
            return StatusCode.BOOK_UPDATE_SUCCESS;
        } else {
            return StatusCode.BOOK_UPDATE_FAIL;
        }
    }

    //图书设置标签关系
    private void setBookTag(Long id, Book book) {
        for (Tag tag : book.getTag()) {
            Tag existingTag = null;

            if (tag.getName() != null && !tag.getName().isEmpty()) {
                // 按名称查找现有标签
                existingTag = tagMapper.selectOneByQuery(QueryWrapper.create()
                        .where(TAG.NAME.eq(tag.getName())));
            }

            if (existingTag == null) {
                if (tag.getName() == null || tag.getName().isEmpty()) {
                    continue; // 跳过无效的tag
                }
                tagMapper.insert(tag);
                existingTag = tag; // 使用插入后的对象
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

    //删除图书
    @Override
    @Transactional
    public StatusCode deleteBook(Long id) {
        // 使用正确的字段名删除关联
        bookTagMapper.deleteByQuery(
                QueryWrapper.create().where("bid = ?", id));

        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数
        Db.tx(() -> {
            affectedRows1.set(bookMapper.deleteById(id));
            return affectedRows1.get() > 0;
        });

        if (affectedRows1.get() > 0) {
            return StatusCode.BOOK_DELETE_SUCCESS;
        } else {
            return StatusCode.BOOK_DELETE_FAIL;
        }
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Book book = bookMapper.selectOneById(id);
        if (book != null) {
            book.setStock(book.getStock() - quantity);
            bookMapper.update(book);
        }
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
}
