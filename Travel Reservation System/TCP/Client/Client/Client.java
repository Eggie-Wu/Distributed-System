package Client;
import java.util.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;

public abstract class Client
{

	public Client()
	{
		super();
	}

	public abstract void connectServer();

	public abstract String sendMessage(String message);

	public void start()
	{
		// Prepare for reading commands
		System.out.println();
		System.out.println("Location \"help\" for list of supported commands");

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			// Read the next command
			String command = "";
			Vector<String> arguments = new Vector<String>();
			try {
				System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
				command = stdin.readLine().trim();
			}
			catch (IOException io) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
				io.printStackTrace();
				System.exit(1);
			}

			try {
				arguments = parse(command);
				Command cmd = Command.fromString((String)arguments.elementAt(0));
				try {
					execute(cmd, arguments);
				}
				catch (ConnectException e) {
					connectServer();
					execute(cmd, arguments);
				}
			}
			catch (IllegalArgumentException|ServerException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			}
			catch (ConnectException|UnmarshalException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mConnection to server lost");
			}
			catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}
	}

	public void execute(Command cmd, Vector<String> arguments) throws RemoteException, NumberFormatException
	{
		switch (cmd)
		{
			case Help:
			{
				if (arguments.size() == 1) {
					System.out.println(Command.description());
				} else if (arguments.size() == 2) {
					Command l_cmd = Command.fromString((String)arguments.elementAt(1));
					System.out.println(l_cmd.toString());
				} else {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
				}
				break;
			}
			case AddFlight: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding a new flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				System.out.println("-Flight Seats: " + arguments.elementAt(3));
				System.out.println("-Flight Price: " + arguments.elementAt(4));

				this.sendMessage(VectorToString(arguments));
				break;
			}
			case AddCars: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new cars [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				System.out.println("-Number of Cars: " + arguments.elementAt(3));
				System.out.println("-Car Price: " + arguments.elementAt(4));

				this.sendMessage(VectorToString(arguments));
				break;
			}
			case AddRooms: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new rooms [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				System.out.println("-Number of Rooms: " + arguments.elementAt(3));
				System.out.println("-Room Price: " + arguments.elementAt(4));
				
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case AddCustomer: {
				checkArgumentsCount(2, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case AddCustomerID: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				this.sendMessage(VectorToString(arguments));
				break;
			}
			case DeleteFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case DeleteCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all cars at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case DeleteRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all rooms at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case DeleteCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a customer from the database [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				this.sendMessage(VectorToString(arguments));
				break;               
			}
			case QueryFlightPrice: {
				checkArgumentsCount(3, arguments.size());
				
				System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case ReserveFlight: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving seat in a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Flight Number: " + arguments.elementAt(3));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case ReserveCar: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a car at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Car Location: " + arguments.elementAt(3));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case ReserveRoom: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a room at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Room Location: " + arguments.elementAt(3));
				this.sendMessage(VectorToString(arguments));
				break;
			}
			case Bundle: {
				if (arguments.size() < 7) {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 7 arguments. Location \"help\" or \"help,<CommandName>\"");
					break;
				}

				System.out.println("Reserving an bundle [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				for (int i = 0; i < arguments.size() - 6; ++i)
				{
					System.out.println("-Flight Number: " + arguments.elementAt(3+i));
				}
				System.out.println("-Location for Car/Room: " + arguments.elementAt(arguments.size()-3));
				System.out.println("-Book Car: " + arguments.elementAt(arguments.size()-2));
				System.out.println("-Book Room: " + arguments.elementAt(arguments.size()-1));

				this.sendMessage(VectorToString(arguments));
				break;
			}
			case generate_summary: {

				checkArgumentsCount(2, arguments.size());
				System.out.println("=== Generating Summary ===");
				this.sendMessage(VectorToString(arguments));
				break;
				// System.out.println(m_resourceManager.generate_summary(Integer.parseInt(arguments.get(1))));
			}
			case generate_analytics: {
				checkArgumentsCount(3, arguments.size());
				System.out.println("=== Generating Analytics with Quantity Lower than " + arguments.get(1)+"===");
				this.sendMessage(VectorToString(arguments));
				break;
				// System.out.println(m_resourceManager.generate_analytics(Integer.parseInt(arguments.get(1)), Integer.parseInt(arguments.get(2))));
			}
			case test:{
				String r;

				//initialize
				this.testerInitialize();

				//flight
				r=this.sendMessage("AddFlight,1,1,10,15");
				printResult(1,r,"true");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(2,r,"10");
				r=this.sendMessage("QueryFlightPrice,1,1");
				printResult(3,r,"15");
				r=this.sendMessage("AddFlight,1,1,20,25");
				printResult(4,r,"true");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(5,r,"30");
				r=this.sendMessage("QueryFlightPrice,1,1");
				printResult(6,r,"25");

				r=this.sendMessage("AddFlight,1,2,10,10");
				printResult(7,r,"true");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(8,r,"10");
				r=this.sendMessage("DeleteFlight,1,2");
				printResult(9,r,"true");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(10,r,"0");

				//car
				r=this.sendMessage("AddCars,1,1,30,35");
				printResult(11,r,"true");
				r=this.sendMessage("QueryCars,1,1");
				printResult(12,r,"30");
				r=this.sendMessage("QueryCarsPrice,1,1");
				printResult(13,r,"35");
				r=this.sendMessage("AddCars,1,1,40,45");
				printResult(14,r,"true");
				r=this.sendMessage("QueryCars,1,1");
				printResult(15,r,"70");
				r=this.sendMessage("QueryCarsPrice,1,1");
				printResult(16,r,"45");

				r=this.sendMessage("AddCars,1,2,10,10");
				printResult(17,r,"true");
				r=this.sendMessage("QueryCars,1,2");
				printResult(18,r,"10");
				r=this.sendMessage("DeleteCars,1,2");
				printResult(19,r,"true");
				r=this.sendMessage("QueryCars,1,2");
				printResult(20,r,"0");

				//room
				r=this.sendMessage("AddRooms,1,1,50,55");
				printResult(21,r,"true");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(22,r,"50");
				r=this.sendMessage("QueryRoomsPrice,1,1");
				printResult(23,r,"55");
				r=this.sendMessage("AddRooms,1,1,60,65");
				printResult(24,r,"true");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(25,r,"110");
				r=this.sendMessage("QueryRoomsPrice,1,1");
				printResult(26,r,"65");

				r=this.sendMessage("AddRooms,1,2,10,10");
				printResult(27,r,"true");
				r=this.sendMessage("QueryRooms,1,2");
				printResult(28,r,"10");
				r=this.sendMessage("DeleteRooms,1,2");
				printResult(29,r,"true");
				r=this.sendMessage("QueryRooms,1,2");
				printResult(30,r,"0");

				//customer
				r=this.sendMessage("AddCustomerID,1,1");
				printResult(31,r,"true");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(32,r,"Bill for customer 1:  With a total price of $0");


				//reserve flight
				r=this.sendMessage("ReserveFlight,1,1,1");
				printResult(33,r,"true");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(34,r,"29");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(35,r,"Bill for customer 1: 1* flight-1 $25,  With a total price of $25");


				//reserve car
				r=this.sendMessage("ReserveCar,1,1,1");
				printResult(36,r,"true");
				r=this.sendMessage("QueryCars,1,1");
				printResult(37,r,"69");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(38,r,"Bill for customer 1: 1* flight-1 $25, 1* car-1 $45,  With a total price of $70");

				//reserve room
				r=this.sendMessage("ReserveRoom,1,1,1");
				printResult(39,r,"true");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(40,r,"109");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(41,r,"Bill for customer 1: 1* flight-1 $25, 1* room-1 $65, 1* car-1 $45,  With a total price of $135");

				r=this.sendMessage("AddFlight,1,2,10,1");
				r=this.sendMessage("AddFlight,1,3,20,2");
				r=this.sendMessage("AddCustomerID,1,2");
				printResult(42,r,"true");
				r=this.sendMessage("Bundle,1,2,1,2,3,1,true,true");
				printResult(43,r,"true");
				r=this.sendMessage("QueryCustomer,1,2");
				printResult(44,r,"Bill for customer 2: 1* flight-3 $2, 1* flight-1 $25, 1* flight-2 $1, 1* room-1 $65, 1* car-1 $45,  With a total price of $138");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(45,r,"9");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(46,r,"19");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(47,r,"28");
				r=this.sendMessage("QueryCars,1,1");
				printResult(48,r,"68");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(49,r,"108");


				r=this.sendMessage("AddCustomerID,1,3");
				printResult(50,r,"true");
				r=this.sendMessage("Bundle,1,3,1,3,1,false,true");
				r=this.sendMessage("QueryCustomer,1,3");
				printResult(51,r,"Bill for customer 3: 1* flight-3 $2, 1* flight-1 $25, 1* room-1 $65,  With a total price of $92");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(52,r,"9");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(53,r,"18");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(54,r,"27");
				r=this.sendMessage("QueryCars,1,1");
				printResult(55,r,"68");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(56,r,"107");


				r=this.sendMessage("AddCustomerID,1,4");
				printResult(57,r,"true");
				r=this.sendMessage("Bundle,1,4,1,2,1,true,false");
				r=this.sendMessage("QueryCustomer,1,4");
				printResult(58,r,"Bill for customer 4: 1* flight-1 $25, 1* flight-2 $1, 1* car-1 $45,  With a total price of $71");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(59,r,"8");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(60,r,"18");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(61,r,"26");
				r=this.sendMessage("QueryCars,1,1");
				printResult(62,r,"67");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(63,r,"107");


				r=this.sendMessage("AddCustomerID,1,5");
				r=this.sendMessage("Bundle,1,5,1,2,3,1,false,false");
				printResult(64,r,"true");
				r=this.sendMessage("QueryCustomer,1,5");
				printResult(65,r,"Bill for customer 5: 1* flight-3 $2, 1* flight-1 $25, 1* flight-2 $1,  With a total price of $28");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(66,r,"7");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(67,r,"17");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(68,r,"25");
				r=this.sendMessage("QueryCars,1,1");
				printResult(69,r,"67");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(70,r,"107");


				r=this.sendMessage("DeleteCustomer,1,2");
				printResult(71,r,"true");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(73,r,"8");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(74,r,"18");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(75,r,"26");
				r=this.sendMessage("QueryCars,1,1");
				printResult(76,r,"68");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(77,r,"108");

				r=this.sendMessage("AddCars,1,2,30,35");
				printResult(78,r,"true");
				r=this.sendMessage("AddRooms,1,2,30,35");
				printResult(79,r,"true");
				r=this.sendMessage("Bundle,1,1,1,true,true");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(80,r,"Bill for customer 1: 1* flight-1 $25, 2* room-1 $65, 2* car-1 $45,  With a total price of $245");

				r=this.sendMessage("Bundle,1,1,1,2,3,2,true,true");
				r=this.sendMessage("QueryCustomer,1,1");
				printResult(81,r,"Bill for customer 1: 1* flight-3 $2, 2* flight-1 $25, 2* room-1 $65, 1* flight-2 $1, 2* car-1 $45, 1* room-2 $35, 1* car-2 $35,  With a total price of $343");

				r=this.sendMessage("DeleteCustomer,1,3");
				printResult(82,r,"true");
				r=this.sendMessage("QueryFlight,1,1");
				printResult(83,r,"26");
				r=this.sendMessage("QueryFlight,1,2");
				printResult(84,r,"7");
				r=this.sendMessage("QueryFlight,1,3");
				printResult(85,r,"18");
				r=this.sendMessage("QueryCars,1,1");
				printResult(86,r,"67");
				r=this.sendMessage("QueryRooms,1,1");
				printResult(87,r,"108");

				r=this.sendMessage("generate_summary,1");
				printResult(88,r,"Customer ID 1:  1 item(s) of flight-3 purchased, with a total price of $2.   2 item(s) of flight-1 purchased, with a total price of $50.   2 item(s) of room-1 purchased, with a total price of $130.   1 item(s) of flight-2 purchased, with a total price of $1.   2 item(s) of car-1 purchased, with a total price of $90.   1 item(s) of room-2 purchased, with a total price of $35.   1 item(s) of car-2 purchased, with a total price of $35.  Customer ID 4:  1 item(s) of flight-1 purchased, with a total price of $25.   1 item(s) of flight-2 purchased, with a total price of $1.   1 item(s) of car-1 purchased, with a total price of $45.  Customer ID 5:  1 item(s) of flight-3 purchased, with a total price of $2.   1 item(s) of flight-1 purchased, with a total price of $25.   1 item(s) of flight-2 purchased, with a total price of $1.  ");
				r=this.sendMessage("generate_analytics,1,30");
				printResult(89,r,"The reservable item flight-3 is on a lower quantity state, with 18 remaining items left. The reservable item flight-1 is on a lower quantity state, with 26 remaining items left. The reservable item flight-2 is on a lower quantity state, with 7 remaining items left. The reservable item car-2 is on a lower quantity state, with 29 remaining items left. The reservable item room-2 is on a lower quantity state, with 29 remaining items left. ");

				r=this.sendMessage("DeleteFlight,1,1");
				printResult(90,r,"false");

				r=this.sendMessage("DeleteCustomer,1,4");
				printResult(91,r,"true");
				r=this.sendMessage("DeleteCustomer,1,5");
				printResult(92,r,"true");

				r=this.sendMessage("DeleteCustomer,1,5,1");
				printResult(93,r,"false");

				this.testerInitialize();

				break;
			}
			case Quit:
				checkArgumentsCount(1, arguments.size());
				this.sendMessage("quit");
				System.out.println("Quitting client");
				System.exit(0);
		}
	}

	private void testerInitialize(){
		this.sendMessage("DeleteCustomer,1,1");
		this.sendMessage("DeleteCustomer,1,2");
		this.sendMessage("DeleteCustomer,1,3");
		this.sendMessage("DeleteCustomer,1,4");
		this.sendMessage("DeleteCustomer,1,5");
		this.sendMessage("DeleteCustomer,1,6");

		this.sendMessage("DeleteFlight,1,1");
		this.sendMessage("DeleteFlight,1,2");
		this.sendMessage("DeleteFlight,1,3");

		this.sendMessage("DeleteCars,1,1");
		this.sendMessage("DeleteCars,1,2");
		this.sendMessage("DeleteCars,1,3");

		this.sendMessage("DeleteRooms,1,1");
		this.sendMessage("DeleteRooms,1,2");
		this.sendMessage("DeleteRooms,1,3");
	}


	private static void printResult(int i,String r1,String r2){
		if(r1.equals(r2)){
			System.out.println("Test case "+Integer.toString(i)+" passed.");
		}else{
			System.out.println("Test case "+Integer.toString(i)+" failed.");
			System.out.println("Expected: "+r2);
			System.out.println("Received: "+r1);
		}
		try{
			Thread.sleep(50);
		}catch(Exception ignored){}
	}

	public static Vector<String> parse(String command)
	{
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

	public static String VectorToString(Vector<String> vector){
		String str = vector.toString();
		if(str.length()<2) return str;
		return str.substring(1, str.length()-1);
	}

	public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException
	{
		if (expected != actual)
		{
			throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
		}
	}

	public static int toInt(String string) throws NumberFormatException
	{
		return (Integer.valueOf(string)).intValue();
	}

	public static boolean toBoolean(String string)// throws Exception
	{
		return (Boolean.valueOf(string)).booleanValue();
	}
}
