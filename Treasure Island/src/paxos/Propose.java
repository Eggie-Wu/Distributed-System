package paxos;
import java.io.Serializable;

class Propose implements Serializable {
    private BallotID ballotID;

    Propose (BallotID ballotID){
        this.ballotID = ballotID;
    }

    BallotID getBallotID() {
        return ballotID;
    }
}
