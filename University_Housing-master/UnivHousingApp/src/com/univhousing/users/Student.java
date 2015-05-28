package com.univhousing.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;

public class Student {

	public int studentId;
	public int personId;
	public String studentType;

	public String studentDesignation = "";

	/**
	 * @param personId
	 * @return PersonID for the givenStudentID, used for authentication
	 */
	public int getPersonIdForStudentId(int studentId) {
		int personId = 0;
		/*
		 * Write SQL Query to fetch the person Id for the corresponding Student
		 * id that the student enters. This is done because our authentication
		 * is based on PersonId
		 */

		try {
			ResultSet getPersonId = null;
			Connection dbConnection = ConnectionUtils.getConnection();
			String query = "SELECT person_id FROM Student WHERE student_id = ?";
			PreparedStatement preparedStatement = dbConnection
					.prepareStatement(query);

			preparedStatement.setInt(1, studentId);
			getPersonId = preparedStatement.executeQuery();

			while (getPersonId.next()) {
				personId = getPersonId.getInt("person_id");
			}
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return personId;
	}

	/**
	 * @param personId
	 * @return StudentId for the given personId
	 */
	public int getStudentIdForPersonId(int personId) {
		int studentId = 0;
		/*
		 * Write SQL Query to fetch the student Id for personId Use the instance
		 * Variable to save the student's ID and return it;
		 */

		// SELECT student_id FROM student WHERE person_id = ?
		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		try {
			dbConnection = ConnectionUtils.getConnection();

			String selectQuery = "SELECT student_id FROM student WHERE person_id = ? ";

			preparedStatement = dbConnection.prepareStatement(selectQuery);

			preparedStatement.setInt(1, personId);

			rs = preparedStatement.executeQuery();

			// If record exists , rs.next() will evaluate to true
			if (rs.isBeforeFirst()) {
				rs.next();
				System.out.print(rs.getInt("student_id") + "\t\t");
				studentId = rs.getInt("student_id");
			}

		} catch (SQLException e1) {
			System.out.println("getStudentIdForPersonId SQLException: "
					+ e1.getMessage());
			System.out.println("getStudentIdForPersonId VendorError: "
					+ e1.getErrorCode());
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
		return studentId;
	}

	/**
	 * @param studentId
	 * @action Checks if the studentId s enrolled in University Housing
	 * @return True is the studentId is enrolled in university housing else
	 *         returns False
	 */
	public boolean checkStudentInUnivHousing(int personId) {
		System.out.println("PERSON ID : " + personId);
		boolean isStudentHavingAccomodation = false;
		String requestStatus;
		/*
		 * Write SQL Query to check if the studentId has an accommodation and
		 * only then will the student be allowed parking spot Then set
		 * isStudentAccomodated as true
		 */

		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		try {
			dbConnection = ConnectionUtils.getConnection();

			String selectQuery = "SELECT count(*) AS total_count FROM PERSON_ACCOMODATION_LEASE WHERE person_id = ?";

			preparedStatement = dbConnection.prepareStatement(selectQuery);

			preparedStatement.setInt(1, personId);

			rs = preparedStatement.executeQuery();
			int totalCount = 0;
			if (rs.isBeforeFirst()) {
				rs.next();
				totalCount = rs.getInt("total_count");
				
				if(totalCount != 0)
				isStudentHavingAccomodation = true;
			}
			

		} catch (SQLException e1) {
			e1.printStackTrace();
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

		System.out.println("isStudentHavingAccomodation: "
				+ isStudentHavingAccomodation);
		return isStudentHavingAccomodation;
	}

	/**
	 * @param personId
	 * @return
	 */
	public String getStudentDesignation(int personId) {
		String studentDesignation = "";

		/*
		 * Write SQL Query to get the student's designation from
		 * login_credentials table i think
		 */
		try {
			ResultSet getStudentDesignation = null;
			PreparedStatement preparedStatement = null;
			Connection dbConnection = ConnectionUtils.getConnection();

			String query = "SELECT designation  FROM login_credentials WHERE person_id = ?";
			preparedStatement = dbConnection.prepareStatement(query);
			preparedStatement.setInt(1, personId);

			getStudentDesignation = preparedStatement.executeQuery();
			while (getStudentDesignation.next()) {
				studentDesignation = getStudentDesignation
						.getString("designation");
			}
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return studentDesignation;
	}

	public boolean isElligibleForGraduateResidentHall(int personId)
	{
		System.out.println(" PErson id is : "+personId);
		String studentType = "";
		try
		{
			PreparedStatement ps = null;
			Connection conn = ConnectionUtils.getConnection();
			ResultSet rs = null;
			String query = "select student_type from student where person_id = ?";
			ps = conn.prepareStatement(query);
			ps.setInt(1, personId);
			rs = ps.executeQuery();
			while(rs.next())
			{
				studentType = rs.getString("student_type");
			}
			if(studentType.equalsIgnoreCase(Constants.GRADUATE))
				return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
