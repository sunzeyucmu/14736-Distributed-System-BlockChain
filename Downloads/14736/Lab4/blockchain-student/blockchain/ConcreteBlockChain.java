import lib.Block;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.commons.codec.digest.*;

/**
 * Concrete Class Implementing the BlockChainBase Interface
 */
public class ConcreteBlockChain implements BlockChainBase{
    private static final int RANDOM_BYTES_SIZE = 32;

    private int NodeID;

    /* Need to Pass the Node instance into Concrete Block Chain Object
        <The Node Object in possess of this Concrete Block Chain>
     */
    private Node Node;

    private int difficulty; // Difficulty for 'Proof-of-Work'
    private String pof_Hash_prefix; // Hash Prefix for Proof-of-work (Difficulty Number of Zeros)

    private Random randomGenerator; // random number generator

    /* Queue for Minded Block */
//    private Block minded_Block;
    private Queue<Block> Minded_Block_Queue;

    /* The real List of Blocks */
    private List<Block> block_chains;

    /**
     * Constructor
     * @param NodeID Need to Pass in the NodeID to the constructor
     * @return
     */
    public ConcreteBlockChain(int NodeID, Node node){
        this.NodeID = NodeID;
        this.Node = node;

        // Difficulty Level Default to 20
//        this.difficulty = 4; // Just For Now
//        generateHashPrefix();
        this.randomGenerator = new Random();

        this.Minded_Block_Queue = new LinkedList<Block>();

        this.block_chains = new ArrayList<Block>();
        /* Add Genesis Block to the Chain Upon the Start of the Node*/
        System.out.println("Node"+this.NodeID+" Added the first Genesis Block, Node Launched!");
        this.block_chains.add(createGenesisBlock());

    }

    /**
     * Add New Block to This Node's Chain
     * @param block the block to be added.
     * @return
     */
    @Override
    public boolean addBlock(Block block) {
        /* Need Validation of the Block First */
        Block cur_lastBlock = getLastBlock();

        if(isValidNewBlock(block, cur_lastBlock)){
            System.out.println("In Node"+this.NodeID+", Add new Block: ["+block.toString()+"]");
            this.block_chains.add(block);
            return true;
        }

        return false;
    }

    /**
     * The First Block in the BlockChain, No Previous Block hash
     * Can be Directly Hardcoded
     * @return
     */
    @Override
    public Block createGenesisBlock() {
        /* The First Block of Each BlockChain is Completely Hardcoded
           Take Random Hash, Previous Hash, Data
         */
        /* all Genesis Block Should Have the Same current Hash Value
                <For Adding The First Block in the future>
         */
//        String genesis_Hash = "GENESIS_BLOCK";
//        String random_PrevHash = generateRandomString();
//        String data = generateRandomString();

        String genesis_Hash = "GENESIS_BLOCK";
        String random_PrevHash = "GENESIS_PREVIOUS_HASH";
        String data = "GENESIS_RANDOM_DATA";
        long time_stamp = System.currentTimeMillis();

        Block genesisBlock = new Block(genesis_Hash, random_PrevHash, data, time_stamp);
        genesisBlock.setIndex(0);
        genesisBlock.setDifficulty(this.difficulty);
        genesisBlock.setNonce(0);

        return genesisBlock;
    }

