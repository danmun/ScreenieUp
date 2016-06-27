/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import java.security.NoSuchAlgorithmException;
import java.net.MalformedURLException;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.io.BufferedInputStream;
import java.net.ProtocolException;
import java.net.HttpURLConnection;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import java.io.FileInputStream;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.util.List;
import java.io.File;
import java.net.URL;
import java.net.URI;


/**
 *
 * @author Daniel Munkacsi
 */
public class Updater extends SwingWorker<Void, Integer>{
    private final String[] updateText = {"Connecting to dropbox...","Getting response...","Hashing file in cloud...","Hashing file on your HDD...","Writing update to file...","Updated!"};
    public String applink = "https://www.dropbox.com/s/u59ak9hdru7jdi2/ScreenieUp.jar?dl=1";// at the end: "?dl=1" MUST BE 1 otherwise it will result in a corrupt file... go figure                        
    public String host = "http://www.dropbox.com"; // not used // gives 301 Permanently moved
    public final String classfile = "Updater.class";
    public String filename = "ScreenieUp.jar";
    public final String HASHING_ALGO = "SHA1";
    private JProgressBar progressBar;
    private String CLASS_PATH = null;
    private JDialog progressDialog;
    private JLabel progressLabel;
    public String jarDirectory;
    public File jarFile = null;
    private boolean updated;
    public String filepath;

    

