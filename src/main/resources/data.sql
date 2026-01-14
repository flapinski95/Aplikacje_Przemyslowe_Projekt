-- Inicjalizacja danych dla bazy danych
-- Ten plik jest automatycznie wykonywany przez Spring Boot przy starcie aplikacji (tylko w profilu dev)

-- Dodatkowe książki do bazy danych (poza tymi z DemoApplication.java)
INSERT INTO book (title, author, isbn) VALUES
('1984', 'George Orwell', '978-83-12345-67-9'),
('Zbrodnia i kara', 'Fiodor Dostojewski', '978-83-23456-78-0'),
('Wojna i pokój', 'Lew Tołstoj', '978-83-34567-89-1'),
('Lalka', 'Bolesław Prus', '978-83-45678-90-2'),
('Quo Vadis', 'Henryk Sienkiewicz', '978-83-56789-01-3');
