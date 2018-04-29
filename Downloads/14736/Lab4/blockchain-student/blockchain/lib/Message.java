package lib;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Indicates the source address.
     */
    private int src_addr;
    /**
     * Indicates the destination address.
     */
    private int dest_addr;
    /**
     * Indicates the message type.
     */
    private MessageType type;
    /**
     * The payload of the message packet.
     */
    private byte[] body;

    /**
     * Message - Construct a message to be sent within this network.
     *
     * @param type the message type
     * @param src_addr source
     * @param dest_addr destination
     * @param body payload
     */
    public Message(MessageType type, int src_addr, int dest_addr, byte[] body) {
        this.type = type;
        this.src_addr = src_addr;
        this.dest_addr = dest_addr;
        this.body = body;
    }

    public int getSrc() {
        return this.src_addr;
    }

    public int getDest() {
        return this.dest_addr;
    }

    public byte[] getBody() {
        return this.body;
    }

    public MessageType getType(){
        return this.type;
    }
}
