package com.univhousing.accomodation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;

public class ResidenceHall {
	
	public String hallName;
	public int phoneNumber;
	public String availableFor;
	public int hallNo;
	public String streetName;
	public String city;
	public int postCode;

	public boolean isGraduateHall(String hallName)
	{
		String hallType = "";
		try
		{
			PreparedStatement ps = null;
			Connection conn = ConnectionUtils.getConnection();
			ResultSet rs = null;
			String query = "select available_for from residence_hall where hall_name = ?";
			ps = conn.prepareStatement(query);
			ps.setString(1, hallName);
			rs = ps.executeQuery();
			while(rs.next())
			{
				hallType = rs.getString("available_for");
				if(hallType!= null && hallType.equalsIgnoreCase(Constants.GRADUATE_STUDENTS))
					return true;
			}
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
