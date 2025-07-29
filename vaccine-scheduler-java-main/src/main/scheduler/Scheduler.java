package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // While users don't enter quit, continue to process various inputs
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // create_patient <user> <password>
        // check 1: if the statement user entered is valid with length 3 including operation name
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        if(!passwordCheck(password)){
            System.out.println("Create patient failed, please use a strong password (8+ char, at least one upper and one lower, " +
                    "at least one letter and one number, and at least one special character, " +
                    "from \"!\", \"@\", \"#\", \"?\")");
            return;
        }

        // check 2: check if the username has been taken already
        if(usernameExistsPatients(username)) {
            System.out.println("Username taken, try again");
            return;
        }

        // If username hasn't been taken, generate hash and salt
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        // Create the patient
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            // Save the patient information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e){
            System.out.println("Create patient failed");
        }
    }

    private static boolean passwordCheck(String password){
        // Has upper and lower case letters
        if(password.length() < 8){
            return false;
        }
        if(!password.contains("!") && !password.contains("@") && !password.contains("#") && !password.contains("?")){
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumbers = false;
        boolean hasLetters = false;

        for(char c : password.toCharArray()){
            if(Character.isUpperCase(c)){
                hasUpper = true;
            }
            if(Character.isLowerCase(c)){
                hasLower = true;
            }
            if(Character.isDigit(c)){
                hasNumbers = true;
            }
            if(Character.isLetter(c)){
                hasLetters = true;
            }
            if(hasUpper && hasLower && hasNumbers && hasLetters){
                return true;
            }
        }
        return false;
    }

    // Check if username for patients exist in database
    // False if there are no rows when using SQL statements to filter out rows with username
    // True if username already exists
    private static boolean usernameExistsPatients(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            // Check if the statement user entered is valid or not with length 3
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }

        if(!passwordCheck(password)){
            System.out.println("Create caregiver failed, please use a strong password (8+ char, at least one upper and one lower, " +
                    "at least one letter and one number, " +
                    "and at least one special character, from \"!\", \"@\", \"#\", \"?\")");
            return;
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build(); 
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // Check if there is an account that is logged in right now
        if(currentPatient != null || currentCaregiver != null){
            System.out.println("User already logged in, try again");
            return;
        }

        // Check if the operation statement is valid
        if(tokens.length != 3){
            System.out.println("Login patient failed");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;

        // Check there is an account in the database
        // Return null if there isn't
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e){
            System.out.println("Login patient failed");
        }

        if(patient == null){
            System.out.println("Login patient failed");
        } else {
            System.out.println("Logged in as " + username);
            currentPatient = patient;
        }


    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
        }

        if (caregiver == null) {
            // check if login there is an account to log in in the database
            System.out.println("Login failed.");
        } else {
            // If yes, output this
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // Check 1: Make sure that someone is logged in
        if(currentPatient == null && currentCaregiver == null){
            System.out.println("Please login first");
            return;
        }
        // Check 2: If there are 2 tokens
        if(tokens.length != 2){
            System.out.println("Please try again");
            return;
        }

        String date = tokens[1];
        try {
            searchCaregiverScheduleMethod(date);
        } catch (IllegalArgumentException e) {
            // Check 3: If date input isn't valid
            System.out.println("Please try again");
            return;
        } catch (SQLException e){
            System.out.println("Please try again");
        }

        try {
            searchVaccineMethod();
        } catch (SQLException e) {
            System.out.println("Please try again");
        }

    }

    // Output the username for the caregivers that are available for the date ordered by username of caregiver
    private static void searchCaregiverScheduleMethod(String date) throws SQLException {
        // Create a connection manager
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        // Used to check if the date that the user inputted was correct - throw IllegalArgumentException if not correct
        Date d = Date.valueOf(date);

        // Create the query
        String searchSchedule = "SELECT a.Username FROM Availabilities AS a WHERE a.Time = ? ORDER BY a.Username;";
        try {
            // Place the input value in the parameter
            PreparedStatement statement = con.prepareStatement(searchSchedule);
            statement.setDate(1, d);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("Caregivers: ");

            if(resultSet.isBeforeFirst()){
                while(resultSet.next()){
                    System.out.println(resultSet.getString("Username"));
                }
            } else {
                System.out.println("No caregivers available");
            }
        } catch (SQLException e){
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    // Output the names of the vaccines and its number of doses
    private static void searchVaccineMethod() throws SQLException {
        // Create a connection manager
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String searchDoses = "SELECT v.Name, v.Doses FROM Vaccines AS v;";
        try {
            PreparedStatement statement = con.prepareStatement(searchDoses);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("Vaccines: ");
            // If no rows are returned, print that there isn't any vaccines available.
            if(resultSet.isBeforeFirst()){
                while(resultSet.next()){
                    System.out.println(resultSet.getString("Name") + " " +
                            resultSet.getString("Doses"));
                }
            } else {
                System.out.println("No vaccines available");
            }
        } catch (SQLException e){
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    private static void reserve(String[] tokens){
        // Only patients can perform this operation
        if(currentPatient == null && currentCaregiver != null){
            System.out.println("Please login as a patient");
            return;
        }

        if(currentPatient == null && currentCaregiver == null){
            System.out.println("Please login first");
            return;
        }

        if(tokens.length != 3){
            System.out.println("Please try again");
            return;
        }

        String date = tokens[1];
        String vaccine = tokens[2];

        try {
            reserveMethod(date, vaccine);

        } catch (IllegalArgumentException e) {
            System.out.println("Please try again");
        } catch (SQLException e){
            System.out.println("Please try again");
        }

    }

    private static void reserveMethod(String date, String vaccine) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        Date d = Date.valueOf(date);

        String caregiverQuery = "SELECT a.username FROM Availabilities AS a WHERE a.Time = ? ORDER BY a.Username LIMIT 1;";
        String vaccineQuery = "SELECT v.Doses FROM Vaccines AS v WHERE v.Name = ?;";
        String maxIDQuery = "SELECT MAX(appointment_id) FROM RESERVATIONS;";
        String reservationQuery = "INSERT INTO Reservations VALUES (?,?,?,?,?)";
        String availabilityQuery = "DELETE FROM Availabilities WHERE username = ? AND Time = ?;";
        String caregiverCheck = "SELECT * FROM Reservations WHERE C_username = ? AND Time = ?;";
        try {
            // Caregiver
            PreparedStatement caregiverAvailable = con.prepareStatement(caregiverQuery);
            caregiverAvailable.setDate(1, d);
            ResultSet resultSet = caregiverAvailable.executeQuery();

            // Vaccines
            PreparedStatement vaccineDoses = con.prepareStatement(vaccineQuery);
            vaccineDoses.setString(1, vaccine);
            ResultSet vaccinesAvailable = vaccineDoses.executeQuery();

            if(!resultSet.next()){
                System.out.println("No caregiver is available");
                return;

            }

            if(!vaccinesAvailable.next() || vaccinesAvailable.getInt("Doses") == 0){
                System.out.println("Not enough available doses");
                return;
            }

            String username = resultSet.getString("Username");


            // Check if on that that day, there is a reservation with that caregiver name
            PreparedStatement caregiverCheckQuery = con.prepareStatement(caregiverCheck);
            caregiverCheckQuery.setString(1, username);
            caregiverCheckQuery.setDate(2, d);
            ResultSet checkResultSet = caregiverCheckQuery.executeQuery();

            if(checkResultSet.isBeforeFirst()){
                System.out.println("Please try again");
                return;
            }

            PreparedStatement maxID = con.prepareStatement(maxIDQuery);
            ResultSet resultSetID = maxID.executeQuery();
            int appointmentID = 1;
            if(resultSetID.next()){
                appointmentID += resultSetID.getInt("MAX(appointment_id)");
            }

            // Add to reservation
            PreparedStatement reservation = con.prepareStatement(reservationQuery);
            reservation.setDate(1, d);
            reservation.setString(2, username);
            reservation.setString(3, currentPatient.getUsername());
            reservation.setString(4, vaccine);
            reservation.setInt(5, appointmentID);
            reservation.executeUpdate();

            // Remove availabilities
            PreparedStatement removeAvailability = con.prepareStatement(availabilityQuery);
            removeAvailability.setString(1, username);
            removeAvailability.setDate(2, d);
            removeAvailability.executeUpdate();

            // Print out line
            System.out.println("Appointment ID " + appointmentID + ", Caregiver username " +
                    username);

        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }

        // Decrement vaccine doses
        Vaccine v = new Vaccine.VaccineGetter(vaccine).get();
        v.decreaseAvailableDoses(1);

    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
        }
    }

    private static void cancel(String[] tokens) {
        // Both patients and caregivers can perform this operation
        // If the appointment id is valid

        // Check if a patient or a caregiver is logged in
        if(currentPatient == null && currentCaregiver == null){
            System.out.println("Please login first");
            return;
        }

        if(tokens.length != 2){
            System.out.println("Please try again");
            return;
        }

        try {
            int id = Integer.parseInt(tokens[1]);
            cancelMethod(id);
        } catch (NumberFormatException | SQLException e) {
            System.out.println("Please try again");
        }

    }

    private static void cancelMethod(int id) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String vaccineName = null;
        String caregiverName = null;
        Date date = null;
        // Check if the id is on the reservations table
        String idQuery = "SELECT * FROM Reservations WHERE appointment_id = ?;";
        String removeReservation = "DELETE FROM Reservations WHERE appointment_id = ?;";
        String updateCaregiver = "INSERT INTO Availabilities VALUES (?,?);";

        try {
            PreparedStatement idCheck = con.prepareStatement(idQuery);
            idCheck.setInt(1, id);
            ResultSet idResultSet = idCheck.executeQuery();

            // If appointment id doesn't exist
            if(!idResultSet.isBeforeFirst()){
                System.out.println("Appointment ID " + id + " does not exist");
                return;
            }

            // Get the vaccine name
            vaccineName = idResultSet.getString("Vaccine_name");
            // Get the caregiver name
            caregiverName = idResultSet.getString("C_username");
            // Get the date
            date = idResultSet.getDate("Time");

            // Update the availability of caregiver
            PreparedStatement updateAvailability = con.prepareStatement(updateCaregiver);
            updateAvailability.setDate(1, date);
            updateAvailability.setString(2, caregiverName);
            updateAvailability.executeUpdate();

            // Remove the tuple from Reservations
            PreparedStatement reservationRemove = con.prepareStatement(removeReservation);
            reservationRemove.setInt(1, id);
            reservationRemove.executeUpdate();

            System.out.println("Appointment ID " + id + " has been successfully canceled");

        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
        // Update the availability of vaccine
        Vaccine v = new Vaccine.VaccineGetter(vaccineName).get();
        v.increaseAvailableDoses(1);

    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        if(currentPatient == null && currentCaregiver == null){
            System.out.println("Please login first");
            return;
        }

        if(tokens.length != 1){
            System.out.println("Please try again");
            return;
        }

        try {
            showAppointmentsMethod();
        } catch (SQLException e) {
            System.out.println("Please try again");
        }

    }

    private static void showAppointmentsMethod() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        // Write the queries for patients
        String patientQuery = "SELECT r.appointment_id AS id, r.Vaccine_name AS vaccine, r.Time AS date, " +
                "r.C_username AS caregiver FROM Reservations AS r WHERE r.P_username = ? ORDER BY r.appointment_id ASC;";
        String caregiverQuery = "SELECT r.appointment_id AS id, r.Vaccine_name AS vaccine, r.Time AS date, " +
                "r.P_username AS patient FROM Reservations AS r WHERE r.C_username = ? ORDER BY r.appointment_id ASC;";
        if(currentPatient != null){
            try {
                PreparedStatement patientAppointments = con.prepareStatement(patientQuery);
                patientAppointments.setString(1, currentPatient.getUsername());
                ResultSet resultSet = patientAppointments.executeQuery();

                if(!resultSet.isBeforeFirst()){
                    System.out.println("No appointments scheduled");
                } else {
                    while(resultSet.next()){
                        int id = resultSet.getInt("id");
                        String vaccine = resultSet.getString("vaccine");
                        String date = resultSet.getDate("date").toString();
                        String caregiver = resultSet.getString("caregiver");
                        System.out.println(id + " " + vaccine + " " + date + " " + caregiver);
                    }
                }
            } catch (SQLException e) {
                throw new SQLException(e);
            } finally {
                cm.closeConnection();
            }
        } else {
            try {
                PreparedStatement caregiverAppointments = con.prepareStatement(caregiverQuery);
                caregiverAppointments.setString(1, currentCaregiver.getUsername());
                ResultSet resultSet = caregiverAppointments.executeQuery();

                if(!resultSet.isBeforeFirst()){
                    System.out.println("No appointments scheduled");
                } else {
                    while(resultSet.next()){
                        int id = resultSet.getInt("id");
                        String vaccine = resultSet.getString("vaccine");
                        String date = resultSet.getDate("date").toString();
                        String patient = resultSet.getString("patient");
                        System.out.println(id + " " + vaccine + " " + date + " " + patient);
                    }
                }
            } catch (SQLException e) {
                throw new SQLException(e);
            } finally {
                cm.closeConnection();
            }
        }
    }
    private static void logout(String[] tokens) {
        // Other errors: "Please try again"
        if(tokens.length != 1){
            System.out.println("Please try again");
            return;
        }

        // If not logged in, print "Please log in first"
        if(currentCaregiver == null && currentPatient == null){
            System.out.println("Please login first");
            return;
        }

        // Otherwise, print "Successfully logged out"
        if(currentCaregiver != null){
            currentCaregiver = null;
        } else {
            currentPatient = null;
        }
        System.out.println("Successfully logged out");
    }
}
