package paxos;

import java.io.Serializable;
class AcceptAck implements Serializable {
    private final boolean accept;
    private final BallotID ballotID;

    AcceptAck(boolean accept, BallotID ballotID) {
        this.accept = accept;
        this.ballotID = ballotID;
    }

    boolean isAccept() {
        return accept;
    }

    BallotID getBallotID() {
        return ballotID;
    }
}
