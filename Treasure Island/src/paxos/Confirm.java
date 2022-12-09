package paxos;
import java.io.Serializable;
import java.util.ArrayList;

class Confirm implements Serializable {
    private final ArrayList<String> order;
    private final BallotID ballotID;

    long receiveTime=0;

    Confirm(BallotID b, ArrayList<String> o){
        this.order = o;
        this.ballotID = b;
    }

    public ArrayList<String> getOrder() {
        return order;
    }

    public BallotID getBallotID() {
        return ballotID;
    }
}
