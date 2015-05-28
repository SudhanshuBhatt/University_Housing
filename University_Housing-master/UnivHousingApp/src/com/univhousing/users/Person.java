package com.univhousing.users;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.main.Utils;

public class Person {

	//public int personId;
	public String firstName;
	public String lastName;
	public String streetName;
	public String city;
	public int postCode;
	public long phoneNumber;
	public String gender;
	public java.sql.Date DOB;
	
	
	Student studentObj = new Student();
	Scanner inputObj = new Scanner(System.in);
	Guest guestObj = new Guest();
	NextOfKin nokObj = new NextOfKin();
	/**
	 * @param personId But for guests this parameter is personId
	 * @action Displays name, number, address, dob, gender, category (freshman, sophomore, etc) family details
	 *  if needed, special needs etc (refer the description for a comprehensive list)
	 */
	public void viewProfileDetails(int personId) {
		int studentId = studentObj.getStudentIdForPersonId(personId);
		System.out.println("Person Id is: "+personId);
		System.out.println("Student Id is: "+studentId);
		String nationality = "";
		String smoker = "";
		String specialNeeds = "";
		String housingStatus = "";
		String studyField = "";
		/*Write SQL Query for fetching:
		 * name, number, address, dob, gender, category (freshman, sophomore, etc) family details (from NextOfKin)
		 * if needed, special needs for a student.
		 * There might be more than these please check the project document for this*/
		
			


		ResultSet studentProfile = null;
		ResultSet studentType = null;
		Connection dbConnection1 = ConnectionUtils.getConnection();
		Connection dbConnection = ConnectionUtils.getConnection();
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		String queryPersonProfile = "";
		String queryStudentType = "";
		
		try
		{
			queryPersonProfile = "SELECT P.first_name, P.last_name, P.street_no, P.city, P.postcode, P.phone_number, P.gender," +
					"P.DOB, P.nationality, P.smoker, P.special_needs, P.housing_status, P.study_field FROM Person P WHERE P.person_id = ?";
			queryStudentType = "SELECT S.student_type FROM Student S WHERE S.student_id = ?";
			preparedStatement1 = dbConnection1.prepareStatement(queryPersonProfile);
			preparedStatement1.setInt(1, personId);
			studentProfile = preparedStatement1.executeQuery();

			while(studentProfile.next())
			{
				firstName = studentProfile.getString("first_name");
				lastName = studentProfile.getString("last_name");
				streetName = studentProfile.getString("street_no");
				city = studentProfile.getString("city");
				postCode = studentProfile.getInt("postcode");
				phoneNumber = studentProfile.getLong("phone_number");
				gender = studentProfile.getString("gender");
				DOB = studentProfile.getDate("DOB");
				nationality = studentProfile.getString("nationality");
				smoker = studentProfile.getString("smoker");
				specialNeeds = studentProfile.getString("special_needs");
				housingStatus = studentProfile.getString("housing_status");
				studyField = studentProfile.getString("study_field");
				
				System.out.println("Name: "+firstName+" "+lastName+"\n"+"Address: "+streetName+","+city+","+postCode+"\t");
				System.out.println("Phone Number: "+phoneNumber+"\n"+"Gender: "+gender+"\nDOB: "+DOB);
				System.out.println("Nationality: "+nationality+"\n"+"Smoker: "+smoker);
				System.out.println("Special Needs: "+specialNeeds+"\n"+"Housing Status: "+housingStatus);
				System.out.println("Study Field: "+studyField);
			}
			
			if(!guestObj.checkPersonIsGuest(personId))
			{
				preparedStatement2 = dbConnection.prepareStatement(queryStudentType);
				preparedStatement2.setInt(1, studentId);
				studentType = preparedStatement2.executeQuery();
				String studentCategory = "";

				while(studentType.next())
				{
					studentCategory = studentType.getString("student_type");
				}
				System.out.println("Category: "+ studentCategory);
				dbConnection.close();
			}
			System.out.println("Now displaying your Next of Kin details:\n");
			nokObj = new NextOfKin();
			nokObj.getNextOfKinDetails(studentId);
			if(!nokObj.checkIfNextOfKinPresent(studentId))
				System.out.println("No Kin details saved, please update profile to add details");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * @param personId
	 * @throws ParseException 
	 * @action First fetches all the profile details and then asks new values for all of them.
	 */
	public void updateProfile(int personId) throws ParseException 
	{

		int studentId;
		// Fetch the student Id for a particular Person Id
		studentId = studentObj.getStudentIdForPersonId(personId);
		
		/*Write SQL Query for fetching:
		 * name, number, address, dob, gender, category (freshman, sophomore, etc) family details (from NextOfKin)
		 * if needed, special needs for a student.
		 * There might be more than these please check the project document for this*/		
		
		System.out.println("Enter new values for the fields you want," +
				" and leave the fields you don't want changes as Blank");
		
		System.out.println("First Name: ");
		String newFirstName = inputObj.nextLine();
		
		System.out.println("Last Name: ");
		String newLastName = inputObj.nextLine();
		
		System.out.println("Street Name.: ");
		String newStreetNo = inputObj.nextLine();
		
		System.out.println("City: ");
		String newCity = inputObj.nextLine();
		
		System.out.println("DOB in MM/dd/yyyy format: ");
		String newDOBString = inputObj.nextLine();
		java.sql.Date newDOB = Utils.convertStringToSQLDateFormat(newDOBString);
		
		System.out.println("Gender: ");
		String newGender = inputObj.nextLine();

		System.out.println("Student Category: ");
		String newStudentType = inputObj.nextLine();
		
		System.out.println("Special Needs");
		String newSpecialNeeds = inputObj.nextLine();
		
		System.out.println("Nationality");
		String newNationality = inputObj.nextLine();
		
		System.out.println("Smoker?");
		String newSmoker = inputObj.nextLine();
		
		System.out.println("Study Field");
		String newStudyField = inputObj.nextLine();
		
		System.out.println("ZipCode: ");
		int newZipCode = inputObj.nextInt();
		
		System.out.println("Phone Number: ");
		long newPhone = inputObj.nextLong();
		
		System.out.println("Note: You can't change you own Housing Status");

		try
		{
			PreparedStatement preparedStatement1 = null;
			Connection dbConnection1 = ConnectionUtils.getConnection();
			
			String query1 = "UPDATE Person SET first_name = ?, last_name = ?, street_no = ?, city = ?," +
					" postcode = ?, phone_number = ?, gender = ?, DOB = ?, nationality = ?, smoker = ?, special_needs = ?, study_field = ? WHERE person_id = ?";
			preparedStatement1 = dbConnection1.prepareStatement(query1);
			preparedStatement1.setString(1, newFirstName);
			preparedStatement1.setString(2, newLastName);
			preparedStatement1.setString(3, newStreetNo);
			preparedStatement1.setString(4, newCity);
			preparedStatement1.setInt(5, newZipCode);
			preparedStatement1.setLong(6, newPhone);
			preparedStatement1.setString(7, newGender);
			preparedStatement1.setDate(8, newDOB);
			preparedStatement1.setString(9, newNationality);
			preparedStatement1.setString(10, newSmoker);
			preparedStatement1.setString(11, newSpecialNeeds);
			preparedStatement1.setString(12, newStudyField);
			preparedStatement1.setInt(13, personId);
			
			preparedStatement1.executeUpdate();
			
			
			if(!guestObj.checkPersonIsGuest(personId))
			{
				PreparedStatement preparedStatement2 = null;
				Connection dbConnection2 = ConnectionUtils.getConnection();
				
				String query2 = "UPDATE Student SET student_type = ? WHERE student_id = ? ";
				preparedStatement2 = dbConnection2.prepareStatement(query2);
				preparedStatement2.setString(1, newStudentType);
				preparedStatement2.setInt(2, studentId);
				preparedStatement2.executeUpdate();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		System.out.println("Please enter Next Of Kin Details now: ");
		
		// Consuming the enter after last input 
		inputObj.nextLine();
		
		System.out.println("Next of Kin First Name:");
		String newNOKFirstName = inputObj.nextLine();
		
		System.out.println("Next of Kin Last Name: ");
		String newNOKLastName = inputObj.nextLine();
		
		System.out.println("Next of Kin Street Name.: ");
		String newNOKStreetNo = inputObj.nextLine();
		
		System.out.println("Next of Kin City: ");
		String newNOKCity = inputObj.nextLine();
		
		System.out.println("Next of Kin Gender: ");
		String newNOKGender = inputObj.nextLine();
		
		System.out.println("Next of Kin DOB in MM/dd/yyyy format: ");
		String newStringNOKDOB = inputObj.nextLine();
		java.sql.Date newNOKDOB = Utils.convertStringToSQLDateFormat(newStringNOKDOB);
		
		System.out.println("Next of Kin ZipCode: ");
		int newNOKZipCode = inputObj.nextInt();
		
		System.out.println("Next of Kin Phone Number: ");
		long newNOKPhone = inputObj.nextLong();
		
		// Consuming the enter after last input 
		inputObj.nextLine();
		
		/*Write SQL Query to to update the above information for the retrieved Student ID
		 * Students and NextOfKin(NOK) both details have to be updated.*/
		
		if(nokObj.checkIfNextOfKinPresent(studentId))
		{
			try
			{
				PreparedStatement preparedStatement3 = null;
				Connection dbConnection3 = ConnectionUtils.getConnection();
				
				String query3 = "UPDATE kin_person SET first_name = ?, last_name = ?, street_name = ?," +
						" city_name = ?, zip_code = ?, phone_number = ?, gender = ?, DOB = ? WHERE person_id = ?";
				preparedStatement3 = dbConnection3.prepareStatement(query3);
				preparedStatement3.setString(1, newNOKFirstName);
				preparedStatement3.setString(2, newNOKLastName);
				preparedStatement3.setString(3, newNOKStreetNo);
				preparedStatement3.setString(4, newNOKCity);
				preparedStatement3.setInt(5, newNOKZipCode);
				preparedStatement3.setLong(6, newNOKPhone);
				preparedStatement3.setString(7, newNOKGender);
				preparedStatement3.setDate(8, newNOKDOB);
				preparedStatement3.setInt(9, personId);
				preparedStatement3.executeUpdate();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				PreparedStatement preparedStatement3 = null;
				Connection dbConnection3 = ConnectionUtils.getConnection();
				
				String query3 = "INSERT INTO kin_person (person_id,first_name,last_name,street_name,city_name,zip_code,phone_number,gender,DOB) VALUES(?,?,?,?,?,?,?,?,?)";
				preparedStatement3 = dbConnection3.prepareStatement(query3);
				preparedStatement3.setInt(1, studentId);
				preparedStatement3.setString(2, newNOKFirstName);
				preparedStatement3.setString(3, newNOKLastName);
				preparedStatement3.setString(4, newNOKStreetNo);
				preparedStatement3.setString(5, newNOKCity);
				preparedStatement3.setInt(6, newNOKZipCode);
				preparedStatement3.setLong(7, newNOKPhone);
				preparedStatement3.setString(8, newNOKGender);
				preparedStatement3.setDate(9, newNOKDOB);
				preparedStatement3.executeUpdate();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param personId
	 * @param status 
	 * @action This method is introduced because a housing_status field has been added to Person table which says whether his housing status is
	 * Place or Waiting
	 */
	public void updateHousingStatus(int personId, String status) {
		
		PreparedStatement ps = null;
		Connection conn = ConnectionUtils.getConnection();
		String query = "UPDATE Person SET housing_status = ? WHERE person_id = ?";
		try 
		{
			ps = conn.prepareStatement(query);
			ps.setString(1, status);
			ps.setInt(2, personId);
			ps.executeUpdate();
			ConnectionUtils.closeConnection(conn);
			ps.close();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @param personId
	 * @action Displays the first and last name of the person who has logged in
	 */
	public void fetchFirstAndLastName(int personId)
	{
		String fName = "";
		String lName = "";
		PreparedStatement ps = null;
		Connection conn = ConnectionUtils.getConnection();
		String query = "SELECT first_name,last_name from person where person_id = ?";
		ResultSet rs = null;
		try
		{
			ps = conn.prepareStatement(query);
			ps.setInt(1, personId);
			rs = ps.executeQuery();
			
			while(rs.next())
			{
				fName = rs.getString("first_name");
				lName = rs.getString("last_name");
			}
			System.out.println("Welcome "+fName+" "+lName);
			rs.close();
			ps.close();
			ConnectionUtils.closeConnection(conn);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
