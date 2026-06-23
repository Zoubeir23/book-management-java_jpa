package sn.iage.isi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import sn.iage.isi.entities.Book;
import sn.iage.isi.entities.Category;
import sn.iage.isi.utils.JpaUtil;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class BookRepository {

    private final EntityManager em = JpaUtil.getEntityManager();

    public void createBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
            book.setIsbn(generateIsbn());
        }
        
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Category managedCategory = book.getCategory();
            if (managedCategory != null) {
                if (managedCategory.getId() != 0) {
                    managedCategory = em.getReference(Category.class, managedCategory.getId());
                    book.setCategory(managedCategory);
                } else {
                    em.persist(managedCategory);
                }
            } else {
                throw new RuntimeException("La catégorie est obligatoire.");
            }

            book.setUserCreated("admin");
            book.setUserUpdated("admin");

            em.persist(book);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<Book> ListAllBooks() {
        return em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
    }

    public Book findBookById(int id) {
        return em.find(Book.class, id);
    }

    public Book findBookByIsbn(String isbn) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", isbn);
        return query.getResultStream().findFirst().orElse(null);
    }

    public void updateBook(int id, Book newBook) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book existingBook = em.find(Book.class, id);
            if (existingBook != null) {
                if (newBook.getTitle() != null) existingBook.setTitle(newBook.getTitle());
                if (newBook.getAuthor() != null) existingBook.setAuthor(newBook.getAuthor());
                if (newBook.getYear() != 0) existingBook.setYear(newBook.getYear());
                if (newBook.getCategory() != null) existingBook.setCategory(newBook.getCategory());
                if (newBook.getIsbn() != null) existingBook.setIsbn(newBook.getIsbn());
                
                existingBook.setUserUpdated("admin");
                em.merge(existingBook);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public void deleteBook(int id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book book = em.find(Book.class, id);
            if (book != null) {
                em.remove(book);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<Book> ListeBooksByCategory(String categoryName) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE b.category.name = :category", Book.class);
        query.setParameter("category", categoryName);
        return query.getResultList();
    }

    public List<Book> searchBooksByTitle(String keyword) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:keyword)", Book.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(:keyword)", Book.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    public List<Book> searchBooksAfterYear(int year) {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE b.year > :year", Book.class);
        query.setParameter("year", year);
        return query.getResultList();
    }

    public Map<String, Long> countBooksByCategory() {
        List<Object[]> results = em.createQuery(
                "SELECT b.category.name, COUNT(b) FROM Book b GROUP BY b.category.name", 
                Object[].class
        ).getResultList();
        
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    public long countAllBooks() {
        return em.createQuery("SELECT COUNT(b) FROM Book b", Long.class).getSingleResult();
    }

    private String generateIsbn() {
        String[] prefixes = {"978", "979"};
        Random random = new Random();

        String prefix = prefixes[random.nextInt(2)];
        String group = String.valueOf(random.nextInt(2));
        String publisher = String.format("%04d", random.nextInt(10000));
        String title    = String.format("%04d", random.nextInt(10000));

        String base = prefix + group + publisher + title;
        int checkDigit = computeIsbn13CheckDigit(base);

        return String.format("%s-%s-%s-%s-%d", prefix, group, publisher, title, checkDigit);
    }

    private int computeIsbn13CheckDigit(String base12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int remainder = sum % 10;
        return remainder == 0 ? 0 : 10 - remainder;
    }
}
