package com.univhousing.invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import com.univhousing.main.ConnectionUtils;
import com.univhousing.main.Constants;
import com.univhousing.main.Utils;
import com.univhousing.users.Student;

public class TicketPersonStaff_Relation {
	
	public int staffNo;
	public int ticketNo;
	public String ticketStatus;
	public String ticketSeverity;
	public int personId;

	Scanner inputObj = new Scanner(System.in);
	Student studentObj = new Student();

	/**
	 * @param personId
	 * @action Raises a new ticket based on the information given by the user
	 * @dataSeverity Water, Electricity � High severity
					 Appliances, Internet � Medium severity
					 Cleaning, Miscellaneous � Low severity
	 */
	public void raiseNewTicket(int personId) {

		String severity = "", description = "";
		String ticketType = "";
		int ticketNo = 0, staffNoMax = 0, staffNoMin = 0;
		System.out.println("Select the ticket type:\n" +
				"1. Water\n" +
				"2. Electricity\n" +
				"3. Applicance\n" +
				"4. Internet\n" +
				"5. Cleaning\n" +
				"6. Miscellaneous\n");

		int type = inputObj.nextInt();

		switch (type) 
		{
			case 1:
				ticketType = Constants.WATER;
				severity = Constants.HIGH_SEVERITY;
				break;
			case 2:
				ticketType = Constants.ELECTRICITY;
				severity = Constants.HIGH_SEVERITY;
				break;
				
			case 3:
				ticketType = Constants.APPLIANCE;
				severity = Constants.MEDIUM_SEVERITY;
				break;
			case 4:
				ticketType = Constants.INTERNET;
				severity = Constants.MEDIUM_SEVERITY;
				break;
				
			case 5:
				ticketType = Constants.CLEANING;
				severity = Constants.LOW_SEVERITY;
				break;
			case 6:
				ticketType = Constants.MISCELLANEOUS;
				severity = Constants.LOW_SEVERITY;
				break;
		}
		
		// Consuming the  /n after previous input as nextInt(), doesn't consume the \n i.e. enter
		inputObj.nextLine();
		
		System.out.println("Enter the description for the issue:");
		description = inputObj.nextLine();
		
		/*Write SQL Query to fetch the maximum and minimum value of the staff no to randomize who gets the ticket*/
		PreparedStatement ps = null;
		Connection conn = ConnectionUtils.getConnection();
		String query3 = "SELECT MAX(staff_no) AS LastStaff FROM Housing_Staff";
		try 
		{
			ps = conn.prepareStatement(query3);
			ResultSet max = ps.executeQuery();
			while(max.next())
			{
				staffNoMax = max.getInt("LastStaff");
			}
		}
		catch(SQLException e)
		{
			try {
				ConnectionUtils.closeConnection(conn);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		PreparedStatement ps1 = null;
		Connection conn1 = ConnectionUtils.getConnection();
		String query4 = "SELECT MIN(staff_no) AS FirstStaff FROM Housing_Staff";
		try 
		{
			ps1 = conn1.prepareStatement(query4);
			ResultSet min = ps1.executeQuery();
			while(min.next())
			{
				staffNoMin = min.getInt("FirstStaff");
			}
		}
		catch(SQLException e)
		{
			try {
				ConnectionUtils.closeConnection(conn1);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		// Getting a random staff number between First and the Last staff
		staffNo = Utils.randomizeStaff(staffNoMax, staffNoMin);
		System.out.println("Assigning ticket to staff: "+staffNo);
		
		/*Write SQL Query to fetch the maximum value of the ticket number so that we can continue from there
		 * If the value is null then we will just assign value as default ticket number value*/
		
		PreparedStatement preparedStatement = null;
		Connection dbConnection = ConnectionUtils.getConnection();
		String query = "SELECT MAX(ticket_no) AS HighestTicket FROM ticket_person_staff";
		
		try 
		{
			preparedStatement = dbConnection.prepareStatement(query);
			ResultSet maxTicketNo = preparedStatement.executeQuery();
			
			while(maxTicketNo.next())
			{
				ticketNo = maxTicketNo.getInt("HighestTicket");
				if(ticketNo == 0)
				{
					ticketNo = Constants.TICKET_DEFAULT_VALUE;
				}
				else
				{
					ticketNo++;
				}
				
			}

			ConnectionUtils.closeConnection(dbConnection);
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*Write SQL query to create a ticket with type, ticketNo, severity and description
		 * Ticket has to be raised with a staff no who it will be assigned to, so 
		 * we can do random generation of staff no. or we can write logic to choose staff no.*/
		
		System.out.println("Please Note Ticket No:"+ticketNo);
		PreparedStatement preparedStatement1 = null;
		Connection dbConnection1 = ConnectionUtils.getConnection();
		String query1 = "INSERT INTO ticket_person_staff (staff_no,ticket_no,ticket_status,ticket_severity,person_id,description,ticket_type) VALUES (?,?,?,?,?,?,?)";
		
		try 
		{
			preparedStatement1 = dbConnection1.prepareStatement(query1);
			preparedStatement1.setInt(1, staffNo);
			preparedStatement1.setInt(2, ticketNo);
			preparedStatement1.setString(3, Constants.PENDING_STATUS);
			preparedStatement1.setString(4, severity);
			preparedStatement1.setInt(5, personId);
			preparedStatement1.setString(6, description);
			preparedStatement1.setString(7, ticketType);
			
			preparedStatement1.executeUpdate();
			ConnectionUtils.closeConnection(dbConnection1);
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param personId
	 * @throws SQLException 
	 * @action Lists all the tickets for a particular student id for a person Id
	 * and saves them in the ArrayList<Integer> ticketNumbers
	 */
	public void viewAllTicketStatuses(int personId, ArrayList<Integer> ticketNumbersList) throws SQLException 
	{
		int studentId;
		// Fetch the student Id for a particular Person Id
		studentId = studentObj.getStudentIdForPersonId(personId);
		
		/*Write SQL Query to fetch all the tickets*/
		ResultSet viewAllTickets = null;
		
		ticketNumbersList.clear();
		
		//-------------------------------------------------------------------------------------
		
		ResultSet rs = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null ;
		
		try{
			dbConnection = ConnectionUtils.getConnection();
			
			String selectQuery = "SELECT ticket_no FROM ticket_person_staff WHERE person_id = ? " ;
			
//			System.out.println("SELECT QUERY IS: "+selectQuery);
					
			preparedStatement = dbConnection.prepareStatement(selectQuery);
			
			preparedStatement.setInt(1,personId);
			
			rs = preparedStatement.executeQuery();

			//If record exists , rs.next() will evaluate to true
			if(!rs.isBeforeFirst())
			{
				System.out.println("No tickets found!");
				rs.close();
		        preparedStatement.close();
		        dbConnection.close();
		        return;
			}
			else
				{
					while(rs.next())
					{
//						System.out.print(rs.getInt("ticket_no")+"\t\t");
						ticketNumbersList.add(rs.getInt("ticket_no"));
						
					}
				}
			
		}catch(SQLException e1){
			System.out.println("SQLException: "+ e1.getMessage());
			System.out.println("VendorError: "+ e1.getErrorCode());
		}
		catch(Exception e3)
		{
			System.out.println("General Exception Case. Printing stack trace below:\n");
			e3.printStackTrace();
		}
		finally{
				try {
			        rs.close();
			        preparedStatement.close();
			        dbConnection.close();
			      } catch (SQLException e) {
			        e.printStackTrace();
			      }
		}
		
		
		//-------------------------------------------------------------------------------------
		
//		ticketNumbersList.clear();
//		while(rs.next())
//		{
//			ticketNumbersList.add(rs.getInt("ticket_no"));
//		}
		
		//Close the result set if you are not going to use later
		
		System.out.println("\nBelow are the list of tickets:");
		for (int i = 0; i < ticketNumbersList.size(); i++) 
		{
			System.out.println((i+1)+". "+ticketNumbersList.get(i));
		}
		
		
		System.out.println("\nSelect the ticket number you want to view details for:");
		int choice = inputObj.nextInt();
		
		while(choice>ticketNumbersList.size())
		{
			System.out.println("\nPlease select the correct ticket number you want to view details for:");
			choice = inputObj.nextInt();
		}
		
		/* Getting the ticket number form the index, but have to substract 1 because ArrayList has values from 0 to n-1
		 * but the options on screen are from 1 to n-1
		*/
		int ticketSelected = ticketNumbersList.get(choice-1);
		
		ResultSet getTicketDetails = null;
		/*Write SQL query for fetching the details with status for the ticket number (ticketSelected) by student*/
		
		//------------------------------------------------------------------------------------------------------
		
		
		try{
			dbConnection = ConnectionUtils.getConnection();
			
			String selectQuery = "SELECT * FROM ticket_person_staff WHERE ticket_no = ? " ;
			
			System.out.println("SELECT QUERY IS: "+selectQuery);
					
			preparedStatement = dbConnection.prepareStatement(selectQuery);
			
			preparedStatement.setInt(1,ticketSelected);
			
			System.out.println("ticketNo: "+choice);
			
			rs = preparedStatement.executeQuery();

			//If record exists , rs.next() will evaluate to true
			if(rs.isBeforeFirst())
				{
					rs.next();
					System.out.println("=============================================================================================================");
					System.out.print("staff_no\tticket_no\tticket_status\tticket_severity\tperson_id");
					System.out.println("\tdescription\tticket_type\t") ;
					System.out.println("=============================================================================================================");
					
					System.out.print(rs.getInt("staff_no")+"\t\t");
					System.out.print(rs.getInt("ticket_no")+"\t\t");
					System.out.print(rs.getString("ticket_status")+"\t\t");
					System.out.print(rs.getString("ticket_severity")+"\t\t");
					System.out.print(rs.getInt("person_id")+"\t\t");
					System.out.print(rs.getString("description")+"\t\t");
					System.out.print(rs.getString("ticket_type")+"\t\t");
				}
			
			System.out.println("\n=============================================================================================================\n\n");
			
		}catch(SQLException e1){
			System.out.println("SQLException: "+ e1.getMessage());
			System.out.println("VendorError: "+ e1.getErrorCode());
		}
		catch(Exception e3)
		{
			System.out.println("General Exception Case. Printing stack trace below:\n");
			e3.printStackTrace();
		}
		finally{
				try {
			        rs.close();
			        preparedStatement.close();
			        dbConnection.close();
			      } catch (SQLException e) {
			        e.printStackTrace();
			      }
		}
		
		//------------------------------------------------------------------------------------------------------
		
	}

	/**
	 * @param ArrayList<Integer> adminLevelMaintenanceRequests
	 * @throws SQLException 
	 * @action Shows all the requests that have to be handled by the Supervisor
	 */
	public void getAllMaintenanceTickets(ArrayList<Integer> maintenanceTicketsList) throws SQLException 
	{
		int ticketNumber = 0;
		/*Write SQL Query to fetch all the maintenance tickets with status Pending*/
		ResultSet allTickets = null;
		PreparedStatement ps = null;
		Connection conn = ConnectionUtils.getConnection();
		String query = "SELECT ticket_no FROM ticket_person_staff WHERE ticket_status = ? ORDER BY decode(ticket_severity,'High',1 ,'Medium',2,'Low',3) ASC, ticket_no DESC";
		maintenanceTicketsList.clear();
		
		try
		{
			ps = conn.prepareStatement(query);
			ps.setString(1, Constants.PENDING_STATUS);
			System.out.println(ps);
			allTickets = ps.executeQuery();
			
			while(allTickets.next())
			{
				maintenanceTicketsList.add(allTickets.getInt("ticket_no"));
			}
			System.out.println("Total tickets retrieved : "+maintenanceTicketsList.size());
			
			if(maintenanceTicketsList.size()>0)
			{
				System.out.println("Displaying all Maintenance Tickets");
				for (int i = 0; i < maintenanceTicketsList.size(); i++) 
				{
					System.out.println((i+1)+". "+maintenanceTicketsList.get(i));
				}
				System.out.println("Select the ticket you want to: ");
				int ticketSelected = inputObj.nextInt();
				ticketNumber = maintenanceTicketsList.get(ticketSelected-1);
			}
			else
			{
				System.out.println("No tickets to display");
				return;
			}
			
			/*Write SQL Query to fetch the details for one particular ticket and set it's ticket_Status to Processing*/
			
		}		
		catch(SQLException e)
		{
			System.out.println(e.toString());
		}
		
		/*Write SQL Query to fetch all details for this ticket number 
		 * and set it's status to Processing*/
		ResultSet getTicketInfo = null;
		PreparedStatement ps1 = null;
		Connection conn1 = ConnectionUtils.getConnection();
		String query1 = "SELECT * FROM ticket_person_staff WHERE ticket_no = ?";
		
		try
		{
			ps1 = conn1.prepareStatement(query1);
			ps1.setInt(1, ticketNumber);
			getTicketInfo = ps1.executeQuery();
			
			while(getTicketInfo.next())
			{
				int staffNo = getTicketInfo.getInt("staff_no");
				int ticket_no = getTicketInfo.getInt("ticket_no");
				String ticket_status = getTicketInfo.getString("ticket_status");
				String ticket_severity = getTicketInfo.getString("ticket_severity");
				int person_id = getTicketInfo.getInt("person_id");
				String description = getTicketInfo.getString("description");
				String ticket_type = getTicketInfo.getString("ticket_type");

				System.out.println("Staff Handling : "+staffNo);
				System.out.println("Ticket No      : "+ticket_no);
				System.out.println("Ticket Status  : "+ticket_status+"\n");
				System.out.println("Ticket Severity: "+ticket_severity);
				System.out.println("Person ID      : "+person_id);
				System.out.println("Ticket Type    : "+ticket_type);
				System.out.println("Description    : "+description+"\n");
				break;
			}
			ConnectionUtils.closeConnection(conn1);
			
			/*Write SQL Query to set the status of the existing tickets apart from the current ticket from Processing to Complete */
			
			String query4 = "UPDATE ticket_person_staff SET ticket_status = ? WHERE ticket_no != ? and ticket_status = ?";
			PreparedStatement ps4 = null;
			Connection conn4 = ConnectionUtils.getConnection();
			
			System.out.println("Updating all tickets which have status from Processing to Complete apart from current ticket no");
			ps4 = conn4.prepareStatement(query4);
			ps4.setString(1, Constants.COMPLETE_STATUS);
			ps4.setInt(2, ticketNumber);
			ps4.setString(3, Constants.PROCESSING_STATUS);
			ps4.executeUpdate();
			
			ConnectionUtils.closeConnection(conn4);
			
			/*Write SQL Query to set the status of the ticket as Processing*/
			String query3 = "UPDATE ticket_person_staff SET ticket_status = ? WHERE ticket_no = ?";
			PreparedStatement ps3 = null;
			Connection conn3 = ConnectionUtils.getConnection();
			
			System.out.println("Updating ticket "+ticketNumber+ " ticket_status to Processing");
			ps3 = conn3.prepareStatement(query3);
			ps3.setString(1, Constants.PROCESSING_STATUS);
			ps3.setInt(2, ticketNumber);
			ps3.executeUpdate();
			
			ConnectionUtils.closeConnection(conn3);
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		/*Write SQL Trigger to change ticket status to Complete after 30 mins*/
		
	}	
	
	
	
	
}
