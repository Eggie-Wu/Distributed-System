package Server.Middleware;
import Server.Common.ReservedItem;
import Server.Common.ResourceManager;
import Server.Common.Trace;
import Server.Common.Customer;
import Server.Common.*;
import java.net.ServerSocket;
import java.util.*;

public class MiddlewareManager extends ResourceManager{
    protected RemoteServer flight_server;
    protected RemoteServer car_server;
    protected RemoteServer room_server;
    protected ServerSocket serverSocket;
    protected String name;

    public MiddlewareManager(String name, RemoteServer flight, RemoteServer car, RemoteServer room){
        super(name);
        this.flight_server = flight;
        this.car_server = car;
        this.room_server = room;
    }

    public void connectServers(){
        if(this.flight_server!=null) this.flight_server.connectServer();
        if(this.car_server!=null) this.car_server.connectServer();
        if(this.room_server!=null) this.room_server.connectServer();
    }

    @Override
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice){
        String result;
        synchronized(flight_server){
            StringBuilder sb = new StringBuilder();
            sb.append("AddFlight").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(Integer.toString(flightNum)).append(",");
            sb.append(Integer.toString(flightSeats)).append(",");
            sb.append(Integer.toString(flightPrice));
            while(true){
                try{
                    result = this.flight_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
        if(result.equals("true")) return true;
        else return false;    
    }
    

    public boolean addCars(int id, String location, int numCars, int price){
        String result;
        synchronized(car_server){
            StringBuilder sb = new StringBuilder();
            sb.append("addCars").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location).append(",");
            sb.append(Integer.toString(numCars)).append(",");
            sb.append(Integer.toString(price));
            while(true){
                try{
                    result = this.car_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
        if(result.equals("true")) return true;
        else return false;  
    }
   
    
    public boolean addRooms(int id, String location, int numRooms, int price) {
        String result;
        synchronized (room_server) {
            StringBuilder sb = new StringBuilder();
            sb.append("addRooms").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location).append(",");
            sb.append(Integer.toString(numRooms)).append(",");
            sb.append(Integer.toString(price));
            while(true){
                try{
                    result = this.room_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
        if (result.equals("true")) return true;
        else return false;
    }
  
    public boolean deleteFlight(int id, int flightNum){
        String result;
        synchronized(flight_server){
            StringBuilder sb = new StringBuilder();
            sb.append("deleteFlight").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(Integer.toString(flightNum));
            while(true){
                try{
                    result = this.flight_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
        if(result.equals("true")) return true;
        else return false; 
        
    }
	    
    public boolean deleteCars(int id, String location){
        String result;
        synchronized(car_server){
            StringBuilder sb = new StringBuilder();
            sb.append("deleteCars").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.car_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
        if(result.equals("true")) return true;
        else return false; 
        
    }

    public boolean deleteRooms(int id, String location){
        String result;
        synchronized(room_server){
            StringBuilder sb = new StringBuilder();
            sb.append("deleteRooms").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.room_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
        if(result.equals("true")) return true;
        else return false; 
    }
    
    public boolean deleteCustomer(int id, int customerID){
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
                            String result;
                            synchronized(flight_server){
                                StringBuilder sb = new StringBuilder();
                                sb.append("removereservation").append(",");
                                sb.append(reserveditem.getKey()).append(",");
                                sb.append(String.valueOf(id)).append(",");
                                sb.append(String.valueOf(customerID)).append(",");
                                sb.append(String.valueOf(reserveditem.getCount()));
                                while(true){
                                    try{
                                        result = this.flight_server.sendMessage(sb.toString());
                                        break;
                                    }catch (Exception e){
                                        Trace.error("Connect error with the flight server");
                                        this.flight_server.connectServer();
                                    }
                                }
                            }
                        }else if (key.contains("car")){
                            String result;
                            synchronized(car_server){
                                StringBuilder sb = new StringBuilder();
                                sb.append("removereservation").append(",");
                                sb.append(reserveditem.getKey()).append(",");
                                sb.append(String.valueOf(id)).append(",");
                                sb.append(String.valueOf(customerID)).append(",");
                                sb.append(String.valueOf(reserveditem.getCount()));
                                while(true){
                                    try{
                                        result = this.car_server.sendMessage(sb.toString());
                                        break;
                                    }catch (Exception e){
                                        Trace.error("Connect error with the car server");
                                        this.car_server.connectServer();
                                    }
                                }
                            }
                        }else if (key.contains("room")){
                            String result;
                            synchronized(room_server){
                                StringBuilder sb = new StringBuilder();
                                sb.append("removereservation").append(",");
                                sb.append(reserveditem.getKey()).append(",");
                                sb.append(String.valueOf(id)).append(",");
                                sb.append(String.valueOf(customerID)).append(",");
                                sb.append(String.valueOf(reserveditem.getCount()));
                                while(true){
                                    try{
                                        result = this.room_server.sendMessage(sb.toString());
                                        break;
                                    }catch (Exception e){
                                        Trace.error("Connect error with the room server");
                                        this.room_server.connectServer();
                                    }
                                }
                            }
                        }
                    }
                    break;
                }catch(Exception e){
                    Trace.error("Connect error with the server or middleware");
                }
            }
        }
        super.removeData(id, c.getKey());
        // return true;
        return true;
    }

    public int queryFlight(int id, int flightNumber){
        String result;
        synchronized(flight_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryFlight").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(Integer.toString(flightNumber));
            while(true){
                try{
                    result = this.flight_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }

    public int queryCars(int id, String location){
        String result;
        synchronized(car_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryCars").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.car_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }

    public int queryRooms(int id, String location){
        String result;
        synchronized(room_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryRooms").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.room_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }

    public String queryCustomerInfo(int id, int customerID){
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if (c == null){
            Trace.warn("Invalid customer ID, ID not found on ResourceManager");
            return "false";
        }
        Trace.info("queryCustomerInfo triggered at middleware");
        String res = c.getBill();
        return res;
    }
    
    public int queryFlightPrice(int id, int flightNumber){
        String result;
        synchronized(flight_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryFlightPrice").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(Integer.toString(flightNumber));
            while(true){
                try{
                    result = this.flight_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the flight server");
                    this.flight_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }

    public int queryCarsPrice(int id, String location){
        String result;
        synchronized(car_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryCarsPrice").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.car_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the car server");
                    this.car_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }

    public int queryRoomsPrice(int id, String location){
        String result;
        synchronized(room_server){
            StringBuilder sb = new StringBuilder();
            sb.append("queryRoomsPrice").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(location);
            while(true){
                try{
                    result = this.room_server.sendMessage(sb.toString());
                    break;
                }catch (Exception e){
                    Trace.error("Connect error with the room server");
                    this.room_server.connectServer();
                }
            }
        }
        return Integer.parseInt(result);
    }


    public boolean reserveFlight(int id, int customerID, int flightNumber){
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(flight_server){
                String flight_key =  Flight.getKey(flightNumber);
                String location = String.valueOf(flightNumber);
                String res;
                StringBuilder sb = new StringBuilder();
                sb.append("reserve_flight_server").append(",");
                sb.append(String.valueOf(id)).append(",");
                sb.append(String.valueOf(flightNumber));
                res = this.flight_server.sendMessage(sb.toString());
                // boolean res = flight_server.instance.reserve_flight_server(id, flightNumber);
                int flight_price;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("queryFlightPrice").append(",");
                sb2.append(String.valueOf(id)).append(",");
                sb2.append(String.valueOf(flightNumber));
                String p = this.flight_server.sendMessage(sb2.toString());
                
                flight_price = Integer.parseInt(p);
                // int flight_price = flight_server.instance.queryFlightPrice(id, flightNumber);

                if(res.equals("true")){ 
                    c.reserve(flight_key, location, flight_price);
                    writeData(id, Customer.getKey(customerID), c);
                    Trace.info("reserveFlight triggered at middleware");
                    return true;
                }else{
                    Trace.warn("The flight cannot be reserved, flight_server returns a False");
                    return false;
                }
            }
        }
        // return true;
    }

    public boolean reserveCar(int id, int customerID, String location){
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(car_server){
                while(true){
                    try {
                        String car_key =  Car.getKey(location);
                        String location_ = location;
                        String res;
                        StringBuilder sb = new StringBuilder();
                        sb.append("reserve_car_server").append(",");
                        sb.append(String.valueOf(id)).append(",");
                        sb.append(location);
                        res = this.car_server.sendMessage(sb.toString());
                        
                        // boolean res = flight_server.instance.reserve_flight_server(id, flightNumber);
                        int car_price;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("queryCarsPrice").append(",");
                        sb2.append(String.valueOf(id)).append(",");
                        sb2.append(location_);
                        String p = this.car_server.sendMessage(sb2.toString());
                        
                        car_price = Integer.parseInt(p);
                        // int flight_price = flight_server.instance.queryFlightPrice(id, flightNumber);

                        if(res.equals("true")){ 
                            c.reserve(car_key, location_, car_price);
                            writeData(id, Customer.getKey(customerID), c);
                            Trace.info("reserveFlight triggered at middleware");
                            return true;
                        }else{
                            Trace.warn("The flight cannot be reserved, flight_server returns a False");
                            return false;
                        }
                    } catch (Exception e) {
                        Trace.error("Connect error with the flight server");
                        car_server.connectServer();
                        // TODO: handle exception
                    }
                }
            }
        }
        // return true;
    }

    public boolean reserveRoom(int id, int customerID, String location){
        Customer c = (Customer)readData(id, Customer.getKey(customerID));
        if(c == null){
            Trace.warn("customer with such id does not exist");
            return false;
        }
        synchronized(c){
            synchronized(room_server){
                while(true){
                    try {
                        String room_key =  Room.getKey(location);
                        String location_ = location;
                        String res;
                        StringBuilder sb = new StringBuilder();
                        sb.append("reserve_room_server").append(",");
                        sb.append(String.valueOf(id)).append(",");
                        sb.append(location);
                        res = this.room_server.sendMessage(sb.toString());
                        
                        // boolean res = flight_server.instance.reserve_flight_server(id, flightNumber);
                        int car_price;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("queryRoomsPrice").append(",");
                        sb2.append(String.valueOf(id)).append(",");
                        sb2.append(location_);
                        String p = this.room_server.sendMessage(sb2.toString());
                        
                        car_price = Integer.parseInt(p);
                        // int flight_price = flight_server.instance.queryFlightPrice(id, flightNumber);

                        if(res.equals("true")){ 
                            c.reserve(room_key, location_, car_price);
                            writeData(id, Customer.getKey(customerID), c);
                            Trace.info("reserveFlight triggered at middleware");
                            return true;
                        }else{
                            Trace.warn("The flight cannot be reserved, flight_server returns a False");
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
    }

    public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room){
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
        // return true;
    }

    public String generate_analytics(int id, int quantity) {
        String res = "";
        try {
            StringBuilder sb = new StringBuilder();
            // StringBuilder sb2 = new StringBuilder();
            sb.append("generate_analytics").append(",");
            sb.append(Integer.toString(id)).append(",");
            sb.append(Integer.toString(quantity));
            res += this.flight_server.sendMessage(sb.toString()) + this.car_server.sendMessage(sb.toString()) + this.room_server.sendMessage(sb.toString());
            
        
        } catch (Exception e) {
            res += "An Error occured when try to access quantities";
            // TODO: handle exception
        }
        return res;
    }

    
    
    public String getName(){
        return this.name;
    }
}
