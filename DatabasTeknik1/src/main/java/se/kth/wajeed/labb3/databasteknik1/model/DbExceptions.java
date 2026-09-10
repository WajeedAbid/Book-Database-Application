package se.kth.wajeed.labb3.databasteknik1.model;

/**
 * Eget databasutantag (DB-neutralt).
 *
 * Tanken är att varken controller eller övrig klientkod ska behöva känna till
 * databasspecifika undantag (t.ex. SQLException eller MongoException).
 *
 * Varje DB-implementation fångar sina egna undantag och wrappar dem i DbExceptions.
 */
public class DbExceptions extends Exception {

    public DbExceptions(String message) {
        super(message);
    }

    public DbExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
