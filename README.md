# Book Database Application

A Java desktop application developed as part of the Database Technology course at KTH Royal Institute of Technology.

The application provides a graphical interface for managing and searching a book database and was designed to work with both relational and NoSQL database implementations.

## Features

- Search books by title
- Search books by ISBN
- Search books by author
- Search books by genre
- Search books by rating
- Add new books
- Add authors and genres
- Rate books
- Display book and author information
- Database connection handling

## Technologies

- Java
- JavaFX
- Maven
- MySQL
- MongoDB
- JDBC
- MongoDB Java Driver

## Architecture

The application is structured into separate layers for the user interface, application logic and database access.

The database layer is defined through a common `BooksDb` interface, allowing different database implementations to be used with the application.

Implemented database backends include:

- `MySqlBooksDb` – relational database implementation using MySQL and JDBC
- `MongoBooksDb` – NoSQL implementation using MongoDB

The application also separates the graphical interface, controller logic and data model.

## Database Concepts

The project explores several database concepts, including:

- Relational database design
- NoSQL document databases
- SQL queries
- Prepared statements
- Database transactions
- Indexing
- Data relationships
- CRUD operations
- Database abstraction

## Course

Developed as part of the **Database Technology** course at  
**KTH Royal Institute of Technology**.
