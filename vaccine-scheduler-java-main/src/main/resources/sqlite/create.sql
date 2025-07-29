CREATE TABLE Caregivers (
    -- Each caregiver can be assigned to many reservations but each reservation
    -- is assigned to exactly one caregiver.
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    -- Weak entity set since it depends on the primary of key of caregivers
    -- to define itself.
    Time date,
    Username varchar(255) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Caregivers(Username) ON DELETE CASCADE,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    -- Each vaccine can be given to many reservation
    -- but each reservation can be linked to exactly one vaccine.
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patients (
    -- Each patient can book many reservation but each reservation must be booked by exactly one patient.
    Username varchar(255),


    PRIMARY KEY (Username)
);

CREATE TABLE Reservations (
    Time date,
    C_username varchar(255) NOT NULL,
    P_username varchar(255) NOT NULL,
    Vaccine_name varchar(255) NOT NULL,
    appointment_id int,
    -- Declare foreign keys
    FOREIGN KEY (C_username) REFERENCES Caregivers(Username) ON DELETE CASCADE,
    FOREIGN KEY (P_username) REFERENCES Patients(Username) ON DELETE CASCADE,
    FOREIGN KEY (Vaccine_name) REFERENCES Vaccines(Name) ON DELETE CASCADE,
    -- Declare primary key
    -- Since all of these attributes is referenced from another table
    -- and declared as a primary key, the other tables have a one-to-many relationship with this table.
    -- Each reservation is linked to one date, caregiver username, vaccine name and patient username.
    PRIMARY KEY (appointment_id)
);