    /**
     * New Block Mining
     * @param data the data contained in the block
     * @return
     */
    @Override
    public byte[] createNewBlock(String data) {
        /* Calculate hash based on all the information contained in the block
            (prev_hash + timestamp + data + difficulty + Nonce)and the hash of the previous block.

            The proof-of-work needs to calculate a hash with a prefix containing number of difficulty 0s.
            Here we simply add a nonce into our block to achieve our goal.
            Brute-force enumerate all possible nonce until mine the hash that meet the requirement.
            use SHA256 to calculate hash <commons.codec.digest.DigestUtils.sha256Hex()>
         */

        int index = getLastBlock().getIndex() + 1;

        long nonce = Long.MIN_VALUE;
        /* Index + PreviousHash + data + timestamp + difficulty */
        String BlockInfo = Block.SEPARATOR+index+Block.SEPARATOR+getLastBlock().getHash()+Block.SEPARATOR
                +data+Block.SEPARATOR+System.currentTimeMillis()+Block.SEPARATOR+this.difficulty+Block.SEPARATOR;

        String curHash = "";
        /* Enumerate Over all Possible Long Nonce Value */
        while(nonce < Long.MAX_VALUE){
            byte[] curData = (BlockInfo + nonce).getBytes();
            /* Calculates the SHA-256 digest and returns the value as a hex string */
            curHash = DigestUtils.sha256Hex(curData);
            if(curHash.startsWith(this.pof_Hash_prefix)){
                System.out.println("In Node"+this.NodeID+", new Block Mined: "+curHash);
                break;
            }
            else{
//                System.out.println("In Node"+this.NodeID+", Invalid hash: "+curHash);
                nonce ++;
            }
        }

        /* The Complete Block String Representation */
        String Block_str = curHash+BlockInfo+nonce;

        Block new_block = Block.fromString(Block_str);

        /* add the newly Mined Block to the Queue */
//        this.minded_Block = new_block;
        this.Minded_Block_Queue.offer(new_block);

        return  Block_str.getBytes();
    }

    /**
     *  After a block is mined,
     *  the node should broadcast this new block out to all the nodes on the network
     *  to acquire approval to add this block to the chain.
     * @return
     */
    @Override
    public boolean broadcastNewBlock() {

        int peer_num = this.Node.getPeerNumber();

        if(Minded_Block_Queue.peek() == null){
            System.err.println("No Valid Mined Block");
        }
        /* Remove the Head of Minded Blocks Queue */
        Block minded_Block = this.Minded_Block_Queue.poll();
        /* Broadcast to all Nodes (Clients) <Including this Node itself>*/
        System.out.println("Node"+this.NodeID+" Starts to BroadCast Mined Block: "+minded_Block.getIndex());


        for(int i=0; i<peer_num; i++){
            try {
                if (this.Node.broadcastNewBlockToPeer(i, minded_Block.toString().getBytes())) {
                    System.out.println("Node"+i+" Accept Mined Block ["+minded_Block.getIndex()+ "] From Node"+this.NodeID);
                }
                else{
                    System.out.println("Node"+i+" Rejected Mined Block ["+minded_Block.getIndex()+ "] From Node"+this.NodeID);
                    return false;
                }
            }
            catch (RemoteException re){
                re.printStackTrace();
                System.exit(-1);
            }
        }

        return true;
    }


    /**
     * Set the Difficulty Level
     * @param difficulty the difficulty
     */
    @Override
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
//        System.out.println("Node"+this.NodeID+" 's Difficulty Level Changed to "+this.difficulty);

