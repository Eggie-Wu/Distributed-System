package paxos;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

class BallotID implements Comparable<BallotID>, Serializable {
    private final long curTime;
    private final String process;

    BallotID(String process) {
        this.curTime = System.nanoTime();
        this.process = process;
    }

    @Override
    public int compareTo(BallotID ballotID) {
        if(this.curTime - ballotID.curTime>0) return 1;
        else if(this.curTime - ballotID.curTime < 0) return -1;
        else return this.process.compareTo(ballotID.process);
    }

    @Override
    public String toString() {
        return curTime+" "+process;
    }
}
