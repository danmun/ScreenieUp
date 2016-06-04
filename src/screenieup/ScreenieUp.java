/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * FEATURES TO ADD:
 * - Ability to paste text into pasting area and the program uploads it as .txt (where hosts allow), pretty much a pastebin
 * - Pasting an image (not a file) from clipboard doesn't work for pomf
 * - Progress window during upload should terminate upload if closed
 * - Write each uploaded image link to a file
 * - Create a command line handler, make the app operable from command line. Requires Scanner which should work from a swing worker thread.
 * - Instant upload. With instant upload ON, when the JFrame gains focus, clipboard content is grabbed and uploaded to selected host.
 * - Instant upload V2. Implement JIntellitype to listen for printscreen button when app is in the background and grab image as soon as it is pressed. (USER'S DISCRETION ADVISED)
 * - If Uguu is the selected host, user should be able to upload any file type. When a file is dropped, it is recognised as a file so this shouldn't be read into an image straight away,
 *   but rather into a byte output stream or something like that.
 */

        // config file content has format: 
        //---------file begins---------
        //|insta-upload 0             |   this line says what position the instant upload toggle was left when user last used the app, 0 for off, 1 for on (instauploadLine)
        //|host 1                     |   this line says which image hoster the user used last before closing the app (hostLine)
        //----------file ends----------

/**
 * 
 * @author Daniel Munkacsi
 */
public class ScreenieUp implements Hosts{
    
    private static final String TOGGLE_FILENAME = "toggleValue.txt";
    private static final int DEFAULT_HOST_VALUE = 1; // 0 = imgur, 1 = uguu
    private static final int DEFAULT_TOGGLE_VALUE = 0; // 0 = instant upload DISABLED
    private static final String INSTAUPLOAD_LINE = "insta-upload";
    private static final String HOST_LINE = "host";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File toggleFile = new File(TOGGLE_FILENAME);
        int toggleValue = DEFAULT_TOGGLE_VALUE;
        int hostValue = DEFAULT_HOST_VALUE;
        String instaupload = "disabled"; // for console printing only, not used elsewhere
        //check if the config file exists
        if(!toggleFile.exists()){ // if DOESN'T EXIST, create it and write default values to it
            try {
                String warning = "------------------\n"
                        +           "WARNING !" 
                        + "\n" + "------------------" + "\n" 
                        + "This program has a feature called instant uploading.\n"
                        + "This feature is DISABLED by default but you can ENABLE it by toggling the button in the top left corner of the program window.\n"
                        + "GREY is for OFF and GREEN is for ON.\n"
                        + "Turning this feature on will mean that the next time you start the program, the content found in your clipboard (if an image) will immediately be uploaded to the internet.\n"
                        + "Since you can toggle this feature, it is  YOUR responsibility to make sure you have NO SENSITIVE IMAGE in your clipboard before choosing to run the program in this mode.\n"
                        + "\n"
                        + "\n"
                        + "The point of this feature is convenience.\n"
                        + "With instant uploading enabled, it will take you 1 key press and 1 double-click to upload a pic: taking the screenshot and opening this app.\n\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "This warning message will NOT be displayed anymore.\n"
                        + "--------------------------------------------------------------------------------";
                JOptionPane.showMessageDialog(null, warning, "Warning", JOptionPane.WARNING_MESSAGE);
                System.out.println("First app start detected.");
                System.out.println("Creating config file...");
                toggleFile.createNewFile();
                String initialData = INSTAUPLOAD_LINE + " " + Integer.toString(DEFAULT_TOGGLE_VALUE) + "\n" + "" + HOST_LINE + " " + String.valueOf(DEFAULT_HOST_VALUE);
                System.out.println(initialData);
                new ListWriter(TOGGLE_FILENAME).writeList(initialData, false);
                System.out.println("Config file created!");
                System.out.println("Instant uploading is " + instaupload + ". " + "Host will be " + hostValue + ".");
            } catch (IOException ex) {
                System.out.println("Problem creating file!");
                JOptionPane.showMessageDialog(null, "An error occurred while accessing the config file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{ // if DOES EXIST, read the values from it into the appropriate variables
            System.out.println("Reading config file...");
            ListReader lr = new ListReader(TOGGLE_FILENAME);
            String filecontent = lr.listToString(lr.readList(), " - ");
            System.out.println("filecontent = " + filecontent);
            String[] filecontents = filecontent.split(" - ");
            String[] firstline = filecontents[0].split(" ");
            toggleValue = Integer.parseInt(firstline[1]);
            String[] secondline = filecontents[1].split(" ");
            hostValue = Integer.parseInt(secondline[1]);
            if(toggleValue == 1) instaupload = "enabled";
            System.out.println("Config loaded.");
            System.out.println("Instant upload is " + instaupload + ". " + "Host will be " + hostValue + ".");
        } 
        
        // proceed to opening the gui
        GUI gui = new GUI(toggleValue);
        
        if(toggleValue == DEFAULT_TOGGLE_VALUE){ // if toggle is off, we don't need to grab pic straight away, so we just display the gui
            // now make the appropriate radio button the selected one (the one that was last used before exiting the app) AND set the appropriate host
            gui.setHost(hostValue);
            gui.getRadioBtn(hostValue).setSelected(true);
            gui.setVisible(true);
        }else{ // otherwise, we need to grab the image, display the gui and the thumbnail then proceed to upload straight away
            System.out.println("Preparing for instant upload...");
            gui.setHost(hostValue);
            gui.getRadioBtn(hostValue).setSelected(true);
            gui.setVisible(true);
            gui.handlePaste();
        }
    }
}