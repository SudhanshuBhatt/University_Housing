package com.univhousing.agreement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Scanner;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.main.Utils;

public class TerminationRequest_Relation {

	public String reason;
	public int terminationRequestNo;
	public String status;
	public Date terminationDate;
	public Date inspectionDate;
	public int personId;
	public int staffNo;

	Scanner inputObj = new Scanner(System.in);

	/**
	 * @param personId
	 * @throws ParseException
	 * @action Generates a new Termination request for a person id and Delete
	 *         the related data entries from respective tables
	 */
	public void generateLeaseTerminationRequest(int personId)
			throws ParseException {

		/*
		 * Write SQL Query to fetch the lease of the person id and generate a
		 * termination request thus deleting data from the tables that relates
		 * to person id
		 */
		int leaseNumber = 0;
		int terminationRequestNumber = 0;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;

		try {
			dbConnection = ConnectionUtils.getConnection();

			String SelectQuery1 = "SELECT T1.lease_no FROM person_accomodation_lease T1 WHERE "
					+ "LEASE_MOVE_IN_DATE=(SELECT MAX(LEASE_MOVE_IN_DATE) FROM PERSON_ACCOMODATION_LEASE T2 WHERE T1.Person_id=T2.person_id) "
					+ "AND T1.person_id =?";

			preparedStatement = dbConnection.prepareStatement(SelectQuery1);
			preparedStatement.setInt(1, personId);
			rs = preparedStatement.executeQuery();

			if (rs.isBeforeFirst()) {
				rs.next();
				leaseNumber = rs.getInt("lease_no");
			}

			rs.close();
			preparedStatement.close();

			String selectQuery2 = "SELECT MAX(termination_request_number) as termination_request_number FROM termination_requests";

			preparedStatement = dbConnection.prepareStatement(selectQuery2);
			rs = preparedStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				terminationRequestNumber = rs
						.getInt("termination_request_number") + 1;

			}

			rs.close();
			preparedStatement.close();

			System.out
					.println("Enter date you wnt to leave in MM/dd/YYYY format: ");
			String moveOutDate = inputObj.nextLine();

			java.sql.Date sqlMoveOutDate = Utils
					.convertStringToSQLDateFormat(moveOutDate);

			System.out
					.println("Enter the reason for leaving[Less than 50 characters]:");
			String reasonToMoveOut = inputObj.nextLine();

			System.out.println("Do you want to: \n" + "1. Submit\n"
					+ "2. Back ");

			String choice = inputObj.nextLine();	
			
			/* SELECTING STAFF NO BLOCK START */
			String query3 = "SELECT MAX(staff_no) AS LastStaff FROM Housing_Staff";
			int staffNoMax = 0;
			int staffNoMin = 0;
			try {
				preparedStatement = dbConnection.prepareStatement(query3);
				ResultSet max = preparedStatement.executeQuery();
				while (max.next()) {
					staffNoMax = max.getInt("LastStaff");
				}

				max.close();
				preparedStatement.close();
			} catch (SQLException e) {
				try {
					ConnectionUtils.closeConnection(dbConnection);
				} catch (SQLException e1) {

					e1.printStackTrace();
				}
				e.printStackTrace();
			}

			// PreparedStatement preparedStatement = null;
			// Connection conn1 = ConnectionUtils.getConnection();
			String query4 = "SELECT MIN(staff_no) AS FirstStaff FROM Housing_Staff";

			try {
				preparedStatement = dbConnection.prepareStatement(query4);
				ResultSet min = preparedStatement.executeQuery();
				while (min.next()) {
					staffNoMin = min.getInt("FirstStaff");
				}
			} catch (SQLException e) {
				try {
					ConnectionUtils.closeConnection(dbConnection);
				} catch (SQLException e1) {

					e1.printStackTrace();
				}
				e.printStackTrace();
			}

			// Getting a random staff number between First and the Last staff
			staffNo = Utils.randomizeStaff(staffNoMax, staffNoMin);

			/* SELECTING STAFF NI BLOCK END */

			//System.out.println(" Before marker");
			
			//System.out.println("After marker");

			if (choice.equals("1")) {

				String reqStatus = Constants.PENDING_STATUS;
				String selectQuery3 = "INSERT INTO Termination_Requests (REASON,termination_request_number,"
						+ "status,termination_date,inspection_date,person_id,staff_no) "
						+ "VALUES (?,?,?,?,?,?,?)";

				preparedStatement = dbConnection.prepareStatement(selectQuery3);
				preparedStatement.setString(1, reasonToMoveOut);
				preparedStatement.setInt(2, terminationRequestNumber);
				preparedStatement.setString(3, reqStatus);
				preparedStatement.setDate(4, sqlMoveOutDate);
				preparedStatement.setDate(5, sqlMoveOutDate);
				preparedStatement.setInt(6, personId);
				preparedStatement.setInt(7, staffNo);
				preparedStatement.executeQuery();

				System.out
						.println("Your request has been submitted for processing. Please note the request No:"
								+ terminationRequestNumber);
			} else if (choice.equals("2")) {
				System.out.println("Request Cancelled");

			}
		} catch (SQLException e1) {
			System.out.println("SQLException: " + e1.getMessage());
			System.out.println("VendorError: " + e1.getErrorCode());
		} catch (Exception e3) {
			System.out
					.println("General Exception Case. Printing stack trace below:\n");
			e3.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				dbConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
