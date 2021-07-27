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

public class LZdecode {
    public static void main(String[] args) throws IOException {

		//read all data from standard in to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[32 * 1024];

		int bytesRead;
		//while there is more input
		while ((bytesRead = System.in.read(buffer)) > 0) 
		{
			baos.write(buffer, 0, bytesRead);
		}
		byte[] bytes = baos.toByteArray();
		


		//create a bitset large enough to hold all of the bytes
        BitSet bitset = new BitSet(8 * bytes.length);

		//for each byte in the byte array
        for (int x = 0; x < bytes.length; x++)
		{
			//gets the current byte as a binary string
            String binary = byteToString(bytes[x]);
            
            int y = 0;
            // parse byte in reverser order
            //for each bit in the byte
            for (int i = 7; i >= 0; i--) 
            {
            	//if the bit at pos i in the byte is set set tne matching pos in the bitset
                if(binary.charAt(i) == '1')
                {
                    bitset.set(x * 8 + y);
                }
                y++;
            }
        }


        boolean isTwoBytes = !bitset.get(0);
        boolean endsOnMissmatch = !bitset.get(1);
        
        ArrayList<Byte> listToPrint = new ArrayList<>(); //the reconstructed file
        ArrayList<ArrayList<Byte>> dictionary = new ArrayList<>(); //the dictionary
        
        //if the file was encoded with shorts
        if (isTwoBytes)
        {
            //for each short+byte(pair) worth of bits in the bitset
            for(int i = 2; i < bitset.length(); i+=24)
            {
            	//get bitset that has the bits of the short in it, then convert it to a bitstring, then convert that bitstring to a short
                short phraseNum = stringToShort(bitSetToString(bitset.get(i, i + 16), 16));
                
            	//get bitset that has the bits of the byte in it, then convert it to a bitstring, then convert that bitstring to a byte
                byte mismatch = stringToByte(bitSetToString(bitset.get(i + 16, i + 24), 8));
                
                ArrayList<Byte> phrase = new ArrayList<>();
                
                //if the phrase number is not 0 then we want to add the path to the node to the phrase
                if(phraseNum != 0)
                {
                	//add the path to the node
                    phrase.addAll(dictionary.get(phraseNum - 1));
                }
                
                phrase.add(mismatch);
                
                //add the new phrase to the dictionary
                dictionary.add(phrase);
                
                //add the new phrase to the list to print
                listToPrint.addAll(phrase);
            }
        }
        //else the file was encoded with ints
        else
        {
            //for each int+byte(pair) worth of bits in the bitset
            for(int i = 2; i < bitset.length(); i+=40)
            {
            	//get bitset that has the bits of the int in it, then convert it to a bitstring, then convert that bitstring to a int
                int phraseNum = stringToInt(bitSetToString(bitset.get(i, i + 32), 32));
                
            	//get bitset that has the bits of the byte in it, then convert it to a bitstring, then convert that bitstring to a byte
                byte mismatch = stringToByte(bitSetToString(bitset.get(i + 32, i + 40), 8));
                
                ArrayList<Byte> phrase = new ArrayList<>();
                
                //if the phrase number is not 0 then we want to add the path to the node to the phrase
                if (phraseNum != 0)
                {
                	//add the path to the node
                    phrase.addAll(dictionary.get(phraseNum - 1));
                }
                
                phrase.add(mismatch);
                
                //add the new phrase to the dictionary
                dictionary.add(phrase);
                
                //add the new phrase to the list to print
                listToPrint.addAll(phrase);
            }
        }
		//if we end on a missmatch
		if(!endsOnMissmatch)
		{
		    byte data[] = new byte[listToPrint.size()];
		    
		    //for each byte in the list
		    for(int i = 0; i < listToPrint.size(); i++)
		    {
		    	//set the array to match the list
		        data[i] = listToPrint.get(i);
			}
		
			//write the byte array to system out
			OutputStream out = new BufferedOutputStream(System.out);
		    out.write(data, 0, data.length);
		    out.flush();
		}
		//if we don't end on a missmatch
		else
		{
		    byte data[] = new byte[listToPrint.size() - 1];
		    
		    //for each byte in the list
		    for(int i = 0; i < listToPrint.size() - 1; i++)
		    {
		    	//set the array to match the list
		        data[i] = listToPrint.get(i);
			}
		
			//write the byte array to system out
			OutputStream out = new BufferedOutputStream(System.out);
		    out.write(data, 0, data.length);
		    out.flush();
		}
    }

	//create a bitstring from a bitset and a length
    private static String bitSetToString(BitSet b, int size){
    
        StringBuilder stringBuilder = new StringBuilder();
        
        //for the length of the string
        for(int i = 0; i < size; i++)
        {
        	//if the bit at pos i is a 1 we add a 1 to the string
            if(b.get(i))
            {
                stringBuilder.append('1');
            }
            //else the bit at pos i is a 0 we add a 0 to the string
            else
            {
                stringBuilder.append('0');
            }
        }
        return stringBuilder.toString();
    }

	//converts a byte to a binary string
    private static String byteToString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

	//create a short from the string
    private static short stringToShort(String s){
        return Short.parseShort(s, 2);
    }
    
	//create a int from the string
    private static int stringToInt(String s){
        return Integer.parseInt(s, 2);
    }
    
	//create a byte from the string
    private static byte stringToByte(String s){
        return (byte) ((byte) Integer.parseInt(s, 2) & 0xFF);
    }
}




