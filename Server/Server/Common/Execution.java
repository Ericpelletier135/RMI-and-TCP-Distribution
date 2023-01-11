package Server.Common;

import java.util.*;

public class Execution {

    private static String defaultString = "";
    private static String defaultInt = "-1";
    private static String defaultBool = "false";

    public static String execute(ResourceManager manager, Vector<String> command) {
        String type = "string";

        try {
            switch (command.get(0).toLowerCase()) {
                case "addflight": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int flightNumber = Integer.parseInt(command.get(2));
                    int num = Integer.parseInt(command.get(3));
                    int price = Integer.parseInt(command.get(4));
                    return Boolean.toString(manager.addFlight(xid, flightNumber, num, price));
                }
                case "addcars": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    int num = Integer.parseInt(command.get(3));
                    int price = Integer.parseInt(command.get(4));
                    return Boolean.toString(manager.addCars(xid, location, num, price));
                }
                case "addrooms": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    int num = Integer.parseInt(command.get(3));
                    int price = Integer.parseInt(command.get(4));
                    return Boolean.toString(manager.addRooms(xid, location, num, price));
                }
                case "deleteflight": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Boolean.toString(manager.deleteFlight(xid, flightNum));
                }
                case "deletecars": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Boolean.toString(manager.deleteCars(xid, location));
                }
                case "deleterooms": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Boolean.toString(manager.deleteRooms(xid, location));
                }
                case "queryflight": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Integer.toString(manager.queryFlight(xid, flightNum));
                }
                case "querycars": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(manager.queryCars(xid, location));
                }
                case "queryrooms": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(manager.queryRooms(xid, location));
                }
                case "queryflightprice": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    int flightNum = Integer.parseInt(command.get(2));
                    return Integer.toString(manager.queryFlightPrice(xid, flightNum));
                }
                case "querycarsprice": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(manager.queryCarsPrice(xid, location));
                }
                case "queryroomsprice": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    String location = command.get(2);
                    return Integer.toString(manager.queryRoomsPrice(xid, location));
                }
                case "querycustomerinfo": {
                    type = "string";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    return manager.queryCustomerInfo(xid, customerID);
                }
                case "newcustomer": {
                    type = "int";
                    int xid = Integer.parseInt(command.get(1));
                    return Integer.toString(manager.newCustomer(xid));
                }
                case "newcustomerid": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int id = Integer.parseInt(command.get(2));
                    return Boolean.toString(manager.newCustomer(xid, id));
                }
                case "deletecustomer": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    return Boolean.toString(manager.deleteCustomer(xid, customerID));
                }
                case "reserveflight": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    int flightNum = Integer.parseInt(command.get(3));
                    return Boolean.toString(manager.reserveFlight(xid, customerID, flightNum));
                }
                case "reservecar": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    String location = command.get(3);
                    return Boolean.toString(manager.reserveCar(xid, customerID, location));
                }
                case "reserveroom": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));
                    String location = command.get(3);
                    return Boolean.toString(manager.reserveRoom(xid, customerID, location));
                }
                case "bundle": {
                    type = "bool";
                    int xid = Integer.parseInt(command.get(1));
                    int customerID = Integer.parseInt(command.get(2));

                    Vector<String> flightNumbers = new Vector<String>();
                    for (int i = 0; i < command.size() - 6; ++i) {
                        flightNumbers.add(command.elementAt(3+i));
                    }

                    String location = command.get(command.size()-3);
                    boolean car = toBoolean(command.get(command.size()-2));
                    boolean room = toBoolean(command.get(command.size()-1));

                    return Boolean.toString(manager.bundle(xid, customerID, flightNumbers, location, car, room));
                }
            }
        } catch(Exception e) {
            System.err.println((char)27 + "[31;1mExecution exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
        }

        if (type.equals("string"))
            return defaultString;
        else if (type.equals("int"))
            return defaultInt;
        else
            return defaultBool;

    }

    private static boolean toBoolean(String string)
    {
        return (Boolean.valueOf(string)).booleanValue();
    }
}