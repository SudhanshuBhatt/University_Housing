package com.univhousing.agreement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.main.Utils;
import com.univhousing.users.Guest;
import com.univhousing.users.Person;

public class HousingStaffManagesLease_Relation {

	public int leaseNo;
	public int duration;
	public Date cutOffDate;
	public String modeOfPayment;
	public float deposit;
	public Date date;
	public int staffNo;

	boolean wasAccomodationApproved = false;
	Scanner inputObj = new Scanner(System.in);
	Person personObj;
	int personID = 0;
	Guest guestObj = new Guest();
	LeaseRequest_Relation obj = new LeaseRequest_Relation();

	/**
	 * @param ArrayList
	 *            <Integer> allLeaseRequestsToMonitor
	 * @param extraCredit
	 *            This parameter tells if this call is for extra credit of
	 *            regular
	 * @throws SQLException
	 * @action This fetches all the new lease requests submitted for approval
	 */
	public void getAllNewLeaseRequests(
			ArrayList<Integer> allLeaseRequestsToMonitor, boolean extraCredit)
			throws SQLException {
		personObj = new Person();
		/* Write SQL Query to fetch all the lease requests pending for approval */

		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection dbConnection = null;
		String accomodationType = "";
		allLeaseRequestsToMonitor.clear();
		String status = Constants.PENDING_STATUS;
		String modeofPayment = "";
		Date moveInDate = null;
		String duration = "";
		int newLeaseNumber = 0;
		String accomodationTypeGiven = "";
		int accomodationIDGiven = 0;
		String startSemester = "";
		ArrayList<String> preferences = new ArrayList<String>();
		ArrayList<String> requestStatus = new ArrayList<String>();
		requestStatus.clear();

		try {

			dbConnection = ConnectionUtils.getConnection();
			String selectQuery = "SELECT application_request_no,request_status "
					+ "FROM PERSON_ACC_STAFF "
					+ "WHERE (request_status = ? "
					+ "OR request_status = ?) "
					+ "ORDER BY request_status DESC, "
					+ "application_request_no";

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setString(1, status);
			preparedStatement.setString(2, Constants.WAITING_STATUS);

			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				allLeaseRequestsToMonitor.add(rs
						.getInt("application_request_no"));
				requestStatus.add(rs.getString("request_status"));
			}

			if (allLeaseRequestsToMonitor.size() > 0) {
				System.out.println("Displaying all the requests to approve: ");
				for (int i = 0; i < allLeaseRequestsToMonitor.size(); i++) {
					System.out.println(String.format("%3d%-2s%-4d%-3s%-10s",
							(i + 1), ".", allLeaseRequestsToMonitor.get(i),
							" - ", requestStatus.get(i)));
				}
			} else {
				System.out.println("No leases to approve.");
				return;
			}

			int requestChosen = inputObj.nextInt();

			while (requestChosen > allLeaseRequestsToMonitor.size()) {
				System.out.println("Enter your choice again:");
				requestChosen = inputObj.nextInt();
			}
			int requestNumber = allLeaseRequestsToMonitor
					.get(requestChosen - 1);
			/* Write SQL Query to fetch all the details of the requestNumber */
			// ResultSet requestDetails = null;
			String selectQueryDetails = "SELECT APPLICATION_REQUEST_NO, PERSON_ID, "
					+ "ACCOMODATION_TYPE, MODE_OF_PAYMENT, LEASE_MOVE_IN_DATE,"
					+ "DURATION, PREFERENCE1, PREFERENCE2, PREFERENCE3, start_semester "
					+ "FROM PERSON_ACC_STAFF "
					+ "WHERE application_request_no = ?";
			preparedStatement.close();
			rs.close();

			preparedStatement = dbConnection
					.prepareStatement(selectQueryDetails);
			preparedStatement.setInt(1, requestNumber);
			rs = preparedStatement.executeQuery();

			System.out.println(String.format(
					"%-13s%-11s%-20s%-15s%-15s%-9s%-15s%-15s%-15s",
					"App Req No.", "Person ID", "Accommodation Type",
					"Payment Mode", "Move in Date", "Duration", "Preference 1",
					"Preference 2", "Preference 3"));
			System.out
					.println("-----------------------------------------------"
							+ "---------------------------------------------------------"
							+ "--------------------");

			while (rs.next()) {
				System.out.println(String.format(
						"%-13s%-11s%-20s%-15s%-15s%-9s%-15s" + "%-15s%-15s", rs
								.getInt("APPLICATION_REQUEST_NO"), rs
								.getInt("PERSON_ID"), rs
								.getString("ACCOMODATION_TYPE"), rs
								.getString("MODE_OF_PAYMENT"), rs
								.getDate("LEASE_MOVE_IN_DATE"), rs
								.getString("DURATION"), rs
								.getString("PREFERENCE1"), rs
								.getString("PREFERENCE2"), rs
								.getString("PREFERENCE3")));

				/*
				 * Getting the preferences in case the user entered residence
				 * hall.
				 */

				modeofPayment = rs.getString("MODE_OF_PAYMENT");
				duration = rs.getString("DURATION");
				personID = rs.getInt("person_id");
				moveInDate = rs.getDate("LEASE_MOVE_IN_DATE");
				startSemester = rs.getString("start_semester");

				String moveInDateStr = Utils.convertSQLDateToString(moveInDate);

				java.util.Date moveInDateUtil = Utils
						.convertStringToUtilDateFormat(moveInDateStr);

				Calendar c = Calendar.getInstance();
				c.setTime(moveInDateUtil);
				c.add(Calendar.DATE, 15);
				java.util.Date cutOffDateUtils = c.getTime();

				String cutOffDateStr = Utils
						.changeUtilDateToString(cutOffDateUtils);

				cutOffDate = Utils.convertStringToSQLDateFormat(cutOffDateStr);

				preferences.add(rs.getString("ACCOMODATION_TYPE"));
				preferences.add(rs.getString("PREFERENCE1"));
				preferences.add(rs.getString("PREFERENCE2"));
				preferences.add(rs.getString("PREFERENCE3"));

				/*
				 * rs.close(); preparedStatement.close();
				 */
			}

			int flag = 0;
			String approvalStatus = "";
			String moveInDateStr = Utils.convertSQLDateToString(moveInDate);
			String[] tempDateStr = moveInDateStr.split("/");

			/*
			 * String dateA = "01"; String monthA = "08";
			 * 
			 * String dateB = "01"; String monthB = "01";
			 * 
			 * String dateC = "01"; String monthC = "06";
			 * 
			 * if (tempDateStr[0].equalsIgnoreCase(monthA) &&
			 * tempDateStr[1].equalsIgnoreCase(dateA)) { flag = 0;
			 * 
			 * } else if (tempDateStr[0].equalsIgnoreCase(monthB) &&
			 * tempDateStr[1].equalsIgnoreCase(dateB)) {
			 * 
			 * flag = 0; } else if (tempDateStr[0].equalsIgnoreCase(monthC) &&
			 * tempDateStr[1].equalsIgnoreCase(dateC)) {
			 * 
			 * flag = 0; }
			 * 
			 * else { flag = 1;
			 * 
			 * }
			 */
			boolean isValidDate;
			if (guestObj.checkPersonIsGuest(personID)) {
				System.out.println("Do you want to approve this request? Y/N");
				approvalStatus = inputObj.next();
			} else {
				isValidDate = obj.checkAnyCorrectDate(tempDateStr,
						startSemester);
				if (isValidDate) {
					System.out
							.println("Do you want to approve this request? Y/N");
					approvalStatus = inputObj.next();
				} else {
					System.out.println("Not a valid date");
					approvalStatus = "N";
				}
			}

			/*
			 * START: Check to see if the person is already living on some lease
			 * Currently
			 */

			// System.out.println("BEFORE>>>>>>>>"+approvalStatus);
			if (approvalStatus.equalsIgnoreCase("Y")) {
				PreparedStatement ps2 = null;
				ResultSet rs2 = null;
				String queryToFindIfAlreadyLiving = "SELECT * FROM person_accomodation_lease WHERE person_id=?";
				ps2 = dbConnection.prepareStatement(queryToFindIfAlreadyLiving);
				ps2.setInt(1, personID);
				rs2 = ps2.executeQuery();
				if (!rs2.isBeforeFirst()) {

					approvalStatus = "Y";
				} else {
					approvalStatus = "N";
					System.out
							.println("This is a duplicate Housing Request, Turn his/her status to waiting and inform the person!!!!");
				}
			}

			if (approvalStatus.equalsIgnoreCase("Y")) {

				/*
				 * You want to approve the request. There may or may not be
				 * accommodation available.
				 */

				/*
				 * Now we look whether the accomodation_type provided by the
				 * student is available or not If YES: Assign it to him and
				 * update the respective tables If NO: Request will already be
				 * approved, but the request status will be changed to PENDING
				 * NOTE: accomodationType has to be taken from the request
				 * details
				 */

				// boolean accAvailability =
				// checkIfAccomodationTypeAvailable(preferences);
				ArrayList<String> availableAcco = checkIfAccomodationTypeAvailable(preferences);
				selectQuery = "SELECT MAX(lease_no) AS lease_no "
						+ "FROM Lease";
				rs.close();
				preparedStatement.close();

				preparedStatement = dbConnection.prepareStatement(selectQuery);
				rs = preparedStatement.executeQuery();
				rs.next();
				newLeaseNumber = rs.getInt("lease_no") + 1;

				if (availableAcco.get(0).equalsIgnoreCase(
						Constants.RESIDENCE_HALL)) {
					accomodationTypeGiven = availableAcco.get(0);
					wasAccomodationApproved = true;
					System.out.println("Availability in: "
							+ availableAcco.get(1));
					// Now we will give him the accommodation he wanted
					/*
					 * Write SQL Query to update his records in the tables
					 * necessary Student should be alloted a room number and a
					 * palce number
					 */

					// System.out.println("TEST1");
					// System.out.println("TEST2");
					int accID = 0;
					// Chechking if extra credit query is made then we call
					// procedure for hall
					if (extraCredit) {
						// Call Manish's procedure
						Connection conn = ConnectionUtils.getConnection();
						CallableStatement cst = conn
								.prepareCall("{call get_best_accomodation_id_hall (?,?,?)}");
						cst.setString(1, Constants.RESIDENCE_HALL);
						cst.setInt(2, personID);
						// Now registering out parameter
						cst.registerOutParameter(3, Types.INTEGER);
						cst.execute();
						accID = cst.getInt(3);
						cst.close();
						ConnectionUtils.closeConnection(conn);
					} else {
						// If no extra credit then normal query to get
						// accomodation id
						String selectQueryRes = "SELECT accomodation_id "
								+ "FROM residence_hall_provides_room "
								+ "WHERE hall_number = (SELECT hall_number "
								+ "						FROM residence_hall "
								+ "						WHERE hall_name = ?) "
								+ "AND accomodation_id NOT IN (SELECT accomodation_id "
								+ "							FROM person_accomodation_lease)";

						preparedStatement = dbConnection
								.prepareStatement(selectQueryRes);
						preparedStatement.setString(1, availableAcco.get(1));
						rs = preparedStatement.executeQuery();
						rs.next();
						accID = rs.getInt("accomodation_id");
						accomodationIDGiven = accID;
						rs.close();
						preparedStatement.close();
					}

					// System.out.println("TEST3");
					// If the extra credit procedure call returns nothing then
					// call regular query
					if (accID == -1 || accID == 0) {
						System.out
								.println("No best matching accomodation was found");
						String selectQueryRes = "SELECT accomodation_id "
								+ "FROM residence_hall_provides_room "
								+ "WHERE hall_number = (SELECT hall_number "
								+ "						FROM residence_hall "
								+ "						WHERE hall_name = ?) "
								+ "AND accomodation_id NOT IN (SELECT accomodation_id "
								+ "							FROM person_accomodation_lease)";

						preparedStatement = dbConnection
								.prepareStatement(selectQueryRes);
						preparedStatement.setString(1, availableAcco.get(1));
						rs = preparedStatement.executeQuery();
						rs.next();
						accID = rs.getInt("accomodation_id");
						accomodationIDGiven = accID;
						rs.close();
						preparedStatement.close();
					}

					// Adding accomodation id to lease with lease number
					int depositAmountResHall = returnDeposit(accID,
							Constants.RESIDENCE_HALL);

					String insertQuery = "INSERT INTO LEASE (lease_no,deposit,mode_of_payment,duration,cutoff_date) "
							+ "VALUES (?,?,?,?,?)";
					rs.close();
					preparedStatement.close();

					preparedStatement = dbConnection
							.prepareStatement(insertQuery);

					preparedStatement.setInt(1, newLeaseNumber);
					preparedStatement.setString(2, String
							.valueOf(depositAmountResHall));
					preparedStatement.setString(3, modeofPayment);
					preparedStatement.setString(4, duration);
					preparedStatement.setDate(5, cutOffDate);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println(newLeaseNumber);
					String insertPerAccQuery = "INSERT INTO person_accomodation_lease (accomodation_id,person_id,lease_no,accomodation_type,lease_move_in_date) "
							+ "VALUES(?,?,?,?,?)";

					preparedStatement = dbConnection
							.prepareStatement(insertPerAccQuery);
					preparedStatement.setInt(1, accID);
					preparedStatement.setInt(2, personID);
					preparedStatement.setInt(3, newLeaseNumber);
					preparedStatement.setString(4, Constants.RESIDENCE_HALL);
					preparedStatement.setDate(5, moveInDate);
					preparedStatement.executeUpdate();

					String updateQuery = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";
					preparedStatement.close();

					// System.out.println("TEST4");

					preparedStatement = dbConnection
							.prepareStatement(updateQuery);
					preparedStatement.setString(1, Constants.PROCESSED_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("The status for Request Id "
							+ requestNumber + " is APPROVED!!!!");

				} else if (availableAcco.get(0).equalsIgnoreCase(
						Constants.GENERAL_APARTMENT)) {
					accomodationTypeGiven = availableAcco.get(0);
					wasAccomodationApproved = true;

					int accomodationIDApartment = 0;
					// If extracredit is true then call procedure for apartment
					if (extraCredit) {
						// Call Manish's procedure
						Connection conn = ConnectionUtils.getConnection();
						CallableStatement cst = conn
								.prepareCall("{call get_best_accomodation_id_apt (?,?,?)}");
						cst.setString(1, Constants.GENERAL_APARTMENT);
						cst.setInt(2, personID);
						// Now registering out parameter
						cst.registerOutParameter(3, Types.INTEGER);
						cst.execute();
						accomodationIDApartment = cst.getInt(3);
						cst.close();
						ConnectionUtils.closeConnection(conn);

					} else {
						// If not extra credit then just call regular query
						String SQL3 = "SELECT bedroom.accomodation_id FROM BEDROOM "
								+ "WHERE bedroom.accomodation_id NOT IN(SELECT PERSON_ACCOMODATION_LEASE.accomodation_id "
								+ "FROM PERSON_ACCOMODATION_LEASE)";

						preparedStatement = dbConnection.prepareStatement(SQL3);
						rs = preparedStatement.executeQuery();
						rs.next();

						accomodationIDApartment = rs.getInt("accomodation_id");
						accomodationIDGiven = accomodationIDApartment;
						rs.close();
						preparedStatement.close();
					}

					if (accomodationIDApartment == -1
							|| accomodationIDApartment == 0) {
						// If the extra credit returns nothing then we will run
						// regular query again
						System.out
								.println("Sorry no matching accomodations were found");
						String SQL3 = "SELECT bedroom.accomodation_id FROM BEDROOM "
								+ "WHERE bedroom.accomodation_id NOT IN(SELECT PERSON_ACCOMODATION_LEASE.accomodation_id "
								+ "FROM PERSON_ACCOMODATION_LEASE)";

						preparedStatement = dbConnection.prepareStatement(SQL3);
						rs = preparedStatement.executeQuery();
						rs.next();

						accomodationIDApartment = rs.getInt("accomodation_id");
						accomodationIDGiven = accomodationIDApartment;
						rs.close();
						preparedStatement.close();
					}

					// Inserting into lease

					int depositeApartment = returnDeposit(
							accomodationIDApartment,
							Constants.GENERAL_APARTMENT);

					String SQL2 = "INSERT INTO LEASE (lease_no,deposit,mode_of_payment,duration,cutoff_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQL2);
					preparedStatement.setInt(1, newLeaseNumber);
					preparedStatement.setString(2, String
							.valueOf(depositeApartment));
					preparedStatement.setString(3, modeofPayment);
					preparedStatement.setString(4, duration);
					preparedStatement.setDate(5, cutOffDate);
					preparedStatement.executeUpdate();

					preparedStatement.close();

					String SQL4 = "INSERT INTO PERSON_ACCOMODATION_LEASE (accomodation_id,person_id,lease_no,accomodation_type,lease_move_in_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQL4);
					preparedStatement.setInt(1, accomodationIDApartment);
					preparedStatement.setInt(2, personID);
					preparedStatement.setInt(3, newLeaseNumber);
					preparedStatement.setString(4, Constants.GENERAL_APARTMENT);
					preparedStatement.setDate(5, moveInDate);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					String SQL5 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";

					preparedStatement = dbConnection.prepareStatement(SQL5);
					preparedStatement.setString(1, Constants.PROCESSED_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("The status for Request Id "
							+ requestNumber + " is APPROVED!!!!");

					/*
					 * Write SQL query to approve the general apartment request
					 * and allocate a room to the applicant
					 */

				} else if (availableAcco.get(0).equalsIgnoreCase(
						Constants.FAMILY_APARTMENT)) {
					accomodationTypeGiven = availableAcco.get(0);
					wasAccomodationApproved = true;

					// String depositeApartment =
					// Constants.FAMILY_APARTMENT_DEPOSITE;

					String SQLF3 = "SELECT Family.accomodation_id FROM Family_Apartment Family "
							+ "WHERE Family.accomodation_id NOT IN(SELECT PERSON_ACCOMODATION_LEASE.accomodation_id "
							+ "FROM PERSON_ACCOMODATION_LEASE)";

					preparedStatement = dbConnection.prepareStatement(SQLF3);
					rs = preparedStatement.executeQuery();
					rs.next();

					int accomodationIDApartment = rs.getInt("accomodation_id");
					accomodationIDGiven = accomodationIDApartment;
					rs.close();
					preparedStatement.close();

					int depositFamilyApartment = returnDeposit(
							accomodationIDApartment, Constants.FAMILY_APARTMENT);
					String SQLF2 = "INSERT INTO LEASE (lease_no,deposit,mode_of_payment,duration,cutoff_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQLF2);
					preparedStatement.setInt(1, newLeaseNumber);
					preparedStatement.setString(2, String
							.valueOf(depositFamilyApartment));
					preparedStatement.setString(3, modeofPayment);
					preparedStatement.setString(4, duration);
					preparedStatement.setDate(5, cutOffDate);
					preparedStatement.executeUpdate();

					preparedStatement.close();
					String SQL4 = "INSERT INTO PERSON_ACCOMODATION_LEASE (accomodation_id,person_id,lease_no,accomodation_type,lease_move_in_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQL4);
					preparedStatement.setInt(1, accomodationIDApartment);
					preparedStatement.setInt(2, personID);
					preparedStatement.setInt(3, newLeaseNumber);
					preparedStatement.setString(4, Constants.FAMILY_APARTMENT);
					preparedStatement.setDate(5, moveInDate);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					String SQLF5 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";

					preparedStatement = dbConnection.prepareStatement(SQLF5);
					preparedStatement.setString(1, Constants.PROCESSED_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("The status for Request Id "
							+ requestNumber + " is APPROVED!!!!");

					/*
					 * Write an SQL query to approve the family apartment
					 * request and allocate an apartment to the applicant
					 */

					/*
					 * System.out.println("Please enter your family members name (max 4): "
					 * ); inputObj.nextLine();
					 * System.out.println("Name of Family member 1: "); String
					 * memberOne = inputObj.nextLine();
					 * System.out.println("DOB Of member 1: "); String
					 * dobMemberOne = inputObj.nextLine(); java.sql.Date dmo =
					 * Utils.convertStringToSQLDateFormat(dobMemberOne);
					 * 
					 * System.out.println("Name of Family member 2: "); String
					 * memberTwo = inputObj.nextLine();
					 * System.out.println("DOB Of member 2: "); String
					 * dobMemberTwo = inputObj.nextLine(); java.sql.Date dmto =
					 * Utils.convertStringToSQLDateFormat(dobMemberTwo);
					 * 
					 * System.out.println("Name of Family member 3: "); String
					 * memberThree = inputObj.nextLine();
					 * System.out.println("DOB Of member 3: "); String
					 * dobMemberThree = inputObj.nextLine(); java.sql.Date dmth
					 * = Utils.convertStringToSQLDateFormat(dobMemberThree);
					 * 
					 * System.out.println("Name of Family member 4: "); String
					 * memberFour = inputObj.nextLine();
					 * System.out.println("DOB Of member 4: "); String
					 * dobMemberFour = inputObj.nextLine(); java.sql.Date dmf =
					 * Utils.convertStringToSQLDateFormat(dobMemberFour);
					 * 
					 * Write SQL Query to fill student's family details in
					 * students table PreparedStatement ps = null; Connection
					 * conn = ConnectionUtils.getConnection(); String query =
					 * "UPDATE student set member_one = ?, member_two = ?, member_three = ?, member_four = ?"
					 * +
					 * "dob_member_one = ?  (), dob_member_two = ?, dob_member_three = ?, dob_member_four = ? WHERE "
					 * ;
					 * 
					 * ps = conn.prepareStatement(query); ps.setString(1,
					 * memberOne); ps.setString(2, memberTwo); ps.setString(3,
					 * memberThree); ps.setString(4, memberFour); ps.setDate(5,
					 * dmo); ps.setDate(6, dmto); ps.setDate(7, dmth);
					 * ps.setDate(8, dmf); ps.executeUpdate(); ps.close();
					 * ConnectionUtils.closeConnection(conn);
					 */
				}// ////////////////////////////////////////////////////////////////////
				else if (availableAcco.get(0).equalsIgnoreCase(
						Constants.PRIVATE_ACCOMODATION)) {

					accomodationTypeGiven = availableAcco.get(0);
					wasAccomodationApproved = true;

					String depositeApartment = Constants.PRIVATE_APARTMENT_DEPOSITE; // deposite

					String SQL2 = "INSERT INTO LEASE (lease_no,deposit,mode_of_payment,duration,cutoff_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQL2);
					preparedStatement.setInt(1, newLeaseNumber);
					preparedStatement.setString(2, depositeApartment);
					preparedStatement.setString(3, modeofPayment);
					preparedStatement.setString(4, duration);
					preparedStatement.setDate(5, cutOffDate);
					preparedStatement.executeUpdate();

					preparedStatement.close();

					int accomodationIDApartment = 0;
					// If extracredit is true then call procedure for apartment

					// If not extra credit then just call regular query
					String SQL3 = "SELECT P.accomodation_id FROM private_accomodation P WHERE P.accomodation_type = ? AND"
							+ " P.accomodation_id NOT IN(SELECT PL.accomodation_id FROM PERSON_ACCOMODATION_LEASE PL)";

					preparedStatement = dbConnection.prepareStatement(SQL3);
					preparedStatement.setString(1, preferences.get(1));
					rs = preparedStatement.executeQuery();
					rs.next();

					accomodationIDApartment = rs.getInt("accomodation_id");
					accomodationIDGiven = accomodationIDApartment;
					rs.close();
					preparedStatement.close();
					// /////////////////////////////////

					String SQL4 = "INSERT INTO PERSON_ACCOMODATION_LEASE (accomodation_id,person_id,lease_no,accomodation_type,lease_move_in_date) "
							+ "VALUES (?,?,?,?,?)";

					preparedStatement = dbConnection.prepareStatement(SQL4);
					preparedStatement.setInt(1, accomodationIDApartment);
					preparedStatement.setInt(2, personID);
					preparedStatement.setInt(3, newLeaseNumber);
					preparedStatement.setString(4, availableAcco.get(1));
					preparedStatement.setDate(5, moveInDate);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					String SQL5 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";

					preparedStatement = dbConnection.prepareStatement(SQL5);
					preparedStatement.setString(1, Constants.PROCESSED_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("The status for Request Id "
							+ requestNumber + " is APPROVED!!!!");

					/*
					 * Write SQL query to approve the general apartment request
					 * and allocate a room to the applicant
					 */

				} else if (availableAcco.get(0).equalsIgnoreCase(
						Constants.NOTHING_AVAILABLE)) {
					wasAccomodationApproved = false;
					String selectQ1 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";

					preparedStatement = dbConnection.prepareStatement(selectQ1);
					preparedStatement.setString(1, Constants.WAITING_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("The status for Request Id "
							+ requestNumber + " is turned to Waiting!");

				}
			} else { // if availableAcco ="N"
				wasAccomodationApproved = false;
				String selectQ1 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
						+ "WHERE application_request_no = ?";

				preparedStatement = dbConnection.prepareStatement(selectQ1);
				preparedStatement.setString(1, Constants.WAITING_STATUS);
				preparedStatement.setInt(2, requestNumber);
				preparedStatement.executeUpdate();
				preparedStatement.close();

				System.out.println("The status for Request Id " + requestNumber
						+ " is not Approved and hence turned to Waiting!");
			}

			// If the request was approved
			if (wasAccomodationApproved) {
				// Now updating the housing_status for this person to be placed
				personObj
						.updateHousingStatus(personID, Constants.PLACED_STATUS);

				// We will write the logic for invoice generation for this
				// person
				// generateInvoices(modeofPayment,personID,duration,moveInDate,newLeaseNumber,accomodationIDGiven,accomodationTypeGiven);
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
	 * @param modeofPayment
	 *            This is either month or semester
	 * @param personID
	 *            for the person who has to be assigned this room
	 * @param duration
	 *            The duration of his lease
	 * @param moveInDate
	 *            When he wants to move in
	 * @throws SQLException
	 */
	/*
	 * private void generateInvoices(String modeofPayment, int personID,String
	 * duration, Date moveInDate, int newLeaseNumber, int accomodationIdGiven,
	 * String accomodationTypeGiven) throws SQLException { int numberOfInvoices
	 * = 0, livingRent = 0, parkingFees = 0, lateFees = 0; int incidentalCharges
	 * = 0, invoiceNo = 0, leaseNumber = 0, totalPaymentDue = 0, damageCharges =
	 * 0; String paymentStatus = null; String paymentDateString = null;
	 * 
	 * if(modeofPayment.equalsIgnoreCase(Constants.PAYMENTOPTION_MONTHLY)) { int
	 * temp = Integer.parseInt(duration); numberOfInvoices =
	 * temp*Constants.MONTHS_IN_SEMESTER; } else
	 * if(modeofPayment.equalsIgnoreCase(Constants.PAYMENTOPTION_SEMESTER)) {
	 * numberOfInvoices = Integer.parseInt(duration); }
	 * 
	 * PreparedStatement ps = null; Connection conn =
	 * ConnectionUtils.getConnection(); String insertQuery =
	 * "INSERT INTO invoice_person_lease (monthly_housing_rent,monthly_parking_rent,"
	 * +
	 * "late_fees,incidental_charges,invoice_no,payment_date,payment_method,lease_no,payment_status,"
	 * +
	 * "payment_due,damage_charges,person_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	 * ps = conn.prepareStatement(insertQuery); ps.setInt(1, livingRent);
	 * ps.setInt(2, parkingFees); ps.setInt(3, lateFees); ps.setInt(4,
	 * incidentalCharges); ps.setInt(5, invoiceNo); ps.setString(6,
	 * paymentDateString); ps.setString(7, modeofPayment); ps.setInt(8,
	 * newLeaseNumber); ps.setString(9, paymentStatus); ps.setInt(10,
	 * totalPaymentDue); ps.setInt(11, damageCharges); ps.setInt(12, personID);
	 * 
	 * getLivingRent(personID,leaseNumber);
	 * getParkingFees(personID,leaseNumber); generateInvoiceNo();
	 * 
	 * for (int i = 0; i < numberOfInvoices; i++) {
	 * 
	 * } }
	 */
	/*
	 * private int generateInvoiceNo() { // TODO Auto-generated method stub
	 * PreparedStatement ps = null; Connection conn =
	 * ConnectionUtils.getConnection(); return 0; }
	 * 
	 * private void getParkingFees(int personID, int leaseNumber) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * private void getLivingRent(int personID, int leaseNumber) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */

	/**
	 * @param accomodationType
	 * @return True if accomodationType is present else False
	 */
	private ArrayList<String> checkIfAccomodationTypeAvailable(
			ArrayList<String> preferences) {
		/*
		 * Write SQL Query to check if the accommodation type student selected
		 * is actually available If YES: return True If NO: return False
		 */
		String type = preferences.get(0);
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection dbConnection = null;
		ArrayList<String> availableAcco = new ArrayList<String>();
		try {
			dbConnection = ConnectionUtils.getConnection();

			if (type.equalsIgnoreCase(Constants.RESIDENCE_HALL)) {
				for (int i = 0; i < 3; i++) {
					String selectQueryPref = "SELECT COUNT(accomodation_id) AS count "
							+ "FROM residence_hall_provides_room "
							+ "WHERE hall_number = (SELECT hall_number "
							+ "						FROM residence_hall "
							+ "						WHERE hall_name = ?) "
							+ "AND accomodation_id NOT IN (SELECT accomodation_id "
							+ "							FROM person_accomodation_lease)";

					preparedStatement = dbConnection
							.prepareStatement(selectQueryPref);
					preparedStatement.setString(1, preferences.get(i + 1));
					rs = preparedStatement.executeQuery();

					while (rs.next()) {
						if (rs.getInt("count") > 0) {
							availableAcco.add("RESIDENCE HALL");
							availableAcco.add(preferences.get(i + 1));
							dbConnection.close();
							return availableAcco;
						}
					}
					preparedStatement.close();
					rs.close();
				}
				availableAcco.add(Constants.NOTHING_AVAILABLE);
				return availableAcco;
			} else if (type.equalsIgnoreCase(Constants.GENERAL_APARTMENT)) {
				String selectQueryGenApt = "SELECT COUNT (B.bedroom_place_no) AS rooms "
						+ "FROM bedroom B "
						+ "WHERE B.accomodation_id  NOT IN "
						+ "(SELECT accomodation_id "
						+ "FROM person_accomodation_lease)";

				preparedStatement = dbConnection
						.prepareStatement(selectQueryGenApt);
				rs = preparedStatement.executeQuery();

				while (rs.next()) {
					if (rs.getInt("rooms") > 0) {
						availableAcco.add(Constants.GENERAL_APARTMENT);
						availableAcco.add("NA");
						dbConnection.close();
						return availableAcco;
					}
					preparedStatement.close();
					rs.close();
				}
				availableAcco.add(Constants.NOTHING_AVAILABLE);
				return availableAcco;
			} else if (type.equalsIgnoreCase(Constants.FAMILY_APARTMENT)) {
				String selectQueryFamApt = "SELECT COUNT (F.apt_no) AS apartments "
						+ "FROM Family_Apartment F "
						+ "WHERE F.accomodation_id NOT IN "
						+ "(SELECT accomodation_id "
						+ "FROM person_accomodation_lease)";

				preparedStatement = dbConnection
						.prepareStatement(selectQueryFamApt);
				rs = preparedStatement.executeQuery();

				while (rs.next()) {
					if (rs.getInt("apartments") > 0) {
						availableAcco.add(Constants.FAMILY_APARTMENT);
						availableAcco.add("NA");
						dbConnection.close();
						return availableAcco;
					}
					preparedStatement.close();
					rs.close();
				}
				availableAcco.add(Constants.NOTHING_AVAILABLE);
				return availableAcco;
			} else if (type.equalsIgnoreCase(Constants.PRIVATE_ACCOMODATION)) {

				for (int i = 0; i < 3; i++) {
					String selectQueryPref = "select count(accomodation_id) as apartments from private_accomodation where accomodation_type = ?"
							+ " AND accomodation_id NOT IN(select accomodation_id from person_accomodation_lease)";

					preparedStatement = dbConnection
							.prepareStatement(selectQueryPref);
					preparedStatement.setString(1, preferences.get(i + 1));
					rs = preparedStatement.executeQuery();

					while (rs.next()) {
						if (rs.getInt("apartments") > 0) {
							availableAcco.add(Constants.PRIVATE_ACCOMODATION);
							availableAcco.add(preferences.get(i + 1));
							dbConnection.close();
							return availableAcco;
						}
					}
					preparedStatement.close();
					rs.close();
				}
				availableAcco.add(Constants.NOTHING_AVAILABLE);
				return availableAcco;

			} else {
				availableAcco.add(Constants.NOTHING_AVAILABLE);
				return availableAcco;
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
		availableAcco.add("NA");
		return availableAcco;
	}

	/**
	 * @param ArrayList
	 *            <Integer> adminLevelTerminationRequests
	 * @throws SQLException
	 */
	public void getAllNewTerminationRequests(
			ArrayList<Integer> allTerminationRequestsToMonitor)
			throws SQLException {

		/*
		 * BEFORE YOU SHOW ALL THE REQUESTS, UPDATE THE STATUS OF EXISTING
		 * RECORDS WHOSE STATUS IS PROCESSED AND THE INSPECTION DATE IS LESS
		 * THAN THE CURRENT DATE OR SYSDATE.
		 */
		String updateStatusQuery = "update termination_requests set status = ? where status = ? and inspection_date < sysdate";
		PreparedStatement psForUpdate = null;
		Connection connForUpdate = ConnectionUtils.getConnection();

		System.out
				.println("Updating the status of all requests under processing to complete with inspection_date less than today's date");
		psForUpdate = connForUpdate.prepareStatement(updateStatusQuery);
		psForUpdate.setString(1, Constants.COMPLETE_STATUS);
		psForUpdate.setString(2, Constants.PROCESSED_STATUS);
		psForUpdate.executeUpdate();

		ConnectionUtils.closeConnection(connForUpdate);

		System.out
				.println("\nUpdate successful...Fetching the termination requests. Please wait...\n");

		/* Write SQL Query to fetch all the termination requests */
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Connection dbConnection = null;
		allTerminationRequestsToMonitor.clear();
		String status = Constants.PENDING_STATUS;
		ArrayList<Integer> allPersonID = new ArrayList<Integer>();
		allPersonID.clear();

		try {

			/*
			 * Query for getting termination request numbers whose status is
			 * PENDING
			 */
			dbConnection = ConnectionUtils.getConnection();
			String selectQuery = "SELECT termination_request_number, person_id "
					+ "FROM TERMINATION_REQUESTS " + "WHERE STATUS = ?";

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setString(1, status);

			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				allTerminationRequestsToMonitor.add(rs
						.getInt("termination_request_number"));
				allPersonID.add(rs.getInt("person_id"));
			}

			if (allTerminationRequestsToMonitor.size() > 0) {
				System.out.println("Displaying all the requests to approve: ");
				for (int i = 0; i < allTerminationRequestsToMonitor.size(); i++) {
					System.out.println((i + 1) + ". "
							+ allTerminationRequestsToMonitor.get(i) + " - "
							+ allPersonID.get(i));
				}
			} else {
				return;
			}

			int requestChosen = inputObj.nextInt();
			while (requestChosen > allTerminationRequestsToMonitor.size()) {
				System.out.println("Enter correct serial number");
				requestChosen = inputObj.nextInt();
			}
			int requestNumber = allTerminationRequestsToMonitor
					.get(requestChosen - 1);

			/* Write SQL Query to fetch all the details of the requestNumber */
			String selectQueryDetails = "SELECT reason, termination_request_number,"
					+ "status, termination_date, inspection_date, person_id, staff_no "
					+ "FROM Termination_Requests "
					+ "WHERE termination_request_number = ?";
			preparedStatement.close();
			rs.close();

			try {

				preparedStatement = dbConnection
						.prepareStatement(selectQueryDetails);
				preparedStatement.setInt(1, requestNumber);
				rs = preparedStatement.executeQuery();

				System.out.println(String.format(
						"%-50s%-13s%-11s%-15s%-18s%-5s%-10s", "Reason",
						"Term_req_no", "Status", "Term Date",
						"Inspection Date", "P_ID", "Staff No."));
				System.out
						.println("-----------------------------------------------"
								+ "---------------------------------------------------------"
								+ "--------------------");

				while (rs.next()) {
					System.out
							.println(String.format(
									"%-50s%-13s%-11s%-15s%-18s%-5s%-10s", rs
											.getString("reason"),
									rs.getInt("termination_request_number"), rs
											.getString("status"), rs
											.getDate("termination_date"), rs
											.getDate("inspection_date"), rs
											.getInt("person_id"), rs
											.getInt("staff_no")));
				}

			} catch (SQLException e1) {
				System.out.println("SQLException: " + e1.getMessage());
				System.out.println("VendorError: " + e1.getErrorCode());
				return;
			}

			catch (Exception e3) {
				System.out
						.println("General Exception Case. Printing stack trace below:\n");
				e3.printStackTrace();
				return;

			} finally {
				try {
					rs.close();
					preparedStatement.close();
					// dbConnection.close();

				} catch (SQLException e) {
					e.printStackTrace();

				}

			}

			System.out
					.println("Enter the inspection date in MM/DD/YYYY format");

			String inspectionDate = inputObj.next();
			java.sql.Date sqlInspectionDate = Utils
					.convertStringToSQLDateFormat(inspectionDate);

			String updateQueryDate = "UPDATE Termination_Requests "
					+ "SET inspection_date = ? "
					+ "WHERE termination_request_number = ?";

			System.out.println("Marker31");
			PreparedStatement pAddInspecDate = null;
			pAddInspecDate = dbConnection.prepareStatement(updateQueryDate);

			try {

				pAddInspecDate.setDate(1, sqlInspectionDate);
				pAddInspecDate.setInt(2, requestNumber);

				pAddInspecDate.executeUpdate();

			} catch (Exception e) {
				System.out
						.println("Update of termination requests table failed.");
				return;
			}
			System.out.println("Marker32");
			pAddInspecDate.close();

			System.out.println("Marker33");

			System.out.println("Marker1");

			int damageFees = 0;
			System.out.println("Please enter the damage fees:");
			damageFees = inputObj.nextInt();
			double totalPenaltyDamageFees = 0.0;
			/*
			 * Write SQL Trigger to change the status of request to Complete
			 * after the inspection date
			 */

			/*
			 * Write SQL Query to fetch the latest unpaid invoice and add the
			 * damageFees to already existing payment_due
			 */

			String updateQueryStatus = "UPDATE Termination_Requests "
					+ "SET status = ? "
					+ "WHERE termination_request_number = ?";

			PreparedStatement pUpdateStatus = null;

			pUpdateStatus = dbConnection.prepareStatement(updateQueryStatus);
			pUpdateStatus.setString(1, Constants.PROCESSED_STATUS);
			pUpdateStatus.setInt(2, requestNumber);
			pUpdateStatus.executeUpdate();

			pUpdateStatus.close();

			String selectPersonID = "SELECT person_id "
					+ "FROM termination_requests "
					+ "WHERE termination_request_number = ?";
			rs.close();
			preparedStatement.close();

			preparedStatement = dbConnection.prepareStatement(selectPersonID);
			preparedStatement.setInt(1, requestNumber);
			rs = preparedStatement.executeQuery();
			rs.next();
			int personID = rs.getInt("person_id");

			try {

				pUpdateStatus = dbConnection
						.prepareStatement(updateQueryStatus);
				pUpdateStatus.setString(1, Constants.PROCESSED_STATUS);
				pUpdateStatus.setInt(2, requestNumber);
				pUpdateStatus.executeUpdate();
			} catch (Exception e) {
				System.out
						.println("Update of termination requests table failed.");
				return;
			} finally {
				pUpdateStatus.close();
			}

			System.out.println("Marker3");
			// Get the accomodation type for the person who raised that
			// termination request.

			int accomodationId = 0;
			String accomodation_type = "";

			try {

				selectPersonID = "select tr.person_id,pal.accomodation_type,pal.accomodation_id from termination_requests tr,person_accomodation_lease pal "
						+ " where tr.termination_request_number = ? and tr.person_id = pal.person_id ";
				System.out.println("Checking for request numbe "
						+ requestNumber);
				preparedStatement = dbConnection
						.prepareStatement(selectPersonID);
				preparedStatement.setInt(1, requestNumber);
				rs = preparedStatement.executeQuery();
				while (rs.next()) {
					personID = rs.getInt("person_id");
					accomodation_type = rs.getString("accomodation_type");
					accomodationId = rs.getInt("accomodation_id");
				}

			} catch (Exception e) {
				System.out
						.println("Update of termination requests table failed.");
				return;
			} finally {
				rs.close();
				preparedStatement.close();
			}

			String selectMonthlyRent;

			System.out.println("Marker4");

			// if accomodation type is family apartment : get the monthly rent
			// for that accomodation id from family_apartment table

			if (accomodation_type.equals(Constants.FAMILY_APARTMENT)) {
				selectMonthlyRent = "select monthly_rent from family_apartment where accomodation_id= ?";
			} else if (accomodation_type.equals(Constants.RESIDENCE_HALL)) {
				// if accomodation type is residence hall : get the monthly rent
				// for that accomodation id from residence_hall_provides_room
				// table

				selectMonthlyRent = "select monthly_rent_rate  from residence_hall_provides_room where accomodation_id = ?";
			} else if (accomodation_type.equals(Constants.GENERAL_APARTMENT)
					|| accomodation_type.equals(Constants.BEDROOM)) {
				// if accomodation type is general apartment/Apartment or
				// Bedroom : get the monthly rent for that accomodation id from
				// residence_hall_provides_room table

				selectMonthlyRent = "select monthly_rent_rate from bedroom where accomodation_id = ?";

			} else {
				System.out
						.println("Data Corruption: The apartment type is invalid for this person. Please contact Admin.");
				return;
			}
			System.out.println("selectMonthlyRent Query:=" + selectMonthlyRent);
			System.out.println("Marker5 with accomodationId:= "
					+ accomodationId);

			double monthlyRent;

			try {
				preparedStatement = dbConnection
						.prepareStatement(selectMonthlyRent);
				preparedStatement.setInt(1, accomodationId);
				rs = preparedStatement.executeQuery();
				rs.next(); // what if this returns no records
				if (accomodation_type.equals(Constants.FAMILY_APARTMENT))
					monthlyRent = rs.getDouble("monthly_rent");
				else
					monthlyRent = rs.getDouble("monthly_rent_rate");

			} catch (Exception e) {
				e.printStackTrace();
				return;
			} finally {
				rs.close();
				preparedStatement.close();
			}

			// Now get the remaining days
			System.out.println("Marker6");

			String selectRemainingDays = "select pal.person_id, "
					+ "  (add_months(pal.lease_move_in_date,l.duration) - tr.termination_date) "
					+ " from termination_requests tr,lease l,person_accomodation_lease pal "
					+ " where tr.person_id = pal.person_id "
					+ " and pal.lease_no = l.lease_no "
					+ " and tr.termination_request_number = ? ";

			int remainingDays;

			try {
				preparedStatement = dbConnection
						.prepareStatement(selectRemainingDays);
				preparedStatement.setInt(1, requestNumber);
				rs = preparedStatement.executeQuery();
				rs.next();
				personID = rs.getInt("person_id");
				System.out.println("Marker77 before1:=" + requestNumber);
				remainingDays = rs.getInt(2);

			} catch (Exception e) {
				System.out
						.println("Update of termination requests table failed.");
				return;
			} finally {
				rs.close();
				preparedStatement.close();
			}

			// int remainingDays = rs.getInt("remaining_days");

			double penalty = 0.0;

			double remainingAmount = (remainingDays * monthlyRent) / 30;

			System.out.println("remainingDays:= " + remainingDays);
			System.out.println("monthlyRent:= " + monthlyRent);
			System.out.println("remainingAmount:= " + remainingAmount);

			if (remainingDays < 60) // when termination date is after the
									// cut-off date
			{
				penalty = remainingAmount;
				System.out.println("Penalty:= " + penalty);
			} else {
				penalty = 0.2 * remainingAmount;
				System.out.println("Penalty:= " + penalty);
			}

			String selectQueryFees = "SELECT MAX(payment_date) as \"date\""
					+ "FROM invoice_person_lease "
					+ "WHERE (payment_status <> ?  " + "AND person_id = ?) ";

			System.out.println("Getting date for person_id: " + personID

			+ " and requestNumber: " + requestNumber);

			preparedStatement = dbConnection.prepareStatement(selectQueryFees);
			preparedStatement.setString(1, Constants.PAID_INVOICE);
			preparedStatement.setInt(2, personID);
			rs = preparedStatement.executeQuery();

			rs.next();
			Date maxDate = rs.getDate("date");
			System.out.println("Maximum date: " + maxDate);

			if (maxDate == null) {
				System.out.println("No outstanding charges");
				System.out.println("Damage fees: " + damageFees);

				totalPenaltyDamageFees = damageFees + penalty;
				totalPenaltyDamageFees = 0; // [MANISH]
				System.out.println("Marker8");

				if (totalPenaltyDamageFees > 0) {
					
					/*
					 * Query for getting invoice number from
					 * invoice_person_lease
					 */
					String selectQueryInvoiceNum = "SELECT MAX(invoice_no) as invoice_no "
							+ "FROM invoice_person_lease ";
					rs.close();
					preparedStatement.close();

					preparedStatement = dbConnection
							.prepareStatement(selectQueryInvoiceNum);
					rs = preparedStatement.executeQuery();
					rs.next();
					System.out.println("Breakpoint1");
					int invoiceNumber = rs.getInt("invoice_no");
					invoiceNumber++;
					rs.close();

					String selectQueryLeaseNum = "SELECT lease_no "
							+ "FROM person_accomodation_lease "
							+ "WHERE person_id = ?";
					preparedStatement.close();

					preparedStatement = dbConnection
							.prepareStatement(selectQueryLeaseNum);
					preparedStatement.setInt(1, personID);

					System.out.println("Marker9");

					rs = preparedStatement.executeQuery();
					boolean isNotEmpty = rs.next();
					if (isNotEmpty) {
						System.out.println("Breakpoint2");
						int leaseNumber = rs.getInt("lease_no");

						PreparedStatement p2 = null;
						Connection c2 = null;

						/*
						 * Write a query to insert an entry into the
						 * invoice_person_lease table
						 */
						String insertQueryLease = "INSERT INTO invoice_person_lease "
								+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, 30);
						java.util.Date justNowUtils = cal.getTime();
						String temp = Utils
								.changeUtilDateToString(justNowUtils);
						Date oneMonthLater = Utils
								.convertStringToSQLDateFormat(temp);

						c2 = ConnectionUtils.getConnection();
						p2 = c2.prepareStatement(insertQueryLease);
						p2.setInt(1, 0);
						p2.setInt(2, 0);
						p2.setInt(3, 0);
						p2.setInt(4, 0);
						p2.setInt(5, invoiceNumber);
						p2.setDate(6, oneMonthLater);
						p2.setString(7, "Credit");
						p2.setInt(8, leaseNumber);
						p2.setString(9, "Outstanding");
						p2.setDouble(10, totalPenaltyDamageFees);
						p2.setInt(11, 0);
						p2.setInt(12, personID);

						int insertRes = p2.executeUpdate();

						p2.close();
						c2.close();
					} else {
						System.out
								.println("There is no lease entry for this person");
					}
				} else {
					
					/*
					 * There are no damage fees. Write a query to remove the
					 * entry from person_accommodation_lease
					 */
					String getLease = "SELECT lease_no "
							+ "FROM person_accomodation_lease "
							+ "WHERE person_id = ?";
					rs.close();
					preparedStatement.close();

					preparedStatement = dbConnection.prepareStatement(getLease);
					preparedStatement.setInt(1, personID);

					rs = preparedStatement.executeQuery();
					rs.next();
					int leaseNumber = rs.getInt("lease_no");
					System.out.println("Lease number: " + leaseNumber
							+ ". PersonID: " + personID);

					if (!checkIfPersonLeaseExistsInHistory(personID,
							leaseNumber)) {
						/*
						 * Write SQL Query to put the terminated lease request
						 * into person_accomodation_lease_hist
						 */
						Connection histconn = ConnectionUtils.getConnection();
						PreparedStatement preHist = null;
						//asdas
						Connection histconn1 = ConnectionUtils.getConnection();
						PreparedStatement preHist1 = null;
						System.out.println("Creating backup for personID "
								+ personID);
						// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						String historyQuery = "Insert into person_accomodation_lease_hist "
								+ "select * from person_accomodation_lease where person_id = ?";
						
						preHist = histconn.prepareStatement(historyQuery);
						preHist.setInt(1, personID);
						preHist.executeUpdate();
						preHist.close();
						ConnectionUtils.closeConnection(histconn);
						
						
						String historyLease = "Insert into lease_hist "
							+ "select * from lease where lease_no = ?";
						preHist1 = histconn1.prepareStatement(historyLease);
						preHist1.setInt(1, leaseNumber);
						preHist1.executeUpdate();
						preHist1.close();
						ConnectionUtils.closeConnection(histconn1);
					} else {
						System.out.println("Already backed up for this person");
					}
					/*
					 * Delete entry from person_accommodation_lease
					 */
					Connection connDeleteEntry = null;
					PreparedStatement pDeleteEntry = null;

					System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					connDeleteEntry = ConnectionUtils.getConnection();
					String deleteQuery = "DELETE FROM person_accomodation_lease "
							+ "WHERE person_id = ?";

					pDeleteEntry = connDeleteEntry
							.prepareStatement(deleteQuery);
					pDeleteEntry.setInt(1, personID);
					pDeleteEntry.executeUpdate();

					System.out.println("After deleting from "
							+ "person_accomodation_lease");

					/*
					 * Delete entry from invoice_person_lease
					 */
					Connection connDeleteInvoice = null;
					PreparedStatement pDeleteInvoice = null;
					connDeleteInvoice = ConnectionUtils.getConnection();
					String deleteInvoice = "DELETE FROM invoice_person_lease "
							+ "WHERE (lease_no = ? AND person_id = ?)";
					pDeleteInvoice = connDeleteInvoice
							.prepareStatement(deleteInvoice);
					pDeleteInvoice.setInt(1, leaseNumber);
					pDeleteInvoice.setInt(2, personID);
					pDeleteInvoice.executeUpdate();
					System.out
							.println("After deleting from invoice_person_lease");

					/*
					 * Delete entry from lease
					 */
					Connection connDeleteLease = null;
					PreparedStatement pDeleteLease = null;

					connDeleteLease = ConnectionUtils.getConnection();
					String deleteLease = "DELETE FROM Lease "
							+ "WHERE lease_no = ?";

					pDeleteLease = connDeleteLease
							.prepareStatement(deleteLease);
					pDeleteLease.setInt(1, leaseNumber);
					pDeleteLease.executeUpdate();

					System.out.println("After deleting from " + "lease");
					connDeleteEntry.close();
					connDeleteLease.close();
					connDeleteInvoice.close();
					pDeleteEntry.close();
					pDeleteLease.close();
					pDeleteInvoice.close();
				}
			} else {
				System.out.println("Maximum date: " + maxDate);
				Date maxDateCopy = maxDate;
				Date maxDateCopy1 = maxDateCopy;
				String a = "'";
				String maximumDate = maxDateCopy1.toString();
				maximumDate = a.concat(maximumDate);
				maximumDate = maximumDate.concat(a);
				String dateFormat = "'yyyy-mm-dd'";
				String selectQueryDamageFields = "SELECT monthly_housing_rent,"
						+ "monthly_parking_rent,late_fees,incidental_charges,"
						+ "damage_charges "
						+ "FROM invoice_person_lease "
						+ "WHERE payment_date = to_date(?,?) "
						+ "AND person_id = (SELECT person_id FROM termination_requests "
						+ "WHERE termination_request_number = ?)";
				rs.close();
				preparedStatement.close();

				System.out
						.println("Maximum date and string and request number are: "
								+ maximumDate
								+ " "
								+ dateFormat
								+ " "
								+ requestNumber);

				preparedStatement = dbConnection
						.prepareStatement(selectQueryDamageFields);
				preparedStatement.setString(1, maximumDate);
				preparedStatement.setString(2, dateFormat);
				preparedStatement.setInt(3, requestNumber);

				System.out.println("Before updating the table");
				rs = preparedStatement.executeQuery();
				System.out.println("After updating the table");
				rs.next();
				int monthlyHousingRent = rs.getInt("monthly_housing_rent");
				int monthlyParkingRent = rs.getInt("monthly_parking_rent");
				int lateFees = rs.getInt("late_fees");
				int incidentalCharges = rs.getInt("incidental_charges");
				int damageCharges = rs.getInt("damage_charges");

				dbConnection.commit();

				double totalCharges = monthlyHousingRent + monthlyParkingRent
						+ lateFees + incidentalCharges + totalPenaltyDamageFees;
				/*
				 * System.out.println(monthlyHousingRent + " " +
				 * monthlyParkingRent + " " + lateFees + " " + incidentalCharges
				 * + " " + damageCharges);
				 */

				String updateQueryDamage = "UPDATE invoice_person_lease "
						+ "SET payment_due = ?"
						+ "WHERE (payment_date = to_date(?,?) "
						+ "AND person_id = ? )";

				rs.close();
				Connection c1 = ConnectionUtils.getConnection();
				PreparedStatement p1 = null;
				p1 = c1.prepareStatement(updateQueryDamage);
				p1.setDouble(1, totalCharges);
				p1.setString(2, maximumDate);
				p1.setString(3, dateFormat);
				p1.setInt(4, personID);

				// System.out.println(updateQueryDamage);
				int z = p1.executeUpdate();
				System.out.println("AFTER UPDATE");
				p1.close();
				c1.close();

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

	public void checkForLeaseCompletion() {

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;

		try {

			dbConnection = ConnectionUtils.getConnection();

			String selectQuery = "SELECT * FROM PERSON_ACC_STAFF WHERE REQUEST_STATUS= ? OR  REQUEST_STATUS =? ";

			preparedStatement = dbConnection.prepareStatement(selectQuery);
			preparedStatement.setString(1, Constants.PROCESSED_STATUS);
			preparedStatement.setString(2, Constants.COMPLETED_LEASE_STATUS);

			// taking completed entries as well because if bill unpaid, next
			// time it should be checked
			rs = preparedStatement.executeQuery();
			System.out.println("");
			while (rs.next()) {

				int personID = rs.getInt("person_id");
				System.out
						.println("Enter the current Date mm/dd/yyyy <> to check for person"
								+ personID);
				String currentDateStr = inputObj.nextLine();

				java.util.Date currentDateUtil = Utils
						.convertStringToUtilDateFormat(currentDateStr);

				Date moveInDate = rs.getDate("lease_move_in_date");
				int duration = Integer.parseInt(rs.getString("Duration"));

				int requestNumber = rs.getInt("application_request_no");
				String moveInDateStr = Utils.changeUtilDateToString(moveInDate);
				java.util.Date moveInDateUtils = Utils
						.convertStringToUtilDateFormat(moveInDateStr);

				Calendar c = Calendar.getInstance();
				c.setTime(moveInDateUtils);
				c.add(Calendar.MONTH, duration);
				java.util.Date moveOutDateUtils = c.getTime();

				if (currentDateUtil.compareTo(moveOutDateUtils) >= 0) {

					String selectQ1 = "UPDATE PERSON_ACC_STAFF SET request_status = ? "
							+ "WHERE application_request_no = ?";

					preparedStatement = dbConnection.prepareStatement(selectQ1);
					preparedStatement.setString(1,
							Constants.COMPLETED_LEASE_STATUS);
					preparedStatement.setInt(2, requestNumber);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					System.out.println("Request number <" + requestNumber
							+ "> with Person Id [" + personID
							+ "] has completed the LEASE!");

					ResultSet rsToCheckPresentinLeaseAcc = null;
					String selectQ2 = "SELECT person_id from PERSON_ACCOMODATION_LEASE WHERE person_id=?";
					preparedStatement = dbConnection.prepareStatement(selectQ2);
					preparedStatement.setInt(1, personID);
					rsToCheckPresentinLeaseAcc = preparedStatement
							.executeQuery();

					if (rsToCheckPresentinLeaseAcc.isBeforeFirst()) {

						boolean flagUnpaid = false;
						boolean paid = false;
						rsToCheckPresentinLeaseAcc.next();
						System.out.println("Cheking if personID [" + personID
								+ "] has paid his/her dues... ");

						PreparedStatement preparedStatement2 = null;
						ResultSet rsToCheckIfInvoice = null;

						String selectQ3 = "SELECT PAYMENT_STATUS FROM INVOICE_PERSON_LEASE WHERE person_id= ? ";
						preparedStatement2 = dbConnection
								.prepareStatement(selectQ3);
						preparedStatement2.setInt(1, personID);
						rsToCheckIfInvoice = preparedStatement2.executeQuery();

						if (rsToCheckIfInvoice.isBeforeFirst()) {

							while (rsToCheckIfInvoice.next()) {

								if (rsToCheckIfInvoice.getString(
										"payment_status").equals("Outstanding")) {

									flagUnpaid = true;
									break;
								}

							}

							paid = true;
						}

						else {

							System.out
									.println("The person ["
											+ personID
											+ "] has not yet generated the Invioce. Sending Email reminder....\n");
						}

						if (flagUnpaid) {
							System.out
									.println("The person ["
											+ personID
											+ "] has still not paid his/her dues. Sending Email reminder....!");
						} else if (flagUnpaid == false && paid == true) {

							System.out
									.println("DELETING PID< "
											+ personID
											+ "> From lease table. Sending Exit email...");

							/*
							 * Write SQL Query to fetch the lease_no for the
							 * person id from person_accomodation_lease table
							 */
							int leaseNumber = 0;
							PreparedStatement ps1 = null;
							ResultSet rs1 = null;
							Connection conn1 = ConnectionUtils.getConnection();
							String query1 = "select lease_no from person_accomodation_lease where person_id = ?";

							ps1 = conn1.prepareStatement(query1);
							ps1.setInt(1, personID);
							rs1 = ps1.executeQuery();

							while (rs1.next()) {
								leaseNumber = rs1.getInt("lease_no");
								break;
							}

							if (leaseNumber == 0) {
								System.out
										.println("Person is not currently on lease");
							} else {
								if (!checkIfPersonLeaseExistsInHistory(
										personID, leaseNumber)) {
									/*
									 * Write SQL Query to put the terminated
									 * lease request into
									 * person_accomodation_lease_hist
									 */
									Connection histconn = ConnectionUtils
											.getConnection();
									PreparedStatement preHist = null;
									
									Connection histconn1 = ConnectionUtils.getConnection();
									PreparedStatement preHist1 = null;
								//sadsad
								String historyLease = "Insert into lease_hist "
									+ "select * from lease where lease_no = ?";
									
									System.out
											.println("Creating backup for personID "
													+ personID);
									// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
									String historyQuery = "Insert into person_accomodation_lease_hist "
											+ "select * from person_accomodation_lease where person_id = ?";
									preHist = histconn
											.prepareStatement(historyQuery);
									preHist.setInt(1, personID);
									preHist.executeUpdate();
									preHist.close();
									ConnectionUtils.closeConnection(histconn);
									
									
									preHist1 = histconn1.prepareStatement(historyLease);
									preHist1.setInt(1, leaseNumber);
									preHist1.executeUpdate();
									preHist1.close();
									ConnectionUtils.closeConnection(histconn);
									
									
								} else {
									System.out
											.println("Already backed up for this person");
								}
							}

							PreparedStatement preparedStatement3 = null;
							ResultSet rsToDelete = null;

							String deleteQuery = "DELETE FROM PERSON_ACCOMODATION_LEASE WHERE person_id=?";
							preparedStatement3 = dbConnection
									.prepareStatement(deleteQuery);
							preparedStatement3.setInt(1, personID);
							preparedStatement3.executeUpdate();

						}

					}

					else {
						System.out
								.println("The person ID ["
										+ personID
										+ "] has paid his dues and not present anymore");

					}

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

	private int returnDeposit(int id, String type) {
		int securityDeposit = 0;

		PreparedStatement ps = null;
		Connection conn = ConnectionUtils.getConnection();
		ResultSet getDeposit = null;
		String hallQuery = "SELECT security_deposit from residence_hall where hall_number = (select hall_number from residence_hall_provides_room where accomodation_id = ?)";
		String bedroomQuery = "SELECT security_deposit from general_apartment where apt_no = (SELECT apt_no from bedroom where accomodation_id = ?)";
		String familyQuery = "SELECT security_deposit from family_apartment where accomodation_id = ?";
		try {
			if (type.equalsIgnoreCase(Constants.RESIDENCE_HALL)) {
				ps = conn.prepareStatement(hallQuery);
				ps.setInt(1, id);
			} else if (type.equalsIgnoreCase(Constants.GENERAL_APARTMENT)) {
				ps = conn.prepareStatement(bedroomQuery);
				ps.setInt(1, id);
			} else if (type.equalsIgnoreCase(Constants.FAMILY_APARTMENT)) {
				ps = conn.prepareStatement(familyQuery);
				ps.setInt(1, id);
			}

			getDeposit = ps.executeQuery();
			while (getDeposit.next()) {
				securityDeposit = getDeposit.getInt("security_deposit");
			}
			ps.close();
			ConnectionUtils.closeConnection(conn);
			getDeposit.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return securityDeposit;
	}

	public boolean checkIfPersonLeaseExistsInHistory(int personId,
			int leaseNumber) {
		try {
			int count = 0;
			/*
			 * Write SQL Query to put the terminated lease request into
			 * person_accomodation_lease_hist
			 */
			Connection conn = ConnectionUtils.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			System.out.println("Chekcing if lease_no " + leaseNumber
					+ " and personId " + personId + " backup already exists");
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String historyQuery = "select count(*) as does_exist from person_accomodation_lease_hist where person_id = ? and lease_no = ?";
			ps = conn.prepareStatement(historyQuery);
			ps.setInt(1, personId);
			ps.setInt(2, leaseNumber);
			rs = ps.executeQuery();
			while (rs.next()) {
				count = rs.getInt("does_exist");
				if (count > 0)
					return true;
			}
			ps.close();
			ConnectionUtils.closeConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
