package lib;

/**
 * Block Class, the element to compose a Blockchain.
 */
public class Block {

    public final static String SEPARATOR = ",";

    public final static String BLOCK_SEPARATOR = ";"; // To Separate Blocks

    private String hash;

    private String previousHash;

    private String data;

    private long timestamp;

    private int difficulty;

    private long nonce;

    private int index; //Location in the Block Chain, Convenient for Adding New Block(Validation)

    public Block() {}

    public Block(String hash, String previousHash, String data,
                 long timestamp) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = timestamp;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

    /* Serialization for convert between Block Object and String Representation
     * index+","+hash+","+prev_hash+","+"data"+","+timestamp+","+difficulty+","+nonce
     * */

    public  static Block fromString(String s){
        String[] info = s.split(SEPARATOR);
        if(info == null || info.length != 7){
            System.err.println("Wrong Block String: "+s);
        }

        String hash = info[0];
        int index = Integer.parseInt(info[1]);
        String prev_hash = info[2];
        String data_ = info[3];
        Long tstmp = Long.parseLong(info[4]);
        int difficulty = Integer.parseInt(info[5]);
        Long nonce = Long.parseLong(info[6]);
        /* Create the Block Object */
        Block ret = new Block(hash, prev_hash, data_, tstmp);
        ret.setIndex(index);
        ret.setDifficulty(difficulty);
        ret.setNonce(nonce);

        return ret;
    }

    public  String toString(){
        String ret = this.hash+SEPARATOR+this.index+SEPARATOR+this.previousHash+SEPARATOR
                +this.data+SEPARATOR+this.timestamp+SEPARATOR+this.difficulty+SEPARATOR+this.nonce;

        return ret;
    }

}
