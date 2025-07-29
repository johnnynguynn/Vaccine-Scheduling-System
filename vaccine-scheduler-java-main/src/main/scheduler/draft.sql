SELECT v.Name, v.Doses
FROM Vaccines AS v;


        -- Caregiver can only see a maximum of one patient per day (Remove the caregiver availability)

        -- If there are available caregivers on that day, choose by alphabetical order
        --  and print appointment id and caregiver username

        SELECT a.Username
        FROM Availabilities AS a
        WHERE
        a.Time = ?
        ORDER BY a.Username;


        -- Output the scheduled appointments for the current user
        SELECT r.appointment_id AS id, r.Vaccine_name AS vaccine, r.Time AS date, r.P_username AS patient
        FROM Reservations AS r
        WHERE
        r.C_username = ?
        ORDER BY r.appointment_id ASC;



