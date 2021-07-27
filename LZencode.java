//JasonTollison 1319030
//stania klegr 1339709
import java.io.IOException;
import java.util.BitSet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;

public class LZencode {
    public static void main(String[] args) throws IOException {

		//read all data from standard in to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[32 * 1024];

		int bytesRead;
		while ((bytesRead = System.in.read(buffer)) > 0) 
		{
			baos.write(buffer, 0, bytesRead);
		}
		byte[] bytes = baos.toByteArray();


        // build the trie from the byte array of the entire file
        Trie trie = new Trie();
        trie.buildTree(bytes);

        // get pairs in bits
        BitSet fileInBits = pairsToBitSet(trie.pairs, trie.fitsIn2Bytes, trie.endOnMissmatch);

        byte[] data = fileInBits.toByteArray();

		//write the byte array to system out
		OutputStream out = new BufferedOutputStream(System.out);
        out.write(data, 0, data.length);
        out.flush();
    }


	//returns the pairs data as a bitset of bytes
    private static BitSet pairsToBitSet(ArrayList<Pair<Integer, Byte>> pairs, boolean isTwoBytesRequired, boolean endOnMissmatch) {

        BitSet bitSet;
        
        //creates the bitset of the correct size based on if we need shorts or ints for the index space
        if (isTwoBytesRequired)
        {
            bitSet = new BitSet(pairs.size() * 24 + 1);
        } 
        else 
        {
            bitSet = new BitSet(pairs.size() * 40 + 1);
            bitSet.set(0);//flag that we are using ints to represent the indexes
        }
        
		if(endOnMissmatch)
		{
			bitSet.set(1);//flag for if we need to drop the last byte
		}

		//start at 2 because the first 2 bits are flags
        int i = 2;
        //for each pair in pairs
        for (Pair<Integer, Byte> pair : pairs) 
		{
			//if the phrasenumbers will fit in shorts
            if (isTwoBytesRequired) 
            {

                //get the phrasenumber from the pair as a short and store it in a string
                String s = shortToString(pair.getPhraseNum().shortValue());
                
                //loop through all 16 bits in the short
                for(int j = 0; j < 16; j++)
                {
                	//if the char at position j (the smaller loop) is 1 then set the coresponding
                	// position in the bitset to 1 i+j (big loop pos + small loop pos)
                    if(s.charAt(j) == '1')
                    {
                        bitSet.set(i + j);
                    }
                }
                //increment out position in the big loop by the length of a short 
                i += 16;
            } 
            //if we need full intergers to fit the phrasenumbers 
            else 
            {
                //get the phrasenumber from the pair and store it in a string
                String s = intToString(pair.getPhraseNum());
                
                //loop through all 32 bits in the int
                for(int j = 0; j < 32; j++)
                {
                	//if the char at position j (the smaller loop) is 1 then set the coresponding
                	// position in the bitset to 1 i+j (big loop pos + small loop pos)
                    if(s.charAt(j) == '1')
                    {
                        bitSet.set(i + j);
                    }
                }
                //increment out position in the big loop by the length of a int 
                i += 32;
            }
            //get the byte from the pair and store it as a string
            String s = byteToString(pair.getMissmatch());
            
            //loop through all 8 bits in the byte
            for(int j = 0; j < 8; j++)
            {
            	//if the char at position j (the smaller loop) is 1 then set the coresponding
            	// position in the bitset to 1 i+j (big loop pos + small loop pos)
                if(s.charAt(j) == '1')
                {
                    bitSet.set(i + j);
            	}
            }
            //increment out position in the big loop by the length of a byte 
            i+=8;
        }
        return bitSet;
    }

	//converts a byte to a string of 1s and 0s
    private static String byteToString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
	//converts a int to a string of 1s and 0s
    private static String intToString(int i){
        return String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
    }
	//converts a short to a string of 1s and 0s
    private static String shortToString(short s){
        return String.format("%16s", Integer.toBinaryString(s & 0xFFFF)).replace(' ', '0');
    }
}


class Trie {

    Node root = new Node();
    int nodeCount = 0;
    boolean fitsIn2Bytes = true;
	boolean endOnMissmatch = true;
    ArrayList<Pair<Integer, Byte>> pairs;

	//takes a byte array of the entire file to be encoded, builds a tree from it 
	//and stores the pairs to be written in the list
    void buildTree(byte[] word){
    
        Node curNode = root;
        pairs = new ArrayList<>();
		
		//for each byte in the array
        for (int i = 0; i < word.length; i++) 
		{
			//if the byte dosent exist in our children
            if (curNode.childern[word[i] + 128] == null) 
            {
                // create new leaf node
                curNode.childern[word[i] + 128] = new Node();
                
                //add the index and mismatch byte to the list of pairs
                pairs.add(new Pair<>(curNode.index, word[i]));
                
                nodeCount++;

				//set the new nodes index
                curNode.childern[word[i] + 128].index = nodeCount;
                
                // and begin again with root node
                curNode = root;
                continue;//returns us to the start of the loop
			}
			//if we are at the end of the file
			else if(i == word.length - 1)
			{
				endOnMissmatch = false;

				//add the last node to the tree
				curNode = curNode.childern[word[i] + 128];

				// if we in the end of file and there is no other symbol and we just write number of nodes in dictionary
				pairs.add(new Pair<>(curNode.index, (byte)0));
			}
			//set the current node
            curNode = curNode.childern[word[i] + 128];
        }
        // if the tree is too big then use integer (4 bytes)
        if(nodeCount > Short.MAX_VALUE)
        {
            fitsIn2Bytes = false;
    	}
    }
}


//trie node
class Node {
    Node[] childern = new Node[256];
    int index;

	//constructor
    Node(int index){
        this.index = index;
    }
	//constructor for a "blank" node
    Node(){
        index = 0;
    }
}