        /* Update Hash Prefix based on Difficulty value */
        generateHashPrefix();
    }

    /**
     * Generate String Representation of Current Block Chain
     * @return
     */
    @Override
    public byte[] getBlockchainData() {
        StringBuilder builder = new StringBuilder(this.block_chains.get(0).toString()); // Append the Genesis Block First

        for(int i=1; i<getBlockChainLength(); i++){
            builder.append(Block.BLOCK_SEPARATOR);
            builder.append(this.block_chains.get(i).toString());
        }

        String ret_str = builder.toString();
        byte[] ret = ret_str.getBytes();

        return ret;
    }

    /**
     * When a Node is Connected or Reconnected to the Network,
     * it needs to Download the block chain from its peer
     */
    @Override
    public void downloadBlockchain() {
        /* Calling 'getBlockChainDataFromPeer' method by using the Node Instance */
        List<Block> candidate_Block_chain = new ArrayList<Block>();

        /* Download BlockChains from others
           Choose the one with the longest length
         */
        int num_peers = Node.getPeerNumber();

        for(int i=0; i<num_peers; i++){
            if(i != this.NodeID){
                try {
                    byte[] downloaded = this.Node.getBlockChainDataFromPeer(i);
                    List<Block> cur_bc = parseBlockChain(downloaded);
                    /* Shorter Chain, Ignore */
                    if(cur_bc.size() < candidate_Block_chain.size()){
                        continue;
                    }
                    /* Longer Chain, Accept */
                    else if(cur_bc.size() > candidate_Block_chain.size()){
                        System.out.println("Node"+this.NodeID+" Switch to apply Chains From Node"+i);
                        candidate_Block_chain = cur_bc;
                    }
                    /* For blockchains with the same length
                       Choose the one containing
                       the earliest timestamp in the last block.
                     */
                    else{
                        long cur_chain_last_block_tstmp = cur_bc.get(cur_bc.size()-1).getTimestamp();
                        if(cur_chain_last_block_tstmp < candidate_Block_chain.get(getBlockChainLength()-1).getTimestamp()){
                            System.out.println("Node"+this.NodeID+" Switch to apply Chains From Node"+i);
                            candidate_Block_chain = cur_bc;
                        }
                    }
                }
                catch (RemoteException re){
                    re.printStackTrace();
                    System.exit(-1);
                }
            }
        }

        if(candidate_Block_chain.size() == 0){
            System.err.println("Error: Node"+this.NodeID+" Failed to Download BloackChain From Peers");
            System.exit(-1);
        }

        this.block_chains = candidate_Block_chain;
    }

    @Override
    public void setNode(Node node) {
        if(node == null){
            System.err.println("Setting Empty Node!");
            System.exit(-1);
        }
        this.Node = node;
    }

    /* Carry Out the Validation of New Block
            While Taking the previous Block as Reference
     */
    @Override
    public boolean isValidNewBlock(Block newBlock, Block prevBlock) {
        /* 1. Index */
        int prev_index = prevBlock.getIndex();
        if (newBlock.getIndex() != prev_index + 1){
            System.out.println("In Node"+this.NodeID+" new Block ["+newBlock.toString()+"] Incompatible with current Last Block ["+prevBlock.toString()+"]");
            return false;
        }
        /* 2. Previous Hash Value */
        String prev_hash = prevBlock.getHash();
        if(!prev_hash.equals(newBlock.getPreviousHash())){
            System.out.println("In Node"+this.NodeID+" new Block's Previous hash"+newBlock.getPreviousHash()+" Incompatible with current Last Block's Hash"+prevBlock.getHash());
            return false;
        }
        /* 3. Hash Correction < Hash_Prefix Proof of Work Verification>*/
        String cur_hash = newBlock.getHash();
        if(!cur_hash.startsWith(this.pof_Hash_prefix)){
            System.out.println("In Node"+this.NodeID+" new Block's Hash"+newBlock.getHash()+" Incompatible with current Difficulty Hash Prefix"+this.pof_Hash_prefix);
            return false;
        }

        return true; // Pass all Examination Afterwards
    }

    @Override
    public Block getLastBlock() {
        Block lastBlock = this.block_chains.get(this.block_chains.size()-1);
        if(lastBlock == null){
            System.out.println("Node"+this.NodeID+" contains Empty Block Chains");
        }
        return lastBlock;
//        return null;
    }

    @Override
    public int getBlockChainLength() {
        if(this.block_chains == null){
            System.err.println("Node"+NodeID+" 's Chain is still Empty!");
        }
        return this.block_chains.size();
    }

    /**
     * Helper Functions
     */

    /**
     * Generate Random String By Random Bytes
     * @return
     */
    public String generateRandomString(){
        byte[] randomBytes = new byte[RANDOM_BYTES_SIZE];
        randomGenerator.nextBytes(randomBytes); // Result is placed in the User-supplied Array
        String ret = new String(randomBytes, Charset.defaultCharset());

        return ret;
    }

    /**
     *  get Hash Prefix Based on the value of Difficulty
     */
    public void generateHashPrefix(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<this.difficulty/4; i++){
            sb.append('0');
        }

        this.pof_Hash_prefix = sb.toString();
//        System.out.println("Current Hash Prefix for PROOF OF WORK is "+this.pof_Hash_prefix);
    }

    /**
     * Parse BlockChain Sting Bytes to BlockChain List
     */
    public List<Block> parseBlockChain(byte[] block_chain_bytes){
        List<Block> ret = new ArrayList<Block>();

        String bc = new String(block_chain_bytes, Charset.defaultCharset());
//        System.out.println("Downloaded Block String: "+bc);
        String[] blocks = bc.split(Block.BLOCK_SEPARATOR);

        if(blocks == null){
            System.err.println("Invalid Block Chain String Representation: "+bc);
            System.exit(-1);
        }

        for(String block : blocks){
            ret.add(Block.fromString(block));
        }

        return ret;
    }
}
