package Server.TCP;

import Server.Common.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class TCPRequestHandler extends Thread {
    private volatile static int count = 0;
    private int id;
    private Socket socket;
    private ResourceManager manager;
    public TCPRequestHandler(Socket socket, ResourceManager manager){
        this.socket = socket;
        this.manager = manager;
        this.id = count;
        count++;
    }

    @Override
    public void run() {
        System.out.println("New Thread created");
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;
        try{
            inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new PrintWriter(socket.getOutputStream(), true);
            while(true){
                String result = "";    
                String message = inFromClient.readLine();
                if(message.equals("quit")) break;
                Vector<String> arguments = parse(message);
                if(arguments!=null) result = this.execute(arguments);
                outToClient.println(result);
                outToClient.println();
            }
            System.out.println("Socket Close");
            inFromClient.close();
            outToClient.close();
            socket.close();

        }catch(Exception e){
            try{
                inFromClient.close();
                outToClient.close();
                socket.close();
            }catch(Exception ignored){}
        }
    } 

    public static Vector<String> parse(String command){
		Vector<String> arguments = new Vector<String>();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument = "";
		while (tokenizer.hasMoreTokens())
		{
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}

    public static int toInt(String string) throws NumberFormatException
	{
		return (Integer.valueOf(string)).intValue();
	}

	public static boolean toBoolean(String string)// throws Exception
	{
		return (Boolean.valueOf(string)).booleanValue();
	}
    
    synchronized public String execute(Vector<String> arguments){
        System.out.println(id+" "+ arguments.toString());
        try {
            switch (arguments.get(0).toLowerCase()) {
                //copied from Client/Client.java from the provided template
                case "addflight": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int flightNumber = Integer.parseInt(arguments.get(2));
                    int num = Integer.parseInt(arguments.get(3));
                    int price = Integer.parseInt(arguments.get(4));
                    return Boolean.toString(manager.addFlight(xid, flightNumber, num, price));
                }
                case "addcars": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    int num = Integer.parseInt(arguments.get(3));
                    int price = Integer.parseInt(arguments.get(4));
                    return Boolean.toString(manager.addCars(xid, location, num, price));
                }
                case "addrooms": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    int num = Integer.parseInt(arguments.get(3));
                    int price = Integer.parseInt(arguments.get(4));
                    return Boolean.toString(manager.addRooms(xid, location, num, price));
                }
                case "addcustomer": {
                    int xid = Integer.parseInt(arguments.get(1));
                    return Integer.toString(manager.newCustomer(xid));
                }
                case "addcustomerid": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int id = Integer.parseInt(arguments.get(2));
                    return Boolean.toString(manager.newCustomer(xid, id));
                }
                case "deleteflight": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int flightNum = Integer.parseInt(arguments.get(2));
                    return Boolean.toString(manager.deleteFlight(xid, flightNum));
                }
                case "deletecars": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Boolean.toString(manager.deleteCars(xid, location));
                }
                case "deleterooms": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Boolean.toString(manager.deleteRooms(xid, location));
                }
                case "deletecustomer": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    return Boolean.toString(manager.deleteCustomer(xid, customerID));
                }
                case "queryflight": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int flightNum = Integer.parseInt(arguments.get(2));
                    return Integer.toString(manager.queryFlight(xid, flightNum));
                }
                case "querycars": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Integer.toString(manager.queryCars(xid, location));
                }
                case "queryrooms": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Integer.toString(manager.queryRooms(xid, location));
                }
                case "querycustomer": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    return manager.queryCustomerInfo(xid, customerID);
                }
                case "queryflightprice": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int flightNum = Integer.parseInt(arguments.get(2));
                    return Integer.toString(manager.queryFlightPrice(xid, flightNum));
                }
                case "querycarsprice": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Integer.toString(manager.queryCarsPrice(xid, location));
                }
                case "queryroomsprice": {
                    int xid = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Integer.toString(manager.queryRoomsPrice(xid, location));
                }
                case "reserveflight": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    int flightNum = Integer.parseInt(arguments.get(3));
                    return Boolean.toString(manager.reserveFlight(xid, customerID, flightNum));
                }
                case "reservecar": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    String location = arguments.get(3);
                    return Boolean.toString(manager.reserveCar(xid, customerID, location));
                }
                case "reserveroom": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    String location = arguments.get(3);
                    return Boolean.toString(manager.reserveRoom(xid, customerID, location));
                }
                case "bundle": {
                    int xid = Integer.parseInt(arguments.get(1));
                    int customerID = Integer.parseInt(arguments.get(2));
                    Vector<String> flightNumbers = new Vector<String>();
                    for (int i = 0; i < arguments.size() - 6; ++i) {
                        flightNumbers.add(arguments.elementAt(3 + i));
                    }
                    // Location
                    String location = arguments.get(arguments.size() - 3);
                    boolean car = toBoolean(arguments.get(arguments.size() - 2));
                    boolean room = toBoolean(arguments.get(arguments.size() - 1));
                    return Boolean.toString(manager.bundle(xid, customerID, flightNumbers, location, car, room));
                }
                case "removereservation": {
                    String reserveditemKey = arguments.get(1);
                    int id=Integer.parseInt(arguments.get(2));
                    int customerID = Integer.parseInt(arguments.get(3));
                    int reserveditemCount = Integer.parseInt(arguments.get(4));
                    return Boolean.toString(manager.removeReservation(reserveditemKey, id, customerID, reserveditemCount));
                }
                case "reserve_flight_server":{
                    int id = Integer.parseInt(arguments.get(1));
                    int flightnumber = Integer.parseInt(arguments.get(2));
                    return Boolean.toString(manager.reserve_flight_server(id, flightnumber));

                }
                case "reserve_car_server":{
                    int id = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Boolean.toString(manager.reserve_car_server(id, location));

                }
                case "reserve_room_server":{
                    int id = Integer.parseInt(arguments.get(1));
                    String location = arguments.get(2);
                    return Boolean.toString(manager.reserve_room_server(id, location));

                }
                case "generate_analytics": {
                    int id = Integer.parseInt(arguments.get(1));
                    int low_quantity = Integer.parseInt(arguments.get(2));
                    return manager.generate_analytics(id, low_quantity);
                    
                }
                case "generate_summary": {
                    int id = Integer.parseInt(arguments.get(1));
                    return manager.generate_summary(id);
                }
            }
        }catch (Exception e) {
            System.err.println("Not supported(arguments: "+(arguments.toString()));
        }
        return "IllegalArgument";
    }     
}
