package com.techelevator.JDBC;

import com.techelevator.DAO.ReservationDAO;
import com.techelevator.classes.Reservation;
import com.techelevator.classes.Space;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

public class JDBCReservationDAO implements ReservationDAO {

    private JdbcTemplate jdbcTemplate;

    public JDBCReservationDAO(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Reservation makeReservation(long space_id, int numberOfAttendees, LocalDate start_date, LocalDate end_date, String reserved_for) {

        //Creating a new reservation object and getting the next reservation Id to feed into the query.
        Reservation reservation = new Reservation();
        long nextReservationId = getNextReservationId();

        //Query to insert a new reservation to reservation table.
        String sql = "INSERT INTO reservation (reservation_id, space_id, number_of_attendees, start_date, end_date, reserved_for) VALUES (?,?,?,?,?,?)";
        jdbcTemplate.update(sql,nextReservationId, space_id,numberOfAttendees, start_date, end_date, reserved_for);

        //Query to display the newly added reservation and mapping result into a reservation object
        String sql2 = "SELECT * FROM reservation WHERE reservation_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql2, nextReservationId);
        while(results.next()){

            reservation = mapRowToReservation(results);
        }

        return reservation;

    }
    // method that maps results of an SQL Query to a reservation object
    private Reservation mapRowToReservation(SqlRowSet results) {

        Reservation reservation = new Reservation();
        reservation.setReservation_id(results.getLong("reservation_id"));
        reservation.setSpace_id(results.getInt("space_id"));
        reservation.setNumber_of_attendees(results.getInt("number_of_attendees"));
        reservation.setStart_date(results.getDate("start_date").toLocalDate());
        reservation.setEnd_date(results.getDate("end_date").toLocalDate());
        reservation.setReserved_for(results.getString("reserved_for"));

        return reservation;
    }

    //method that gets the next reservation Id from sequence table
    private long getNextReservationId() {

        SqlRowSet nextIdResult = jdbcTemplate.queryForRowSet("SELECT nextval('reservation_reservation_id_seq')");

        if (nextIdResult.next()) {

            return nextIdResult.getLong(1);

        } else {

            throw new RuntimeException("Something went wrong while getting an id for the new reservation");

        }
    }

}
