package Server.Middleware;
import java.rmi.RemoteException;
import Server.Common.ResourceManager;
import Server.Common.Trace;
import Server.Common.Customer;
import Server.Common.*;
import java.util.*;

public class Middleware extends ResourceManager {
    final protected RemoteServer flight_server;
    final protected RemoteServer car_server;
    final protected RemoteServer room_server;
    protected String name;

    public Middleware(String name, RemoteServer flight, RemoteServer car, RemoteServer room){
        super(name);
        this.flight_server = flight;
        this.car_server = car;
        this.room_server = room;
    }

    public void connectServers(){
        if(flight_server!=null) this.flight_server.connectServer();
        if(car_server!=null) this.car_server.connectServer();
        if(room_server!=null) this.room_server.connectServer();
    }

    

    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException{
        Trace.info("Function addCars triggered at the middleware");
        synchronized(flight_server){
            while(true){
                try{
                    flight_server.instance.addFlight(id, flightNum, flightSeats, flightPrice);
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteFlight(int id, int flightNum) throws RemoteException{
        Trace.info("Function deleteFlight triggered at the middleware");
        synchronized(flight_server){
            while(true){
                try{
                    return flight_server.instance.deleteFlight(id, flightNum);
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryFlight(int id, int flightNumber) throws RemoteException{
        Trace.info("Function queryFlight triggered at the middleware");
        synchronized(flight_server){
            while(true){
                try{
                    return flight_server.instance.queryFlight(id, flightNumber);
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) throws RemoteException{
        Trace.info("Function queryFlightPrice triggered at the middleware");
        synchronized(flight_server){
            while(true){
                try{
                    return flight_server.instance.queryFlightPrice(id, flightNumber);
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int price) throws RemoteException{
        Trace.info("Function addCars triggered at the middleware");
        synchronized(car_server){
            while(true){
                try{
                    car_server.instance.addCars(id, location, numCars, price);
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteCars(int id, String location) throws RemoteException{
        Trace.info("Function deleteCars triggered at the middleware");
        synchronized(car_server){
            while(true){
                try{
                    return car_server.instance.deleteCars(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryCars(int id, String location) throws RemoteException{
        Trace.info("Function queryCars triggered at the middleware");
        synchronized(car_server){
            while(true){
                try{
                    return car_server.instance.queryCars(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryCarsPrice(int id, String location) throws RemoteException{
        Trace.info("Function queryCarsPrice triggered at the middleware");
        synchronized(car_server){
            while(true){
                try{
                    return car_server.instance.queryCarsPrice(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException{
        Trace.info("Function addRooms triggered at the middleware");
        synchronized(room_server){
            while(true){
                try{
                    room_server.instance.addRooms(id, location, numRooms, price);
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteRooms(int id, String location) throws RemoteException{
        Trace.info("Function deleteRooms triggered at the middleware");
        synchronized(room_server){
            while(true){
                try{
                    return room_server.instance.deleteRooms(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryRooms(int id, String location) throws RemoteException{
        Trace.info("Function queryRooms triggered at the middleware");
        synchronized(room_server){
            while(true){
                try{
                    return room_server.instance.queryRooms(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
    }

    @Override
    public int queryRoomsPrice(int id, String location) throws RemoteException{
        Trace.info("Function queryRoomsPrice triggered at the middleware");
        synchronized(room_server){
            while(true){
                try{
                    return room_server.instance.queryRoomsPrice(id, location);
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
    }

    @Override
    public String queryCustomerInfo(int id, int customerID) throws RemoteException {
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if (c == null){
            Trace.warn("Invalid customer ID, ID not found on ResourceManager");
            return "";
        }
        Trace.info("queryCustomerInfo triggered at middleware");
        String res = c.getBill();
        return res;
    }

    @Override
    public boolean deleteCustomer(int id, int customerID) throws RemoteException {
        Trace.info("Function deleteCustomer triggered at the middleware");
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        
        if(c == null){
            Trace.warn("deleted customer that is not exist");
            return false;
        }
        synchronized(c){
            while(true){
                try{
                    RMHashMap reservations = c.getReservations();
                    for (String key : reservations.keySet()) {
                        ReservedItem reserveditem = c.getReservedItem(key);
                        if(key.contains("flight")){
                            flight_server.instance.removeReservation(reserveditem.getKey(), id, customerID,reserveditem.getCount());
                        }else if (key.contains("car")){
                            car_server.instance.removeReservation(reserveditem.getKey(), id, customerID, reserveditem.getCount());
                        }else{
                            room_server.instance.removeReservation(reserveditem.getKey(), id, customerID, reserveditem.getCount());
                        }
                    }
                    break;
                }catch(Exception e){
                    Trace.error("Connect error with the server or middleware");
                    // this.room_server.connectServer();
                }
            }
        }
        super.removeData(id, c.getKey());
        return true;
    }

    @Override
    public boolean reserveFlight(int id, int customerID, int flightNumber) throws RemoteException {
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(flight_server){
                while(true){
                    try {
                        String flight_key =  Flight.getKey(flightNumber);
                        String location = String.valueOf(flightNumber);
                        boolean res = flight_server.instance.reserve_flight_server(id, flightNumber);
                        int flight_price = flight_server.instance.queryFlightPrice(id, flightNumber);

                        if(res){ 
                            c.reserve(flight_key, location, flight_price);
                            writeData(id, Customer.getKey(customerID), c);
                            Trace.info("reserveFlight triggered at middleware");
                            return true;
                        }else{
                            Trace.warn("The flight cannot be reserved, flight_server returns a False");
                            return false;
                        }
                    } catch (Exception e) {
                        Trace.error("Connect error with the flight server");
                        flight_server.connectServer();
                        // TODO: handle exception
                    }
                }
            }
        }

        // return true;
    }

    @Override
    public boolean reserveCar(int id, int customerID, String location) throws RemoteException {
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(car_server){
                while(true){
                    try {
                        String car_key = Car.getKey(location);
                        String location_ = location;
                        boolean res = car_server.instance.reserve_car_server(id, location);
                        int car_price = car_server.instance.queryCarsPrice(id, location_);

                        if(res){ 
                            c.reserve(car_key, location_, car_price);
                            writeData(id, Customer.getKey(customerID), c);
                            Trace.info("reserveCar triggered at middleware");
                            return true;
                        }else{
                            Trace.warn("The car cannot be reserved, car_server returns a False");
                            return false;
                        }
                    } catch (Exception e) {
                        Trace.error("Connect error with the Car server");
                        car_server.connectServer();
                        // TODO: handle exception
                    }
                }
            }
        }

        // return true;
    }

    @Override
    public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(room_server){
                while(true){
                    try {
                        String room_key = Room.getKey(location);
                        String location_ = location;
                        boolean res = room_server.instance.reserve_room_server(id, location_);
                        int room_price = room_server.instance.queryRoomsPrice(id, location_);

                        if(res){ 
                            c.reserve(room_key, location_, room_price);
                            writeData(id, Customer.getKey(customerID), c);
                            Trace.info("reserveRoom triggered at middleware");
                            return true;
                        }else{
                            Trace.warn("The Room cannot be reserved, room_server returns a False");
                            return false;
                        }
                    } catch (Exception e) {
                        Trace.error("Connect error with the flight server");
                        room_server.connectServer();
                        // TODO: handle exception
                    }
                }
            }
        }

        // return true;
    }

    @Override
    public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        boolean all_reserved = true;
        synchronized(c){
            synchronized(flight_server){
                for(String flight_number : flightNumbers){
                    boolean added_flight = reserveFlight(id, customerID, Integer.parseInt(flight_number));
                    if(!added_flight){
                        Trace.warn("Cannot reserve Flight with flight number " + flight_number);
                        all_reserved = false;
                    }

                }
            }
            if(car){
                synchronized(car_server){
                    boolean added_car = reserveCar(id, customerID, location);
                    if(!added_car){
                        Trace.warn("Cannot reserve car with car location " + location);
                        all_reserved = false;
                    }
                }
            }
            if(room){
                synchronized(room_server){
                    boolean added_room = reserveRoom(id, customerID, location);
                    if(!added_room){
                        Trace.warn("Cannot reserve room with room location " + location);
                        all_reserved = false;
                    }
                }

            }


        }

        if(all_reserved){
            Trace.info("Bundle Succeed!");
            return all_reserved;
        }
        Trace.warn("Bundle failed during the process");
        return all_reserved;
    }

    public String generate_analytics(int id, int quantity) throws RemoteException{
        String res = "";
        try {
        
            String flight_server_result = flight_server.instance.generate_analytics(id, quantity);
            String car_server_result = car_server.instance.generate_analytics(id, quantity);
            String room_server_result = room_server.instance.generate_analytics(id, quantity);
            res += flight_server_result + '\n' +  car_server_result + '\n' +room_server_result;
        } catch (Exception e) {

            res += "An Error occured when try to access quantities";
        // TODO: handle exception

        }
        return res;

    }

}

