package com.univhousing.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.univhousing.main.ConnectionUtils;

public class Guest {
	
	public int approvalId;
	public int personId;
	ResultSet getPersonId = null;
	PreparedStatement preparedStatement = null;
	Connection dbConnection = null;
	
	
	
	public boolean checkPersonIsGuest(int personId){
		
		try 
		{
			dbConnection = ConnectionUtils.getConnection();
			String query = "SELECT person_id FROM Guest WHERE person_id = ?";
			preparedStatement = dbConnection.prepareStatement(query);
			preparedStatement.setInt(1, personId);
			
			getPersonId = preparedStatement.executeQuery();
			
			if(getPersonId.isBeforeFirst())
			{
				return true;
			}
			else
			{
				return false;
			}
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			try {
				dbConnection.close();
				getPersonId.close();
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * @param approvalId
	 * @return PersonId for a given Approval Id
	 */
	public int getPersonIdFromApprovalId(int approvalId) {
		/*Write SQL Query to get PersonId for the given Approval Id*/
		int personId = 0;
		
		try 
		{
			dbConnection = ConnectionUtils.getConnection();
			String query = "SELECT person_id FROM Guest WHERE approval_id = ?";
			preparedStatement = dbConnection.prepareStatement(query);
			preparedStatement.setInt(1, approvalId);
			
			getPersonId = preparedStatement.executeQuery();
			
			while(getPersonId.next())
			{
				personId = getPersonId.getInt("person_id");
			}
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				dbConnection.close();
				getPersonId.close();
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return personId;
	}


}
