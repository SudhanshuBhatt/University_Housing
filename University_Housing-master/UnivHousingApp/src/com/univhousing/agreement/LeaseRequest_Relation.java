package com.univhousing.agreement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.univhousing.accomodation.ResidenceHall;
import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.main.Utils;
import com.univhousing.users.Guest;
import com.univhousing.users.Person;
import com.univhousing.users.Student;

public class LeaseRequest_Relation {

	public static final int A = 5;
	public static final int B = 5;
	public static final int C = 2;
	public int applicationRequestNo;
	public String accomodationType;
	public int personId;
	public int staffNo;
	public ArrayList<String> residenceHallNames = null;
	Person personObj ;
	Student studentObj = new Student();
	Guest guestObj = new Guest();
	ResidenceHall residentObj = new ResidenceHall();
	/**
	 * @param personId
	 * @throws ParseException
	 * @action Generates a new Lease request and Inserts the new data into
	 *         respective tables
	 */
	public void generateNewLeaseRequest(int personId) throws ParseException {

		Scanner inputObj = new Scanner(System.in);
		personObj = new Person();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String startSemester = "";

		try {

			/* Checking if the student is FRESHMEN OR OTHERS */

			boolean isFreshmen = false;
			String housingOption;
			String paymentGateway = "";
			String hallPreference1 = "";
			String hallPreference2 = "";
			String hallPreference3 = "";
			String paymentGatewayOption;
			String studentName = null;
			dbConnection = ConnectionUtils.getConnection();
			String SelectQuery1 = "SELECT S.Student_type, P.First_Name FROM STUDENT S,Person P WHERE S.Person_id=? AND P.person_id=S.person_id";

			preparedStatement = dbConnection.prepareStatement(SelectQuery1);
			preparedStatement.setInt(1, personId);
			rs = preparedStatement.executeQuery();

			if (rs.isBeforeFirst()) {
				rs.next();

				studentName = rs.getString("first_name");
				String tempStr = rs.getString("Student_type");

				if (tempStr.equals(Constants.FRESHMEN)) {
					isFreshmen = true;

				}

			}

			rs.close();
			preparedStatement.close();

			/***********************************************************
			 * Firing a query for getting the last Application_request_no so
			 * that we can add 1 and generate next request number
			 */

			String SelectQuery2 = "SELECT MAX(application_request_no)as application_request_no FROM PERSON_ACC_STAFF";

			//boolean flagGuest = false;
			preparedStatement = dbConnection.prepareStatement(SelectQuery2);
			rs = preparedStatement.executeQuery();

			rs.next();
			int newApplicationRequestNumber = rs
					.getInt("application_request_no") + 1;

			rs.close();
			preparedStatement.close();

			

			
			/************************************************************************/

			// System.out.println("IMPORTANT INFORMATION : One semester is equal to 4 months");

			String[] tempStrArray = null;
			int periodOfLeasing;
			if (!guestObj.checkPersonIsGuest(personId)) 
			{
				System.out.println("Enter the semesters you want to live ");
				System.out.println("=======================================");
				System.out.println("a> Sem-1	: [01 Aug - 31 Dec]");
				System.out.println("b> Sem-2	: [01 Jan - 31 May]");
				System.out.println("c> Summer	: [01 Jun - 31 Jul]");
				System.out
						.println("For multiple semesters please enter options seperated by comma[for example: a,b,c] ");
				
				String sem = inputObj.nextLine();
//				inputObj.nextLine();
				
				while(!sem.equalsIgnoreCase("a") && !sem.equalsIgnoreCase("b") && 
						!sem.equalsIgnoreCase("c") && !sem.equalsIgnoreCase("a,b") && !sem.equalsIgnoreCase("b,a")
						&& !sem.equalsIgnoreCase("a,c") && !sem.equalsIgnoreCase("c,a")
						 && !sem.equalsIgnoreCase("b,c") && !sem.equalsIgnoreCase("c,b")
						 && !sem.equalsIgnoreCase("a,b,c") && !sem.equalsIgnoreCase("a,c,b")
						 && !sem.equalsIgnoreCase("b,c,a") && !sem.equalsIgnoreCase("b,a,c")
						 && !sem.equalsIgnoreCase("c,b,a") && !sem.equalsIgnoreCase("c,a,b"))
				{
					System.out.println("Please enter a valid input:");
					sem = inputObj.nextLine();
				}
				
				
				tempStrArray = sem.split(",");
				int periodOfLeaseInt = 0;
				int totalDuration = 0;
				for (int i = 0; i < tempStrArray.length; i++) 
				{

					if (tempStrArray[i].equalsIgnoreCase("a")) {
						periodOfLeaseInt = periodOfLeaseInt + A;
					} else if (tempStrArray[i].equalsIgnoreCase("b")) {
						periodOfLeaseInt = periodOfLeaseInt + B;
					} else if (tempStrArray[i].equalsIgnoreCase("c")) {
						periodOfLeaseInt = periodOfLeaseInt + C;
					}

					totalDuration = periodOfLeaseInt;
				}
				periodOfLeasing = totalDuration;
			}
			else{
				
				System.out.println("Enter the duration for which you want stay here as a visitor (max 12 months)");
				periodOfLeasing = inputObj.nextInt();
				if(!(periodOfLeasing >0 && periodOfLeasing <=12))
				{
					System.out.println("You entered wrong duration , hence it has been set to 12 months");
					periodOfLeasing = 12;
				}	
			}

			if (isFreshmen == false) {
				System.out.println("Welcome !!");
				System.out.println("Enter your housing preference\n"
						+ "a) Private Housing\n" + "b) Residence Hall\n"
						+ "c) General Apartment\n" + "d) Family Apartment\n"
						+ "Choose appropriate option");

				housingOption = inputObj.next();
				while(!(housingOption.equalsIgnoreCase("a")||housingOption.equalsIgnoreCase("b")||housingOption.equalsIgnoreCase("c")||housingOption.equalsIgnoreCase("d")))
				{
					System.out.println("You have entered the wrong option, please enter again");
					housingOption = inputObj.next();
				}
				if (housingOption.equals("a")) {
					housingOption = "Private Housing";
					ArrayList<String> privateAccoTypes = new ArrayList<String>();
					privateAccoTypes.add("Private Shared House");
					privateAccoTypes.add("Private General Apartment");
					privateAccoTypes.add("Private Family Apartment");
					System.out.println("Enter your 3 preferences :\n");
					String[] prefs = new String[100];
					int count = 0;
					while(count<3)
					{
						System.out.println("Enter preference "+(count+1)+": ");
						for (int i = 0; i < privateAccoTypes.size(); i++) {
							System.out.println(i+1+") "+privateAccoTypes.get(i));
						}
						int choice = inputObj.nextInt();
						prefs[count] = privateAccoTypes.get(choice-1);
						count++;
					}
					count = 0;
					//inputObj.nextLine();
					
					hallPreference1 = prefs[0];
					hallPreference2 = prefs[1];
					hallPreference3 = prefs[2];
				} 
				else if (housingOption.equals("b")) {
					housingOption = Constants.RESIDENCE_HALL;
					

					residenceHallNames = getHallNameList(personId);
					

					hallPreference1 = residenceHallNames.get(0);
					hallPreference2 = residenceHallNames.get(1);
					hallPreference3 = residenceHallNames.get(2);

				} else if (housingOption.equals("c")) {
					housingOption.replaceAll("c", Constants.GENERAL_APARTMENT);
					housingOption = Constants.GENERAL_APARTMENT;
					hallPreference1 = "NA";
					hallPreference2 = "NA";
					hallPreference3 = "NA";
				} else if (housingOption.equals("d")) {
					housingOption = Constants.FAMILY_APARTMENT;
					hallPreference1 = "NA";
					hallPreference2 = "NA";
					hallPreference3 = "NA";
				}

			}

			else {

				System.out.println("Welcome " + studentName + "!");
				System.out
						.println("Enter your housing preference: \n"
								+ "a) Residence Hall\n"
								+ "b) General Apartment\n"
								+ "c) Family Apartment\n"
								+ "Choose appropriate option");
				housingOption = inputObj.next();
				while(!(housingOption.equalsIgnoreCase("a")||housingOption.equalsIgnoreCase("b")||housingOption.equalsIgnoreCase("c")))
				{
					System.out.println("You have entered the wrong option, please enter again");
					housingOption = inputObj.next();
				}
				if (housingOption.equals("a")) {
					housingOption = Constants.RESIDENCE_HALL;

					residenceHallNames = getHallNameList(personId);
					hallPreference1 = residenceHallNames.get(0);
					hallPreference2 = residenceHallNames.get(1);
					hallPreference3 = residenceHallNames.get(2);
				} else if (housingOption.equals("b")) {
					housingOption = Constants.GENERAL_APARTMENT;
					hallPreference1 = "NA";
					hallPreference2 = "NA";
					hallPreference3 = "NA";
				} else if (housingOption.equals("c")) {
					housingOption = Constants.FAMILY_APARTMENT;
					hallPreference1 = "NA";
					hallPreference2 = "NA";
					hallPreference3 = "NA";
				}

			}
			String moveInDate = "";



			System.out
					.println("Date you want to enter the housing in MM/dd/YYYY format: ");
			moveInDate = inputObj.next();

			if(!guestObj.checkPersonIsGuest(personId))
			{
				String[] tempDateStr = moveInDate.split("/");
				if(tempStrArray[0].equalsIgnoreCase("a"))
				{
					startSemester = "Semester 1";
					while(!checkCorrectDate(tempDateStr,tempStrArray[0]))
					{
						System.out.println("You have entered wrong move in date for Sem 1, you move in date can only be 08/01/2015, " +
								"do you want 08/01/2015 as your  move in date [Y/N]? ");
						String choice1 = inputObj.next();
						if (choice1.equalsIgnoreCase("Y"))
						{
							moveInDate = "08/01/2015";
							break;
						}
						else if(choice1.equalsIgnoreCase("N"))
							break;
						
					}
				}	
				else if(tempStrArray[0].equalsIgnoreCase("b"))
				{
					startSemester = "Semester 2";
					while(!checkCorrectDate(tempDateStr,tempStrArray[0]))
					{
						System.out.println("You have entered wrong move in date for Sem 2, you move in date can only be 08/01/2015, " +
								"do you want 01/01/2015 as your  move in date [Y/N]? ");
						String choice1 = inputObj.next();
						if (choice1.equalsIgnoreCase("Y"))
						{
							moveInDate = "01/01/2015";
							break;
						}
						else if(choice1.equalsIgnoreCase("N"))
							break;
					}
				}
				else if(tempStrArray[0].equalsIgnoreCase("c"))
				{
					startSemester = "Summer";
					while(!checkCorrectDate(tempDateStr,tempStrArray[0]))
					{
						System.out.println("You have entered wrong move in date for Summer semester, you move in date can only be 08/01/2015, " +
								"do you want 06/01/2015 as your  move in date [Y/N]? ");
						String choice1 = inputObj.next();
						if (choice1.equalsIgnoreCase("Y"))
						{
							moveInDate = "06/01/2015";
							break;
						}
						else if(choice1.equalsIgnoreCase("N"))
							break;
					}
				}
				
			}
			if (!guestObj.checkPersonIsGuest(personId)) 
			{/*
				System.out.println("This is a student");
				
				
				if (tempStrArray[0].equalsIgnoreCase("a")) 
				{
					System.out.println("Chose semester A");
					if (tempDateStr[0].equalsIgnoreCase(monthA)
							&& tempDateStr[1].equalsIgnoreCase(dateA))
					{
						System.out.println("Date is correct");
					}
					else
					{
						while(true)
						{
							System.out
									.println("You have entered wrong move in date for Sem 1, do you still want to proceed[Y/N] ");
							String choice1 = inputObj.next();
							if (choice1.equalsIgnoreCase("Y"))
								break;
						}
					}
				}
				if (tempStrArray[0].equalsIgnoreCase("b")) 
				{
					System.out.println("Chose semester B");
					if (tempDateStr[0].equalsIgnoreCase(monthB)
							&& tempDateStr[1].equalsIgnoreCase(dateB)) 
					{
						
					} 
					else
					{
						while(true)
						{
							System.out
									.println("You have entered wrong move in date for Sem 2, do you still want to proceed[Y/N] ");
							String choice1 = inputObj.next();
							if (choice1.equalsIgnoreCase("Y"))
								break;
						}
					}
				}
				if (tempStrArray[0].equalsIgnoreCase("c")) 
				{
					System.out.println("Chose semester C");
					if (tempDateStr[0].equalsIgnoreCase(monthC)
							&& tempDateStr[1].equalsIgnoreCase(dateC)) 
					{

					} 
					else
					{
						while(true)
						{
							System.out
									.println("You have entered wrong move in date for Sem 3, do you still want to proceed[Y/N] ");
							String choice1 = inputObj.next();
							if (choice1.equalsIgnoreCase("Y"))
								break;
						}
					}
				}
			*/}
			System.out.println("Please choose your payment gateway");
			System.out.println("a. Cash\n"
					+ "b. Credit Card\n"
					+ "c. Debit Card\n"
					+ "d. Money Order\n");
			
			paymentGatewayOption = inputObj.next();
			if (paymentGatewayOption.equalsIgnoreCase("a")) {
				paymentGateway = "Cash";
			} else if (paymentGatewayOption.equalsIgnoreCase("b")) {
				paymentGateway = "Credit Card";
			} else if (paymentGatewayOption.equalsIgnoreCase("c")) {
				paymentGateway = "Debit Card";
			} else if (paymentGatewayOption.equalsIgnoreCase("d")) {
				paymentGateway = "Money Order";
			} else {
				System.out.println("Please enter a proper option");
			}
			
			System.out.println("Selected payment gateway: " + paymentGateway);
			/*SELECTING STAFF NO BLOCK START*/
			String query3 = "SELECT MAX(staff_no) AS LastStaff FROM Housing_Staff";
			int staffNoMax = 0;
			int staffNoMin=0;
			try 
			{
				preparedStatement = dbConnection.prepareStatement(query3);
				ResultSet max = preparedStatement.executeQuery();
				while(max.next())
				{
					staffNoMax = max.getInt("LastStaff");
				}
				
				max.close();
				preparedStatement.close();
			}
			catch(SQLException e)
			{
				try {
					ConnectionUtils.closeConnection(dbConnection);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			
			//PreparedStatement preparedStatement = null;
			//Connection conn1 = ConnectionUtils.getConnection();
			String query4 = "SELECT MIN(staff_no) AS FirstStaff FROM Housing_Staff";
			
			try 
			{
				preparedStatement = dbConnection.prepareStatement(query4);
				ResultSet min = preparedStatement.executeQuery();
				while(min.next())
				{
					staffNoMin = min.getInt("FirstStaff");
				}
			}
			catch(SQLException e)
			{
				try {
					ConnectionUtils.closeConnection(dbConnection);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			
			// Getting a random staff number between First and the Last staff
			staffNo = Utils.randomizeStaff(staffNoMax, staffNoMin);
			
			
			
			/*SELECTING STAFF NI BLOCK END*/
			
			java.sql.Date sqlMoveInDate = Utils
					.convertStringToSQLDateFormat(moveInDate);

			System.out.println("Enter the payment options: \n" + "a) Monthly\n"
					+ "b) Semester\n");
			String paymentOption = inputObj.next();
			if (paymentOption.equals("a"))
				paymentOption = Constants.PAYMENTOPTION_MONTHLY;
			else if (paymentOption.equals("b"))
				paymentOption = Constants.PAYMENTOPTION_SEMESTER;

			System.out.println("Do you want to: \n" + "1. Submit\n"
					+ "2. Back ");

			int choice = inputObj.nextInt();
			if (choice == 1) {

				String reqStatus = Constants.PENDING_STATUS;
				String selectQuery3 = "INSERT INTO PERSON_ACC_STAFF (application_request_no,accomodation_type,person_id,request_status, "
						+ "mode_of_payment,lease_move_in_date,duration,PREFERENCE1,PREFERENCE2,PREFERENCE3,staff_no,payment_gateway,start_semester) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

				preparedStatement = dbConnection.prepareStatement(selectQuery3);
				preparedStatement.setInt(1, newApplicationRequestNumber);
				preparedStatement.setString(2, housingOption);
				preparedStatement.setInt(3, personId);
				preparedStatement.setString(4, reqStatus);
				preparedStatement.setString(5, paymentOption);
				preparedStatement.setDate(6, sqlMoveInDate);
				preparedStatement.setInt(7, periodOfLeasing);
				preparedStatement.setString(8, hallPreference1);
				preparedStatement.setString(9, hallPreference2);
				preparedStatement.setString(10, hallPreference3);
				preparedStatement.setInt(11, staffNo);
				preparedStatement.setString(12, paymentGateway);
				preparedStatement.setString(13, startSemester);
				preparedStatement.executeUpdate();

				System.out
						.println("Success!!!  Please Note down the Request Number for future References=>"
								+ newApplicationRequestNumber);
				
				// Now updating the housing_status for this person to Waiting
				personObj.updateHousingStatus(personId,Constants.WAITING_STATUS);

			} else if (choice == 2) {
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

	/* RETRIEVING SELECTING THE SPECIFIC RESIDENCE HALLS */

	private ArrayList<String> getHallNameList(int personId) {
		Scanner inputObj = new Scanner(System.in);
		ArrayList<String> hallList = new ArrayList<String>();
		ArrayList<String> hallOptions = new ArrayList<String>(3);

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;

		try {

			dbConnection = ConnectionUtils.getConnection();

			String selectQuery1 = "SELECT HALL_NAME FROM RESIDENCE_HALL";
			int preferenceCounter = 1;
			preparedStatement = dbConnection.prepareStatement(selectQuery1);
			rs = preparedStatement.executeQuery();
			String hallNameToAdd = "";
			boolean studentEligibility = studentObj.isElligibleForGraduateResidentHall(personId);
			boolean hallEligibility;
			while (rs.next()) {
				hallNameToAdd = rs.getString("HALL_NAME");
				hallEligibility = residentObj.isGraduateHall(hallNameToAdd);
				if(hallEligibility)
				{
					if(studentEligibility)
					{
						hallList.add(hallNameToAdd);
					}
				}
				else
				{
					hallList.add(hallNameToAdd);
				}
				
			}

			while (preferenceCounter < 4) {

				System.out.println("Enter Preference No. " + preferenceCounter
						+ "=> ");

				for (int i = 0; i < hallList.size(); i++) {

					System.out.println(i + 1 + "." + hallList.get(i));

				}

				int option = inputObj.nextInt();
				hallOptions.add(hallList.get(option - 1));

				preferenceCounter++;
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

		return hallOptions;
	}
	
	
	public boolean checkCorrectDate(String[] tempDateStr, String startMonth)
	{
		String dateA = "01";
		String monthA = "08";
		String dateB = "01";
		String monthB = "01";
		String dateC = "01";
		String monthC = "06";
		
		if (startMonth.equalsIgnoreCase("a") && tempDateStr[0].equalsIgnoreCase(monthA)	&& tempDateStr[1].equalsIgnoreCase(dateA))
			return true;
		if(startMonth.equalsIgnoreCase("b") && tempDateStr[0].equalsIgnoreCase(monthB)	&& tempDateStr[1].equalsIgnoreCase(dateB))
			return true;
		if(startMonth.equalsIgnoreCase("c") && tempDateStr[0].equalsIgnoreCase(monthC)	&& tempDateStr[1].equalsIgnoreCase(dateC))
			return true;
		return false;
	}
	
	public boolean checkAnyCorrectDate(String[] tempDateStr, String startSemester)
	{
		String dateA = "01";
		String monthA = "08";
		String dateB = "01";
		String monthB = "01";
		String dateC = "01";
		String monthC = "06";
		
		if (startSemester.equalsIgnoreCase("Semester 1") && tempDateStr[0].equalsIgnoreCase(monthA)	&& tempDateStr[1].equalsIgnoreCase(dateA))
			return true;
		if(startSemester.equalsIgnoreCase("Semester 2") && tempDateStr[0].equalsIgnoreCase(monthB)	&& tempDateStr[1].equalsIgnoreCase(dateB))
			return true;
		if(startSemester.equalsIgnoreCase("Summer") && tempDateStr[0].equalsIgnoreCase(monthC)	&& tempDateStr[1].equalsIgnoreCase(dateC))
			return true;
		return false;
	}
}