    public Updater(JDialog jpd, JProgressBar jpb, JLabel jpl){
        updated = false;
        progressDialog = jpd;
        progressBar = jpb;
        progressLabel = jpl;
        progressBar.setMaximum(updateText.length);
        CLASS_PATH = this.getClass().getResource(classfile).getFile();
        CLASS_PATH = CLASS_PATH.substring(0, CLASS_PATH.indexOf("!/"));
        try {
            jarFile = new File(new URI(CLASS_PATH));
        } catch (URISyntaxException ex) {
            System.out.println("URISyntaxException in getResponse() while updating.");
        }
        
        try {
            filepath = jarFile.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        int i = filepath.indexOf(filename);
        char[] filepathChars = filepath.toCharArray();
        char[] newfilepathChars = new char[i];
        for(int j = 0; j < newfilepathChars.length; j++){
            newfilepathChars[j] = filepathChars[j]; 
        }
        jarDirectory = new String(newfilepathChars);
        System.out.println("Directory of running JAR file: " + jarDirectory);
    }
    
   @Override
    protected Void doInBackground() throws Exception {
        progressDialog.setVisible(true);
        HttpURLConnection conn = connect();
        getResponse(conn);
        return null;
    } 
    
    @Override
    protected void done(){
        if(!updated){ progressDialog.setVisible(false); return;}
        JOptionPane.showMessageDialog(null, "The app will now restart!");
        try {
            Runtime.getRuntime().exec(" java -jar " + filename);
        } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        progressDialog.setVisible(false);
        System.exit(0);
    }
    
    @Override
    protected void process(List<Integer> chunks){
        progressLabel.setText(updateText[chunks.get(chunks.size()-1)]); // The last value in this array is all we care about.
        progressBar.setValue(chunks.get(chunks.size()-1) + 1);
    }
    
    /**
     * Connect to update hoster.
     * @return the established connection
     */
    public HttpURLConnection connect(){
        publish(0); System.out.println("Connecting to dropbox...");
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(applink);
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException in connect()");
            return null;
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            System.out.println("IOException in connect()");
            return null;
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException ex) {
            System.out.println("ProtocolException in connect()");
            return null;
        }
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename); // read below comment
        conn.setRequestProperty("Content-Type", "application/java-archive"); // added in the case we just want to dl the jar file not the whole folder as zip
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return conn;
    }
    
    /**
     * Listen for a response from the update hoster.
     * @param conn the connection to use
     */
    public void getResponse(HttpURLConnection conn){
        publish(1); System.out.println("Getting response...");
        InputStream instream = null;
        try {
            instream = new BufferedInputStream(conn.getInputStream());
            System.out.println("Response code from server: " + conn.getResponseCode());
        } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MessageDigest md = null;
        String livehash = getOnlineHash(instream,baos,md);
        String jarhash = getCurrentHash(md);
        if(!livehash.equals(jarhash)){
            updated = true;
            int ans = JOptionPane.showConfirmDialog(null, "Update available.\nWould you like to update the app?", "Updater", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(ans != 0){updated = false; return;}
            save(baos);
            File f = new File("toggleValue.txt");
            if(f.exists()) f.delete();
        }else{
            JOptionPane.showMessageDialog(null,"No update was found.");
            try {
                baos.flush();// newly added, basically close baos if hash not the same to conserve memory
                baos.close();// newly added
            } catch (IOException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get hash of the live file.
     * @param instream the input stream to read
     * @param baos the output stream to write to before comparing
     * @param md the message digest object involved in the hashing process
     * @return the file hash
     */
    private String getOnlineHash(InputStream instream, ByteArrayOutputStream baos, MessageDigest md){
        publish(2); System.out.println("Hashing file in cloud...");
        String livehash = "Hashing error.";
        try {
            md = MessageDigest.getInstance(HASHING_ALGO);
            //get hash on the fly
            byte[] bufferForHash = new byte[1024];
            int bytesReadOnline = -1;
            //reading response into 2 streams, one to get hash and one which will potentially save file later
            while((bytesReadOnline = instream.read(bufferForHash)) != -1){
                md.update(bufferForHash, 0, bytesReadOnline);
                baos.write(bufferForHash, 0 ,bytesReadOnline);
            }
            baos.close(); // "has no effect" - javadoc for ByteArrayOutputStream
            instream.close();
            byte[] hashedBytes = md.digest();
            //converting hex to readable string
            livehash = hashToHex(hashedBytes);
            System.out.println("Hash of file on server: " + livehash);
            md.reset();
        } catch (NoSuchAlgorithmException | IOException ex) {
            System.out.println("NoSuchAlgorithmException in getResponse() while updating.");
        }
        return livehash;
    }
    
    /**
     * Get the hash of the file on drive.
     * @param md the message digest object involved in the hashing process
     * @return the file hash
     */
    private String getCurrentHash(MessageDigest md){
        publish(3); System.out.println("Hashing file on your HDD...");
        String currenthash = "Hashing error.";
        try {
            FileInputStream fis = new FileInputStream(jarFile);
            //get hash on the fly
            md = MessageDigest.getInstance(HASHING_ALGO);
            byte[] bufferForHash = new byte[1024];
            int bytesReadHarddrive = -1;
            while((bytesReadHarddrive = fis.read(bufferForHash)) != -1){
                md.update(bufferForHash, 0, bytesReadHarddrive);
            }
            fis.close();
            byte[] hashedBytes = md.digest();
            currenthash = hashToHex(hashedBytes);
            System.out.println("Hash of file on drive: " + currenthash);
        }catch (NoSuchAlgorithmException | IOException ex) {
            System.out.println("NoSuchAlgorithmException in getResponse() while updating.");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return currenthash;
    }
    
    
    /**
     * Save the downloaded file.
     * @param baos the stream to write to.
     */
    public void save(ByteArrayOutputStream baos){
        publish(4); System.out.println("Writing update to file...");
        try {
            byte[] response = baos.toByteArray();
            // write incoming data into outstream
            //create a new file to write outputstream into
            FileOutputStream fos = new FileOutputStream(jarDirectory + filename);
            //write outputstream into file
            fos.write(response);
            baos.flush();
            baos.close();
            fos.flush();
            fos.close();
            publish(5); System.out.println("Updated!");
        } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Convert hash to hexadecimal.
     * @param hashedBytes the bytes of the hash to convert
     * @return the hex string
     */
    private String hashToHex(byte[] hashedBytes){
        StringBuffer stringBuffer = new StringBuffer();
        //converting hash to hex
        for (int i = 0; i < hashedBytes.length; i++) {
            stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16)
            .substring(1));
        }
        return stringBuffer.toString();
    }
   
    
}