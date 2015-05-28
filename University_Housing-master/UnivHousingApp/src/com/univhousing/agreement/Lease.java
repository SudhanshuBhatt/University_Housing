package com.univhousing.agreement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.users.Guest;


public class Lease {

	public int leaseNo;
	public float deposit;
	public String modeOfPayment;
	public int duration;
	public Date cutOffDate;
	Scanner inputObj = new Scanner(System.in);
	Guest guestObj = new Guest();

	/**
	 * @param personId
	 * @param leaseNumber
	 * @action Display the lease details of a particular lease number
	 *         corresponding to a person id
	 */
	public void displayLeaseDetails(int personId, Integer leaseNumber) {

		System.out.println("MARKER HERE 123");
		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		try {

			dbConnection = ConnectionUtils.getConnection();

			/*String selectQuery = "SELECT T1_person.first_name, T2_lease_person.lease_no, T3_lease.duration, T4_Student.Student_Type, "
					+ " T2_lease_person.Lease_Move_In_Date, T2_lease_person.accomodation_type, T2_lease_person.accomodation_id "
					+ "FROM PERSON T1_person, PERSON_ACCOMODATION_LEASE_HIST T2_lease_person,LEASE T3_lease, STUDENT T4_Student "
					+ "WHERE T1_person.person_id = ? AND T2_lease_person.lease_no=? "
					+ "AND T1_person.person_id = T2_lease_person.person_id "
					+ "AND	T3_lease.lease_no = T2_lease_person.lease_no "
					+ "AND T1_person.person_id = T4_Student.person_id";*/
			
		String selectQuery = "";
		
		if(guestObj.checkPersonIsGuest(personId))
		{
			selectQuery = "select p.first_name,pal_outer.lease_no,l.duration,'Visitor' as category,pal_outer.lease_move_in_date," +
					"pal_outer.accomodation_type,pal_outer.accomodation_id "
			+" FROM PERSON p, PERSON_ACCOMODATION_LEASE_HIST pal_outer,LEASE l "
			+" WHERE p.person_id = ? "
			+" and pal_outer.lease_no = l.lease_no "
			+" and p.person_id = pal_outer.person_id ";
			
		}
		else
		{
			selectQuery = "select p.first_name,pal_outer.lease_no,l.duration,s.student_type as category,pal_outer.lease_move_in_date," +
					" pal_outer.accomodation_type,pal_outer.accomodation_id "
					+" FROM PERSON p, PERSON_ACCOMODATION_LEASE_HIST pal_outer,LEASE l,student s "
					+" WHERE p.person_id = ? "
				+" and pal_outer.lease_no = l.lease_no "
				+" and p.person_id = pal_outer.person_id "
				+" and pal_outer.person_id = s.person_id ";
				
		}
		
		
			 
		System.out.println("selectQuery executing:"+selectQuery);

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, personId);
		//	preparedStatement.setInt(2, leaseNumber);
			rs = preparedStatement.executeQuery();

			System.out.println(String.format(
					"%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s",
					"NAME", "LEASE NO", "Duration", "Category",
					"Housing Type", "Move in Date", "Place No", "Room No",
					"Street Name", "City", "Postcode"));

			System.out
					.println("------------------------------------------------------------------------------------------------"
							+ "-------------------------------------------------------");
			String accomodationType = "";
			int accomodationId = 0;
			if (rs.isBeforeFirst()) {

				rs.next();
				accomodationType = rs.getString("accomodation_type");
				accomodationId = rs.getInt("accomodation_id");
				System.out.print(String.format("%-15s", rs
						.getString("first_name")));
				System.out.print(String.format("%-15s", rs
						.getString("lease_no")));
				System.out.print(String.format("%-15s", rs
						.getString("duration")));
				System.out.print(String.format("%-15s", rs
						.getString("category")));
				System.out.print(String.format("%-15s", accomodationType));

				System.out.print(String.format("%-15s", rs
						.getDate("Lease_Move_In_Date")));

			}

			/*
			 * 
			 * Get Accommodation type from Result-statement and Compare it with
			 * Hard-coded "Apartment"/ "Family apartment"/"Residence Hall "
			 * Strings. This way get into one of the three If loops and print
			 * the relevant pa=lace# room # information about them.
			 */

			rs.close();
			preparedStatement.close();

			if (accomodationType.equals(Constants.GENERAL_APARTMENT)) {

				/*
				 * Go to General Apartment Table( and from there to the bedroom
				 * table to fetch room #) and fetch Place # Room # and Address
				 * atributes
				 */

				String selectQuery1 = "SELECT B.bedroom_place_no,B.room_no, GA.street_name,GA.city_name,GA.zip_code "
						+ "FROM General_Apartment GA, bedroom B "
						+ "WHERE B.accomodation_id = ? AND GA.apt_no = B.apt_no ";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {
					rs.next();

					System.out.println(String.format(
							"%-15s%-10s%-20s%-15s%-15s", rs
									.getInt("bedroom_place_no"), rs
									.getInt("room_no"), rs
									.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else if (accomodationType.equals(Constants.FAMILY_APARTMENT)) {

				/*
				 * Go to Family Apartment Table and fetch Place # Room # and
				 * Address atributes (no need to go to bedroom/room table as we
				 * are not concerned) as to what goes on inside a FAMILY
				 * APARTMENT
				 */

				String selectQuery1 = "SELECT F.apt_no,F.street_name,F.city_name,F.zip_code "
						+ "FROM Family_Apartment F WHERE F.accomodation_id=?";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {

					rs.next();
					System.out.println(String.format(
							"%-15s%-15s%-15s%-15s%-15s", rs.getInt("apt_no"),
							"-NA-", rs.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else if (accomodationType.equals(Constants.RESIDENCE_HALL)) {

				/*
				 * Go to Residence Hall Table[FOllow like 1st i condition] and
				 * fetch Place # Room # and Address atributes
				 */
				String selectQuery1 = "SELECT RHPM.residence_place_no,RHPM.room_no, HR.street_name,HR.city_name,HR.zip_code "
						+ "FROM residence_hall HR, residence_hall_provides_room RHPM "
						+ "WHERE RHPM.accomodation_id = ? AND HR.hall_number = RHPM.hall_number ";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {
					rs.next();

					System.out.println(String.format(
							"%-15s%-15s%-15s%-15s%-15s", rs
									.getInt("residence_place_no"), rs
									.getInt("room_no"), rs
									.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else {
				System.out
						.println("Error fetching the Residence Related Details. Please Contact the Admin");
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

	public void displayListOfLeasesForAPerson(int personId,
			ArrayList<Integer> leaseNumbers) throws SQLException {

		/*
		 * ResultSet listOfLeases = null; leaseNumbers.clear();
		 * while(listOfLeases.next()) {
		 * leaseNumbers.add(listOfLeases.getInt("lease_no")); }
		 */

		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		try {

			dbConnection = ConnectionUtils.getConnection();

//			String selectQuery = "SELECT * FROM person_accomodation_lease P  WHERE P.person_id=?";
			
			String selectQuery = "select pal.lease_no,pal.lease_move_in_date,"
			+" add_months(pal.lease_move_in_date,l.duration) end_date "
			+" from person_accomodation_lease_hist pal,lease_hist l "
			+" where pal.person_id = ? "
			+" and pal.lease_no = l.lease_no ";


			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, personId);
			rs = preparedStatement.executeQuery();

			if (rs.isBeforeFirst()) {
				System.out
						.println("============================================");
				System.out.println("Sr# Lease#");
				System.out
						.println("============================================");
			}

			leaseNumbers.clear();
			int count = 1;

			while (rs.next()) {

				leaseNumbers.add(rs.getInt("lease_no"));
				System.out.println(count+". "+"LEASE NO:"+rs.getInt("lease_no")+" PERIOD:: (START:"+rs.getDate("lease_move_in_date")+" END:"+rs.getDate("end_date")+")");
				count++;

			}
			if(leaseNumbers.size()==0)
			{
				System.out.println("No former leases");
				return;
			}
				
//			int count = 1;
			/*for (Integer item : leaseNumbers) {
				System.out.println(count + ". " + item.intValue());
				count++;
			}*/
			count = 0;
			System.out.println("0. Back\n");
			int choice = inputObj.nextInt();
			if (choice == 0)
				return;
			else {
				if (leaseNumbers.size() > 0)
					displayLeaseDetails(personId, leaseNumbers.get(choice - 1));
				else {
					System.out.println("No former leases");
					return;
				}
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

	/***********************************************************************************************
	 * @param personId
	 * @action Displays lease number, duration of the lease, name of student,
	 *         matriculation number of student, place number, room number, Hall
	 *         address or Student apartment address, Date of moving in and if
	 *         present date of leaving the room
	 ***********************************************************************************************/
	public void displayCurrentLease(int personId) {

		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String accomodationType = "";
		int accomodationId = -1;

		try {
			dbConnection = ConnectionUtils.getConnection();

			String selectQuery = "";
			
			if(guestObj.checkPersonIsGuest(personId))
			{
				selectQuery = "select p.first_name,pal_outer.lease_no,l.duration,'Visitor' as category,pal_outer.lease_move_in_date," +
						"pal_outer.accomodation_type,pal_outer.accomodation_id "
				+" FROM PERSON p, PERSON_ACCOMODATION_LEASE pal_outer,LEASE l "
				+" WHERE p.person_id = ? "
				+" and pal_outer.lease_no = l.lease_no "
				+" and p.person_id = pal_outer.person_id "
				+" and pal_outer.lease_move_in_date=(select max(lease_move_in_date) from person_accomodation_lease pal_inner "
				+" where pal_inner.person_id = pal_outer.person_id) ";
			}
			else
			{
				selectQuery = "select p.first_name,pal_outer.lease_no,l.duration,s.student_type as category,pal_outer.lease_move_in_date," +
						" pal_outer.accomodation_type,pal_outer.accomodation_id "
						+" FROM PERSON p, PERSON_ACCOMODATION_LEASE pal_outer,LEASE l,student s "
						+" WHERE p.person_id = ? "
					+" and pal_outer.lease_no = l.lease_no "
					+" and p.person_id = pal_outer.person_id "
					+" and pal_outer.person_id = s.person_id "
					+" and pal_outer.lease_move_in_date=(select max(lease_move_in_date) from person_accomodation_lease pal_inner "
					+" where pal_inner.person_id = pal_outer.person_id) ";
			}
				 
			
			//-----------
			/*String selectQuery = "SELECT T1_person.first_name, T2_lease_person.lease_no, T3_lease.duration, T4_Student.Student_Type, "
					+ " T2_lease_person.Lease_Move_In_Date, T2_lease_person.accomodation_type, T2_lease_person.accomodation_id "
					+ "FROM PERSON T1_person, PERSON_ACCOMODATION_LEASE T2_lease_person,LEASE T3_lease, STUDENT T4_Student "
					+ "WHERE T1_person.person_id = ? AND LEASE_MOVE_IN_DATE=(SELECT MAX(LEASE_MOVE_IN_DATE) "
					+ "FROM PERSON_ACCOMODATION_LEASE inner_P WHERE T2_lease_person.person_id = inner_P.person_id) "
					+ "AND T1_person.person_id = T2_lease_person.person_id "
					+ "AND	T3_lease.lease_no = T2_lease_person.lease_no "
					+ "AND T1_person.person_id = T4_Student.person_id";*/

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, personId);

			rs = preparedStatement.executeQuery();

			System.out.println(String.format(
					"%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s",
					"NAME", "LEASE NO", "Duration", "Category",
					"Housing Type", "Move in Date", "Place No", "Room No",
					"Street Name", "City", "Postcode"));

			System.out
					.println("------------------------------------------------------------------------------------------------"
							+ "-------------------------------------------------------");

			if (rs.isBeforeFirst()) {

				rs.next();
				accomodationType = rs.getString("accomodation_type");
				accomodationId = rs.getInt("accomodation_id");
				System.out.print(String.format("%-15s", rs
						.getString("first_name")));
				System.out.print(String.format("%-15s", rs
						.getString("lease_no")));
				System.out.print(String.format("%-15s", rs
						.getString("duration")));
				System.out.print(String.format("%-15s", rs
						.getString("category")));
				System.out.print(String.format("%-15s", accomodationType));
				System.out.print(String.format("%-15s", rs
						.getDate("Lease_Move_In_Date")));

			}

			/*
			 * Get Accommodation type from Resultstatement and Compare it with
			 * HArdcoded "Apartment"/ "Family apartment"/"Residence Hall "
			 * Strings. This way get into one of the three If loops and print
			 * the relevant pa=lace# room # information about them.
			 */

			/*
			 * Closing RS and Preparedstatement -- because --> I want to reuse
			 * the rs for next queries in the same module
			 */

			rs.close();
			preparedStatement.close();

			if (accomodationType.equals(Constants.GENERAL_APARTMENT)) {

				/*
				 * Go to General Apartment Table( and from there to the bedroom
				 * table to fetch room #) and fetch Place # Room # and Address
				 * atributes
				 */

				String selectQuery1 = "SELECT B.bedroom_place_no,B.room_no, GA.street_name,GA.city_name,GA.zip_code "
						+ "FROM General_Apartment GA, bedroom B "
						+ "WHERE B.accomodation_id = ? AND GA.apt_no = B.apt_no";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {
					rs.next();

					System.out.println(String.format(
							"%-15s%-10s%-20s%-15s%-15s", rs
									.getInt("bedroom_place_no"), rs
									.getInt("room_no"), rs
									.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else if (accomodationType.equals(Constants.FAMILY_APARTMENT)) {

				/*
				 * Go to Family Apartment Table and fetch Place # Room # and
				 * Address atributes (no need to go to bedroom/room table as we
				 * are not concerned) as to what goes on inside a FAMILY
				 * APARTMENT
				 */

				String selectQuery1 = "SELECT F.apt_no,F.street_name,F.city_name,F.zip_code "
						+ "FROM Family_Apartment F WHERE F.accomodation_id=?";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {

					rs.next();
					System.out.println(String.format(
							"%-15s%-15s%-15s%-15s%-15s", rs.getInt("apt_no"),
							"-NA-", rs.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else if (accomodationType.equals(Constants.RESIDENCE_HALL)) {

				/*
				 * Go to Residence Hall Table[FOllow like 1st i condition] and
				 * fetch Place # Room # and Address atributes
				 */
				String selectQuery1 = "SELECT RHPM.residence_place_no,RHPM.room_no, HR.street_name,HR.city_name,HR.zip_code "
						+ "FROM residence_hall HR, residence_hall_provides_room RHPM "
						+ "WHERE RHPM.accomodation_id = ? AND HR.hall_number = RHPM.hall_number ";

				preparedStatement = dbConnection.prepareStatement(selectQuery1);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();

				if (rs.isBeforeFirst()) {
					rs.next();

					System.out.println(String.format(
							"%-15s%-15s%-15s%-15s%-15s", rs
									.getInt("residence_place_no"), rs
									.getInt("room_no"), rs
									.getString("street_name"), rs
									.getString("city_name"), rs
									.getInt("zip_code")));

				}

			} else {
				System.out
						.println("No records found. Please Contact the Admin");
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

	/**
	 * @param personId
	 * @throws SQLException
	 * @action Displays all the requests made by the student along with their
	 *         status and request number
	 */
	public boolean viewAllRequests(int personId) throws SQLException {
		ResultSet viewRequestsSet = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		boolean flag = false;
		

		try {

			dbConnection = ConnectionUtils.getConnection();

			/* Create and insert SQL Query here */

			String selectQuery = "SELECT T.termination_request_number, T.status "
					+ "FROM termination_requests T " + "WHERE T.person_id = ? ";

			String selectQuery1 = "SELECT P.application_request_no, P.request_status "
					+ "FROM PERSON_ACC_STAFF P " + "WHERE P.person_id = ?";

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, personId);

			viewRequestsSet = preparedStatement.executeQuery();
			
			if (!viewRequestsSet.isBeforeFirst()) {
				System.out.println("No Termination requests Found");
			} 
			System.out
			.println("==================================================================");
			System.out.println(String.format("%-15s%-20s%-15s",
					"Request type", "Request number", "Status"));
			System.out
					.println("==================================================================");

			while (viewRequestsSet.next()) {
				flag = true;
				System.out.print(String.format("%-15s%-20s%-15s",
						"Termination", viewRequestsSet
								.getInt("termination_request_number"),
						viewRequestsSet.getString("status")));
				System.out.println("");

				/*
				 * Write an SQL Query for fetching details of each value of
				 * requestNumber variable
				 */
			}
			preparedStatement.close();
			//viewRequestsSet.close();

			preparedStatement = dbConnection.prepareStatement(selectQuery1);
			preparedStatement.setInt(1, personId);
			ResultSet viewAppReq = null;
			viewAppReq = preparedStatement.executeQuery();
			// System.out.println("Request Number" + "\t Status");
			if (!viewAppReq.isBeforeFirst()) {
				System.out.println("No Lease requests Found");
			} 
			while (viewAppReq.next()) {
				flag = true;
				System.out.print(String.format("%-15s%-20s%-15s", "Lease",
						viewAppReq.getInt("application_request_no"),
						viewAppReq.getString("request_status")));

				System.out.println("");
			}
			
			
			
		} catch (SQLException e1) {
			System.out.println("SQLException: " + e1.getMessage());
			System.out.println("Vendor Error: " + e1.getErrorCode());
		} catch (Exception e) {
			System.out.println("Geneal Error. Please see stack trace: ");
			e.printStackTrace();
		} finally {
			try {
				viewRequestsSet.close();
				dbConnection.close();
				preparedStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * @param personId
	 * @param requestNumber
	 * 
	 * @throws SQLException
	 * @action Deletes the requestNumber for the personID
	 */
	public boolean cancelRequest(int personId, int requestNumber,
			String requestType) throws SQLException {

		// viewAllRequests(personId);

		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		boolean isCancelSuccessful = false;
		/*
		 * Write SQL Query to delete the requestNumber mentioned by the PersonId
		 * and set the status as "Cancelled"
		 */
		try {
			if (requestType.equalsIgnoreCase(Constants.T)) {
				dbConnection = ConnectionUtils.getConnection();

				/*
				 * First check if the entry is in the termination table.
				 */
				String selectQuery = "SELECT * "
						+ "FROM Termination_Requests T "
						+ "WHERE T.person_id = ? AND T.termination_request_number = ?";

				preparedStatement = dbConnection.prepareStatement(selectQuery);
				preparedStatement.setInt(1, personId);
				preparedStatement.setInt(2, requestNumber);

				rs = preparedStatement.executeQuery();

				/*
				 * Check if the entry is in the Termination_requests table or
				 * the StudentParkingSpot_Relation table.
				 */
				if (rs.next()) {
					String updateQuery = "UPDATE Termination_Requests "
							+ "SET status = ? "
							+ "WHERE person_id = ? AND termination_request_number = ?";

					// System.out.println(rs.getInt("termination_request_number"));
					rs.close();
					preparedStatement.close();

					preparedStatement = dbConnection
							.prepareStatement(updateQuery);
					preparedStatement.setString(1, Constants.STATUS_CANCELED);
					preparedStatement.setInt(2, personId);
					preparedStatement.setInt(3, requestNumber);

					int update = preparedStatement.executeUpdate();
					// System.out.println("Update returned " + update);

					System.out.println("The request # " + requestNumber
							+ " is cancelled!");

					isCancelSuccessful = true;
					preparedStatement.close();
					rs.close();

				}
			} else if (requestType.equalsIgnoreCase(Constants.L)) {

				/*
				 * Since the entry is not in the termination table, it must be
				 * in the PERSON_ACC_STAFF table
				 */
				ResultSet rs1 = null;
				Connection dbConnection1 = ConnectionUtils.getConnection();
				PreparedStatement preparedStatement1 = null;

				String selectQuery1 = "SELECT * "
						+ "FROM PERSON_ACC_STAFF P "
						+ "WHERE P.person_id = ? AND p.application_request_no = ?";

				preparedStatement1 = dbConnection1.prepareStatement(selectQuery1);
				preparedStatement1.setInt(1, personId);
				preparedStatement1.setInt(2, requestNumber);

				rs1 = preparedStatement1.executeQuery();

				// System.out.println("Executed with values: " + personId +
				// " and " + requestNumber);

				if (rs1.next()) {

					/*
					 * 
					 * If the request id is in the PERSON_ACC_STAFF table update
					 * the status in this table to CANCELED
					 * 
					 * If the request id is in the parking table, get the
					 * student id and update the table. Get the student id first
					 */
					// System.out.println("Executed with values: " + personId +
					// " and " + requestNumber);
					// preparedStatement.close();
					// rs.close();
					/*
					 * Update the status with CANCELED
					 */
					String updateQuery1 = "UPDATE PERSON_ACC_STAFF "
							+ "SET request_status = ? "
							+ "WHERE person_id = ? AND application_request_no = ?";

					preparedStatement1.close();
					preparedStatement1 = dbConnection1
							.prepareStatement(updateQuery1);
					preparedStatement1.setString(1, Constants.STATUS_CANCELED);
					preparedStatement1.setInt(2, personId);
					preparedStatement1.setInt(3, requestNumber);
					int update = preparedStatement1.executeUpdate();
					isCancelSuccessful = true;
				} else {
						isCancelSuccessful = false;
					/*
					 * The value is not present in the parking table either
					 * Print an error message saying that the user needs to
					 * check
					 */
					System.out.println("Please enter a valid request number");
				}
			}
			return isCancelSuccessful;
		} catch (SQLException e1) {
			System.out.println("SQLException: " + e1.getMessage());
			System.out.println("Vendor Error: " + e1.getErrorCode());
		} catch (Exception e) {
			System.out.println("Geneal Error. Please see stack trace: ");
			e.printStackTrace();
		} finally {
			return isCancelSuccessful;
		}

	}

	/**
	 * @action Pulls up all the accommodations that are vacant right now i.e.
	 *         not occupied by any student or family
	 * @throws SQLException
	 */
	public void viewAccomodationVacancies() throws SQLException {
		/* Write SQL Query to pull up all the vacancies to display the student */

		/*
		 * Method for viewing all the vacancies. I am assuming that the
		 * accommodation ID is unique for bedroom, residence hall room and
		 * Family Apartment. Using the person_id, we can index into
		 * person_acc_lease table and get the accommodation id, and query the
		 * three tables to find entries that are not present in lease.
		 */
		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		try {
			dbConnection = ConnectionUtils.getConnection();

			/*
			 * Query for getting count of free bedrooms Assuming that
			 * accommodation_id is unique
			 */
			String selectQueryBedroom = "SELECT COUNT (B.accomodation_id) AS rooms "
					+ "FROM bedroom B "
					+ "WHERE B.accomodation_id  NOT IN "
					+ "(SELECT accomodation_id "
					+ "FROM person_accomodation_lease)";
			preparedStatement = dbConnection
					.prepareStatement(selectQueryBedroom);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				System.out.println("Available vacancies in General "
						+ "Apartments: " + rs.getInt("rooms") + " rooms");
			}
			rs.close();
			preparedStatement.close();
			/*
			 * Query for getting count of free family apartments. Assuming
			 * accommodation_id is unique.
			 */
			String selectQueryFamilyApt = "SELECT COUNT (F.accomodation_id) AS apartments "
					+ "FROM Family_Apartment F "
					+ "WHERE F.accomodation_id NOT IN "
					+ "(SELECT accomodation_id "
					+ "FROM person_accomodation_lease)";
			preparedStatement = dbConnection
					.prepareStatement(selectQueryFamilyApt);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				System.out.println("Available vacancies in Family "
						+ "Apartments: " + rs.getInt("apartments")
						+ " apartments");
			}

			/*
			 * Query for getting count of free residence halls. Exactly same as
			 * the previous two queries.
			 */
			String selectQueryResidence = "SELECT COUNT (R.accomodation_id) AS residence "
					+ "FROM residence_hall_provides_room R "
					+ "WHERE R.accomodation_id NOT IN "
					+ "(SELECT accomodation_id "
					+ "FROM person_accomodation_lease)";
			preparedStatement = dbConnection
					.prepareStatement(selectQueryResidence);
			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				System.out.println("Available rooms in Residence Hall: "
						+ rs.getInt("residence") + " rooms");
			}
		} catch (SQLException s) {
			System.out.println("SQLException: " + s.getMessage());
			System.out.println("Vendor Error: " + s.getErrorCode());
		} catch (Exception e) {
			System.out
					.println("General exception case. Please see stack trace");
			e.printStackTrace();
		} finally {
			rs.close();
			dbConnection.close();
			preparedStatement.close();
		}
	}

	public void cancelLeaseRequest(int personId) {
		// TODO Auto-generated method stub

	}
}
