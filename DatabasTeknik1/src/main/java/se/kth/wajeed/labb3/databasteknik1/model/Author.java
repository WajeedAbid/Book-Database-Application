package se.kth.wajeed.labb3.databasteknik1.model;

public class Author {

    private final int authorId;
    private final String fullName;

    public Author(int authorId, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName is null/blank");
        }

        this.authorId = authorId;
        this.fullName = fullName;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
