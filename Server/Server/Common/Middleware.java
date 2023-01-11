package Server.Common;

import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class Middleware extends ResourceManager
{
	protected static String m_name = "Middleware";
	protected MiddlewareClient FlightCl;
	protected MiddlewareClient CarCl;
	protected MiddlewareClient RoomsCl;
	protected MiddlewareClient CustomerCl;
	protected RMHashMap m_data = new RMHashMap();

	public Middleware(String flightIP, int flightPort, String carIP, int carPort, String roomIP, int roomPort, String customerIP, int customerPort)
	{
		super(m_name);


		FlightCl = new MiddlewareClient(flightIP, flightPort);
		CarCl = new MiddlewareClient(carIP, carPort);
		RoomsCl = new MiddlewareClient(roomIP, roomPort);
		CustomerCl = new MiddlewareClient(customerIP, customerPort);
	}

	public void close() {
		FlightCl.stopClient();
		CarCl.stopClient();
		RoomsCl.stopClient();
		CustomerCl.stopClient();
	}

	// Reads a data item
	protected RMItem readData(int xid, String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	protected void writeData(int xid, String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key)
	{
		Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		// Check if there is such an item in the storage
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(xid, curObj.getKey());
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(int xid, String key)
	{
		Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0;  
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
		return value;
	}    

	// Query the price of an item
	protected int queryPrice(int xid, String key)
	{
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0; 
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
		return value;        
	}

	// Reserve an item
	protected boolean reserveItem(int xid, int customerID, String key, String location)
	{
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );        
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		} 

		// Check if the item is available
		ReservableItem item = (ReservableItem)readData(xid, key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{            
			customer.reserve(key, location, item.getPrice());        
			writeData(xid, customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(xid, item.getKey(), item);

			Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}        
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
	{
		String command = String.format("AddFlight,%d,%d,%d,%d", xid, flightNum, flightSeats, flightPrice);
		return toBoolean(send(FlightCl, 'B', command, true));
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price)
	{
		String command = String.format("AddCars,%d,%s,%d,%d", xid, location, count, price);
		return toBoolean(send(CarCl, 'B', command, true));
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price)
	{
		String command = String.format("AddRooms,%d,%s,%d,%d", xid, location, count, price);
		return toBoolean(send(RoomsCl, 'B', command, true));
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum)
	{
		String command = String.format("DeleteFlight,%d,%d", xid, flightNum);
		return toBoolean(send(FlightCl, 'B', command, true));
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location)
	{
		String command = String.format("DeleteCars,%d,%d", xid, location);
		return toBoolean(send(CarCl, 'B', command, true));
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location)
	{
		String command = String.format("DeleteRooms,%d,%d", xid, location);
		return toBoolean(send(RoomsCl, 'B', command, true));
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum)
	{
		String command = String.format("QueryFlight,%d,%d", xid, flightNum);
		return toInt(send(FlightCl, 'I', command, false));
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location)
	{
		String command = String.format("QueryCars,%d,%s", xid, location);
		return toInt(send(CarCl, 'I', command, false));
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location)
	{
		String command = String.format("QueryRooms,%d,%s", xid, location);
		return toInt(send(RoomsCl, 'I', command, false));
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum)
	{
		String command = String.format("QueryFlightPrice,%d,%d", xid, flightNum);
		return toInt(send(RoomsCl, 'I', command, false));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location)
	{
		String command = String.format("QueryCarsPrice,%d,%s", xid, location);
		return toInt(send(RoomsCl, 'I', command, false));
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location)
	{
		String command = String.format("QueryRoomsPrice,%d,%s", xid, location);
		return toInt(send(RoomsCl, 'I', command, false));
	}

	public String queryCustomerInfo(int xid, int customerID)
	{
		String command = String.format("QueryCustomerInfo,%d,%d", xid, customerID);
		return send(CustomerCl, 'S', command, false);
	}

	public int newCustomer(int xid)
	{
		String command = String.format("NewCustomer,%d", xid);
		return toInt(send(CustomerCl, 'I', command, false));
	}

	public boolean newCustomer(int xid, int customerID)
	{
		String command = String.format("NewCustomerID,%d,%d", xid, customerID);
		return toBoolean(send(CustomerCl, 'B', command, false));
	}

	public boolean deleteCustomer(int xid, int customerID)
	{
		String command = String.format("DeleteCustomer,%d,%d", xid, customerID);
		return toBoolean(send(CustomerCl, 'B', command, false));
	}

	// Adds flight reservation to this customer
	public boolean reserveFlight(int xid, int customerID, int flightNum)
	{
		String key = Flight.getKey(flightNum);
		String location = String.valueOf(flightNum);

		// Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
		// Customer customer = CustomerCl.checkCustomer(xid, customerID);
		// if (customer == null) {
		// 	Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
		// 	return false;
		// }
		// ReservableItem item = FlightCl.checkItem(xid, customerID, key, location);
		// if (item == null) return false;

		// CustomerCl.customerReserve(xid, customer, key, location, item);
		// FlightCl.itemReserve(xid, item);

		Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
		return true;
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location)
	{
		String key = Car.getKey(location);

		// Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
		// Customer customer = CustomerCl.checkCustomer(xid, customerID);
		// if (customer == null) {
		// 	Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
		// 	return false;
		// }

		// ReservableItem item = CarCl.checkItem(xid, customerID, key, location);
		// if (item == null) return false;

		// CustomerCl.customerReserve(xid, customer, key, location, item);
		// CarCl.itemReserve(xid, item);

		Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
		return true;
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location)
	{
		String key = Room.getKey(location);

		// Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
		// Customer customer = CustomerCl.checkCustomer(xid, customerID);
		// if (customer == null) {
		// 	Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
		// 	return false;
		// }

		// ReservableItem item = RoomsCl.checkItem(xid, customerID, key, location);
		// if (item == null) return false;

		// CustomerCl.customerReserve(xid, customer, key, location, item);
		// RoomsCl.itemReserve(xid, item);

		Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
		return true;
	}

	// Reserve bundle 
	public boolean bundle(int xid, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room)
	{
		String flightNumString = "";
		for (int i = 0; i < flightNumbers.size(); i++) {
			flightNumString += flightNumbers.get(i);
			if (i != flightNumbers.size() - 1) {
				flightNumString += ", ";
			}
		}
		int carVal = 0;
		int roomVal = 0;
		if(car) carVal = 1;
		if(room) roomVal = 1;
		Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ") called");

		// // Check if customer exist
		// Customer customer = CustomerCl.checkCustomer(xid, customerID);
		// if (customer == null) {
		// 	Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--customer doesn't exist");
		// 	return false;
		// }

		// // Check if flights exist
		// for (int i = 0; i < flightNumbers.size(); i++) {
		// 	String key = Flight.getKey(Integer.parseInt(flightNumbers.get(i)));
		// 	ReservableItem item = FlightCl.checkItem(xid, customerID, key, location);
		// 	if (item == null) {
		// 		Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--invalid flight number: " + flightNumbers.get(i));
		// 		return false;
		// 	}
		// }

		// // Check if car exist
		// if(car) {
		// 	String key = Car.getKey(location);
		// 	ReservableItem item = CarCl.checkItem(xid, customerID, key, location);
		// 	if (item == null) {
		// 		Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--no cars at the given location");
		// 		return false;
		// 	}
		// }

		// // Check if room exist
		// if(room) {
		// 	String key = Room.getKey(location);
		// 	ReservableItem item = RoomsCl.checkItem(xid, customerID, key, location);
		// 	if (item == null) {
		// 		Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--no rooms at the given location");
		// 		return false;
		// 	}
		// }

		// Reserve the Flights
		for (int i = 0; i < flightNumbers.size(); i++) {
			boolean flightReserved = reserveFlight(xid, customerID, Integer.parseInt(flightNumbers.get(i)));
			if (!flightReserved) {
				Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--invalid flight number: " + flightNumbers.get(i));
				return false;
			}
		}

		// Reserve Car if selected
		if(car) {
			boolean carReserved = reserveCar(xid, customerID, location);
			if (!carReserved) {
				Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--no cars at the given location");
				return false;
			}
		}

		// Reserve Room if selected
		if(room) {
			boolean roomReserved = reserveRoom(xid, customerID, location);
			if (!roomReserved) {
				Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  failed--no rooms at the given location");
				return false;
			}
		}

		Trace.info("RM::bundle(" + xid + ", " + customerID + ", " + flightNumString + ", " + location + ", " + carVal + ", " + roomVal + ")  succeeded");
		return true;
	}

	public String getName()
	{
		return m_name;
	}

	private String send(MiddlewareClient conn, char returnType, String command, boolean sync) {
		String res;
		try {
			if (sync) {
				synchronized (conn) {
					try {
						res = conn.sendMessage(command);
						if (res.equals(""))
							throw new IOException();
						return res;
					} catch (IOException e) {
						conn.connect();
						return conn.sendMessage(command);
					}
				}
			}
			else {
				try {
					res = conn.sendMessage(command);
					if (res.equals(""))
						throw new IOException();
					return res;
				} catch (IOException e) {
					conn.connect();
					return conn.sendMessage(command);
				}
			}

		} catch(Exception e) {
			Trace.error(e.toString());
			if (returnType == 'B')
				return "false";
			else if (returnType == 'I')
				return "-1";
			else
				return "";
		}
	}

	private boolean toBoolean(String s) {
		try {
			return Boolean.parseBoolean(s);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return false;
		}
	}

	private int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return -1;
		}
	}
}
 
