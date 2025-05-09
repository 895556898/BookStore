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

import java.util.Collections;
import java.util.List;
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

        return tagMapper.selectListByQuery(
                QueryWrapper.create().where("id in (?)", tagIds));
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
        if (tids == null || tids.isEmpty()) {
            return searchBooks(pageNum, pageSize, ""); // 空标签，返回全部图书分页
        }

        List<BookTag> bookTags = bookTagMapper.selectListByQuery(
                QueryWrapper.create().where("tid in (?)", tids));

        if (bookTags == null || bookTags.isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize, 0));
        }
        List<Long> bookIds = bookTags.stream().map(BookTag::getBid).distinct().collect(Collectors.toList());

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("id in (?)", bookIds)
                .orderBy("id asc");
        Page<Book> bookPage = bookMapper.paginate(pageNum, pageSize, queryWrapper);

        for (Book book : bookPage.getRecords()) { // 加载标签
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
        System.out.println(bookId);

        if (affectedRows1.get() > 0 && book.getTag() != null && !book.getTag().isEmpty()) {
            // 保存标签关联
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

                // 插入中间表关联
                BookTag bookTag = new BookTag();
                bookTag.setBid(bookId);
                bookTag.setTid(existingTag.getId());
                bookTagMapper.insert(bookTag);
            }
            return StatusCode.BOOK_ADD_SUCCESS;
        } else if (affectedRows1.get() > 0 && book.getTag() == null) {
            return StatusCode.BOOK_ADD_SUCCESS;
        } else {
            return StatusCode.BOOK_ADD_FAIL;
        }
    }

    @Transactional
    @Override
    public StatusCode updateBook(String title, Book newBook) {
        final Book[] oldBookRef = new Book[1];

        // 先尝试通过ID查找
        oldBookRef[0] = bookMapper.selectOneById(newBook.getId());

        // 如果按ID找不到，尝试按标题查找
        if (oldBookRef[0] == null) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(BOOK.TITLE.eq(title));
            oldBookRef[0] = bookMapper.selectOneByQuery(queryWrapper);

            if (oldBookRef[0] == null) {
                return StatusCode.BOOK_UPDATE_FAIL;
            }
        }

        final Long bookId = oldBookRef[0].getId();
        AtomicInteger affectedRows1 = new AtomicInteger(); // 商品表中受影响的行数

        Db.tx(() -> {
            newBook.setId(bookId);
            affectedRows1.set(bookMapper.update(newBook));
            return affectedRows1.get() > 0;
        });

        if (affectedRows1.get() > 0 && newBook.getTag() != null) {
            // 删除旧关联，使用正确的字段名
            bookTagMapper.deleteByQuery(
                    QueryWrapper.create().where("bid = ?", bookId));

            // 添加新的关联关系
            for (Tag tag : newBook.getTag()) {
                BookTag bookTag = new BookTag();
                bookTag.setBid(bookId);
                bookTag.setTid(tag.getId());
                bookTagMapper.insert(bookTag);
            }
            return StatusCode.BOOK_UPDATE_SUCCESS;
        } else if (affectedRows1.get() > 0) {
            return StatusCode.BOOK_UPDATE_SUCCESS;
        } else {
            return StatusCode.BOOK_UPDATE_FAIL;
        }
    }

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
}
