IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'FurnitureLogisticsDB')
BEGIN
    CREATE DATABASE FurnitureLogisticsDB;
END
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'logistics_user')
BEGIN
    CREATE LOGIN logistics_user
    WITH PASSWORD = 'Password123!';
END
GO

USE FurnitureLogisticsDB;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'logistics_user')
BEGIN
    CREATE USER logistics_user FOR LOGIN logistics_user;
END
GO

ALTER ROLE db_owner ADD MEMBER logistics_user;
GO

CREATE TABLE Klient (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Adres VARCHAR(255) NOT NULL,
    Ime VARCHAR(100)
);

CREATE TABLE Potrebitel (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Ime VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Telefon VARCHAR(20)
);

CREATE TABLE Ofis (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Otdel VARCHAR(100),
    Rakovoditel VARCHAR(100)
);

CREATE TABLE Sklad (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Mestopolozhenie VARCHAR(255) NOT NULL,
    Kapacitet INT
);

CREATE TABLE Dostavchik (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Raion VARCHAR(100),
    Zaplata DECIMAL(10, 2)
);

CREATE TABLE Produkt (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Ime_Produkt VARCHAR(100) NOT NULL,
    Cena DECIMAL(10, 2)
);

CREATE TABLE Mebel (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Kategoria VARCHAR(100) NOT NULL
);

CREATE TABLE Profil (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Material VARCHAR(100) NOT NULL
);

CREATE TABLE Poryachka (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Data DATE NOT NULL,
    Klient_ID INT,
    Ofis_ID INT,
    FOREIGN KEY (Klient_ID) REFERENCES Klient(ID),
    FOREIGN KEY (Ofis_ID) REFERENCES Ofis(ID)
);

CREATE TABLE Plashtane (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Banka VARCHAR(100),
    Metod VARCHAR(50),
    Poryachka_ID INT UNIQUE,
    FOREIGN KEY (Poryachka_ID) REFERENCES Poryachka(ID)
);

CREATE TABLE Dostavka (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Adres_Dostavka VARCHAR(255),
    Data DATE,
    Klient_ID INT,
    Dostavchik_ID INT,
    Sklad_ID INT,
    FOREIGN KEY (Klient_ID) REFERENCES Klient(ID),
    FOREIGN KEY (Dostavchik_ID) REFERENCES Dostavchik(ID),
    FOREIGN KEY (Sklad_ID) REFERENCES Sklad(ID)
);

CREATE TABLE Poryachka_Produkt (
    Poryachka_ID INT,
    Produkt_ID INT,
    Kolichestvo INT DEFAULT 1,
    PRIMARY KEY (Poryachka_ID, Produkt_ID),
    FOREIGN KEY (Poryachka_ID) REFERENCES Poryachka(ID),
    FOREIGN KEY (Produkt_ID) REFERENCES Produkt(ID)
);

CREATE TABLE Sklad_Profil (
    Sklad_ID INT,
    Profil_ID INT,
    Nalichnost INT,
    PRIMARY KEY (Sklad_ID, Profil_ID),
    FOREIGN KEY (Sklad_ID) REFERENCES Sklad(ID),
    FOREIGN KEY (Profil_ID) REFERENCES Profil(ID)
);

CREATE TABLE Sklad_Mebel (
    Sklad_ID INT,
    Mebel_ID INT,
    Nalichnost INT,
    PRIMARY KEY (Sklad_ID, Mebel_ID),
    FOREIGN KEY (Sklad_ID) REFERENCES Sklad(ID),
    FOREIGN KEY (Mebel_ID) REFERENCES Mebel(ID)
);

CREATE TABLE Dostavchik_Mebel (
    Dostavchik_ID INT,
    Mebel_ID INT,
    PRIMARY KEY (Dostavchik_ID, Mebel_ID),
    FOREIGN KEY (Dostavchik_ID) REFERENCES Dostavchik(ID),
    FOREIGN KEY (Mebel_ID) REFERENCES Mebel(ID)
);

CREATE TABLE Dostavchik_Profil (
    Dostavchik_ID INT,
    Profil_ID INT,
    PRIMARY KEY (Dostavchik_ID, Profil_ID),
    FOREIGN KEY (Dostavchik_ID) REFERENCES Dostavchik(ID),
    FOREIGN KEY (Profil_ID) REFERENCES Profil(ID)
);

INSERT INTO Klient (Adres, Ime) VALUES
('Sofia, Blvd Bulgaria 1', 'Ivan Petrov'),
('Plovdiv, Main St 20', 'Maria Ivanova'),
('Varna, Sea Garden 5', 'Georgi Dimitrov'),
('Burgas, Port 10', 'Elena Stoyanova'),
('Ruse, Danube 3', 'Stefan Nikolov'),
('Sofia, Mladost 4', 'Kalina Vasileva'),
('Plovdiv, Kapana 8', 'Petar Iliev'),
('Varna, Center 12', 'Gergana Kirova'),
('Stara Zagora, Park 1', 'Dimitar Tanev'),
('Pleven, Square 2', 'Nadezhda Mihaylova');

