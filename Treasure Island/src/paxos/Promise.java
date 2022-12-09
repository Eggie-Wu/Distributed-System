package paxos;
import java.io.Serializable;
import java.util.ArrayList;

class Promise implements Serializable {
    private final boolean promise;
    private final String sender;
    private final BallotID ballotID;
    private BallotID otherID = null;
    private ArrayList<String> order = null;


    Promise(boolean promise, String sender, BallotID ballotID) {
        assert promise;
        this.promise = true;
        this.sender = sender;
        this.ballotID = ballotID;
    }

    Promise(boolean promise, String sender, BallotID ballotID,BallotID otherID) {
        assert !promise && ballotID.compareTo(otherID)<=0;
        this.promise = false;
        this.sender = sender;
        this.ballotID = ballotID;
        this.otherID = otherID;
    }

    Promise(boolean promise, String sender, BallotID ballotID, BallotID otherID, ArrayList<String> ORDER){
        assert promise && ballotID.compareTo(otherID)>0;
        this.promise = true;
        this.sender = sender;
        this.ballotID = ballotID;
        this.otherID = otherID;
        this.order = ORDER;
    }

    boolean isPromise() {
        return promise;
    }

    BallotID getBallotID() {
        return ballotID;
    }

    String getSender() {
        return sender;
    }

    BallotID getOtherID() {
        return otherID;
    }

    ArrayList<String> getOrder() {
        return order;
    }
}
