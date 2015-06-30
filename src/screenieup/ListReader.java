/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.Path;


/**
 *
 * @author Daniel Munkacsi
 */

public class ListReader {
    
    private final int COUNT_POSITION = 0;
    private String path;
    
    /**
     * Initialise list reader.
     * @param p the path name
     */
    public ListReader(String p){
        path = p;
    }
    
    /**
     * Read a text file storing each line into an element of an ArrayList<String>.
     * @return the ArrayList containing the lines read from the file
     */
    public ArrayList<String> readList(){
        ArrayList<String> list = new ArrayList<>();
        Path file = Paths.get(path);
        if(Files.exists(file) && Files.isReadable(file)) {
            try {
                // File reader
                BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
                String line;
                // read each line
                while((line = reader.readLine()) != null) {
                    list.add(line);
                }
                reader.close();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    
    /**
     * Collect the the output from readList() to a single String.
     * Concatenate each element to the String to be returned.
     * Each element will be separated by the separator string supplied.
     * @param al the list returned by readList() containing lines from the file
     * @param separator the character that separates each element (note, this is a string and not a char)
     * @return the single string containing all elements concatenated
     */
    public String listToString(ArrayList<String> al, String separator){ // add a parameter 'String separator' for the separating variable, e.g. "," or ":" or "-"
        String s = "";
        for(String element: al){
            s += element + separator; // then concatenate 'separator'
        }
        return s;
    }
    
    /**
     * Take the element with the supplied index of the supplied ArrayList<String> and parse it to an int.
     * (The first element is equivalent to the first line in the count file.)
     * @param al the list returned by readList() containing lines from the file
     * @param index the element of the array list to be parsed to an int
     * @return the parsed integer value
     */
    public int getCount(ArrayList<String> al, int index){
        int i = 0;
        try{
            i = Integer.parseInt(al.get(index));
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(null, "Failed to get count from the given file. Reason: couldn't parse integer.", "Error", ERROR_MESSAGE);
        }
        return i;
    }
    
    /**
     * Getter for path.
     * @return the path
     */
    public String getPath(){
        return path;
    }
    
    /**
     * Setter for path.
     * @param pth the new path
     */
    public void setPath(String pth){
        path = pth;
    }
}

