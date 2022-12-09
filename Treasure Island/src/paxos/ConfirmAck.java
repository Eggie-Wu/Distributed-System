package paxos;
import java.io.Serializable;

class ConfirmAck implements Serializable {
    private final boolean confirm;
    private final BallotID ballotID;

    public ConfirmAck(boolean confirm, BallotID ballotID) {
        this.confirm = confirm;
        this.ballotID = ballotID;
    }

    boolean isConfirm() {
        return confirm;
    }

    BallotID getBallotID() {
        return ballotID;
    }
}
