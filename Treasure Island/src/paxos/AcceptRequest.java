package paxos;

import java.util.ArrayList;
import java.io.Serializable;

class AcceptRequest implements Serializable {
    private ArrayList<String> order;
    private BallotID ballotID;

    AcceptRequest(BallotID ballotID, ArrayList<String> order){
        this.ballotID = ballotID;
        this.order = order;
    }

    ArrayList<String> getOrder() {
        return order;
    }

    BallotID getBallotID() {
        return ballotID;
    }
}
