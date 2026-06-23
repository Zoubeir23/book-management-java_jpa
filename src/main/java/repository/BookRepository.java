package repository;

import entity.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class BookRepository {

    private EntityManager entityManager;

    public BookRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void createBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
            book.setIsbn(generateIsbn());
        }
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(book);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Book> ListAllBooks() {
        return entityManager.createQuery("SELECT b FROM Book b", Book.class).getResultList();
    }

    public Book findBookById(int id) {
        return entityManager.find(Book.class, id);
    }

    public Book findBookByIsbn(String isbn) {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", isbn);
        return query.getResultStream().findFirst().orElse(null);
    }

    public void updateBook(int id, Book newBook) {
        try {
            entityManager.getTransaction().begin();
            Book existingBook = entityManager.find(Book.class, id);
            if (existingBook != null) {
                if (newBook.getTitle() != null) existingBook.setTitle(newBook.getTitle());
                if (newBook.getAuthor() != null) existingBook.setAuthor(newBook.getAuthor());
                if (newBook.getYear() != 0) existingBook.setYear(newBook.getYear());
                if (newBook.getCategoryName() != null) existingBook.setCategoryName(newBook.getCategoryName());
                if (newBook.getIsbn() != null) existingBook.setIsbn(newBook.getIsbn());
                
                entityManager.merge(existingBook);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void deleteBook(int id) {
        try {
            entityManager.getTransaction().begin();
            Book book = entityManager.find(Book.class, id);
            if (book != null) {
                entityManager.remove(book);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Book> ListeBooksByCategory(String categoryName) {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b WHERE b.categoryName = :category", Book.class);
        query.setParameter("category", categoryName);
        return query.getResultList();
    }

    public List<Book> searchBooksByTitle(String keyword) {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:keyword)", Book.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(:keyword)", Book.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    public List<Book> searchBooksAfterYear(int year) {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b WHERE b.year > :year", Book.class);
        query.setParameter("year", year);
        return query.getResultList();
    }

    public Map<String, Long> countBooksByCategory() {
        List<Object[]> results = entityManager.createQuery(
                "SELECT b.categoryName, COUNT(b) FROM Book b GROUP BY b.categoryName", 
                Object[].class
        ).getResultList();
        
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            countMap.put((String) result[0], (Long) result[1]);
        }
        return countMap;
    }

    public long countAllBooks() {
        return entityManager.createQuery("SELECT COUNT(b) FROM Book b", Long.class).getSingleResult();
    }

    private String generateIsbn() {
        // Préfixe ISBN-13 : 978 ou 979
        String[] prefixes = {"978", "979"};
        Random random = new Random();

        String prefix = prefixes[random.nextInt(2)];        // 978 ou 979
        String group = String.valueOf(random.nextInt(2));    // 0 ou 1 (groupe langue)
        String publisher = String.format("%04d", random.nextInt(10000));   // éditeur 4 chiffres
        String title    = String.format("%04d", random.nextInt(10000));    // titre   4 chiffres

        // Calcul du chiffre de contrôle (checksum ISBN-13)
        String base = prefix + group + publisher + title;   // 12 chiffres
        int checkDigit = computeIsbn13CheckDigit(base);

        String isbn = base + checkDigit;

        // Format lisible : 978-X-XXXX-XXXX-X
        return String.format("%s-%s-%s-%s-%d",
                prefix, group, publisher, title, checkDigit);
    }

    private int computeIsbn13CheckDigit(String base12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;   // alternance poids 1 et 3
        }
        int remainder = sum % 10;
        return remainder == 0 ? 0 : 10 - remainder;
    }
}
