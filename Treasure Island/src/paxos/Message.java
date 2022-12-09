package paxos;
import java.io.Serializable;

class Message implements Serializable{
    private final String id;
    private final Object val;

    Message(String id, Object val) {
        this.id = id;
        this.val = val;
    }

    String getId() {
        return id;
    }

    Object getVal() {
        return val;
    }
}