INSERT INTO Potrebitel (Ime, Email, Telefon) VALUES
('Admin One', 'admin1@system.com', '0888111111'),
('Operator A', 'opA@system.com', '0888222222'),
('Operator B', 'opB@system.com', '0888333333'),
('Sales Manager', 'sales@system.com', '0888444444'),
('Logistics User', 'log@system.com', '0888555555'),
('Support 1', 'sup1@system.com', '0888666666'),
('Support 2', 'sup2@system.com', '0888777777'),
('Manager BG', 'man@system.com', '0888888888'),
('Audit User', 'audit@system.com', '0888999999'),
('System Bot', 'bot@system.com', '0000000000');

INSERT INTO Ofis (Otdel, Rakovoditel) VALUES
('Sales Sofia', 'Yordan Yordanov'),
('Logistics Plovdiv', 'Simeon Simeonov'),
('Support Varna', 'Teodora Toteva'),
('HR Sofia', 'Hristo Hristov'),
('IT Department', 'Iliya Iliev'),
('Marketing', 'Marina Marinova'),
('Accounting', 'Ana Anova'),
('Legal', 'Lazar Lazarov'),
('Export', 'Emil Emilov'),
('Import', 'Ivana Ivanova');

INSERT INTO Sklad (Mestopolozhenie, Kapacitet) VALUES
('Sofia North', 1000),
('Sofia South', 1500),
('Plovdiv Industrial', 2000),
('Varna Port', 3000),
('Burgas West', 1200),
('Ruse Logistics', 1000),
('Stara Zagora', 800),
('Pleven Storage', 900),
('Blagoevgrad', 500),
('Veliko Tarnovo', 750);

INSERT INTO Dostavchik (Raion, Zaplata) VALUES
('Sofia Center', 2500.00),
('Sofia Suburbs', 2400.00),
('Plovdiv Region', 2200.00),
('Varna City', 2300.00),
('Burgas Coast', 2300.00),
('North Bulgaria', 2100.00),
('South Bulgaria', 2100.00),
('West Region', 2000.00),
('East Region', 2250.00),
('Express Team', 3000.00);

INSERT INTO Produkt (Ime_Produkt, Cena) VALUES
('Luxury Sofa', 1200.00),
('Office Chair', 150.00),
('Wooden Table', 450.00),
('Bed Frame', 800.00),
('Mattress King', 600.00),
('Wardrobe 3-door', 950.00),
('Nightstand', 80.00),
('Desk Lamp', 45.00),
('Dining Chair', 90.00),
('Bookshelf', 200.00);

INSERT INTO Mebel (Kategoria) VALUES
('Living Room'), ('Bedroom'), ('Kitchen'), ('Office'), ('Garden'),
('Kids Room'), ('Bathroom'), ('Hallway'), ('Storage'), ('Decor');

INSERT INTO Profil (Material) VALUES
('Oak Wood'), ('Pine Wood'), ('MDF'), ('Plywood'), ('Steel'),
('Aluminum'), ('Glass'), ('Plastic'), ('Fabric'), ('Leather');

INSERT INTO Poryachka (Data, Klient_ID, Ofis_ID) VALUES
('2023-10-01', 1, 1),
('2023-10-02', 2, 1),
('2023-10-03', 3, 2),
('2023-10-04', 4, 3),
('2023-10-05', 5, 2),
('2023-10-06', 6, 1),
('2023-10-07', 7, 4),
('2023-10-08', 8, 3),
('2023-10-09', 9, 2),
('2023-10-10', 10, 1);

INSERT INTO Plashtane (Banka, Metod, Poryachka_ID) VALUES
('DSK Bank', 'Card', 1),
('UniCredit', 'Transfer', 2),
('Fibank', 'Cash', 3),
('Postbank', 'Card', 4),
('Revolut', 'Card', 5),
('DSK Bank', 'Cash', 6),
('UBB', 'Transfer', 7),
('Allianz', 'Card', 8),
('TBI', 'Credit', 9),
('EasyPay', 'Cash', 10);

INSERT INTO Dostavka (Adres_Dostavka, Data, Klient_ID, Dostavchik_ID, Sklad_ID) VALUES
('Sofia, Blvd Bulgaria 1', '2023-10-02', 1, 1, 1),
('Plovdiv, Main St 20', '2023-10-04', 2, 3, 3),
('Varna, Sea Garden 5', '2023-10-05', 3, 4, 4),
('Burgas, Port 10', '2023-10-06', 4, 5, 5),
('Ruse, Danube 3', '2023-10-07', 5, 6, 6),
('Sofia, Mladost 4', '2023-10-08', 6, 2, 2),
('Plovdiv, Kapana 8', '2023-10-09', 7, 3, 3),
('Varna, Center 12', '2023-10-10', 8, 4, 4),
('Stara Zagora, Park 1', '2023-10-11', 9, 7, 7),
('Pleven, Square 2', '2023-10-12', 10, 6, 8);

INSERT INTO Poryachka_Produkt (Poryachka_ID, Produkt_ID, Kolichestvo) VALUES
(1, 1, 1), (1, 2, 4),
(2, 3, 1),
(3, 4, 1), (3, 5, 1),
(4, 6, 2),
(5, 7, 2), (5, 8, 1),
(6, 9, 6),
(7, 10, 2),
(8, 1, 1),
(9, 2, 2),
(10, 3, 1);