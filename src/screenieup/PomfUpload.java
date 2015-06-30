/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Deprecated.
 * @author Dani
 */
public class PomfUpload {
    private final String POMF_POST_URI = "https://pomf.se/upload.php";
    private final String pomfix = "http://a.pomf.se/";
    private final String tmpfilename = "upload";
    private final String tmpfilextension = ".png";
    private final String tmpfiletype = "image/png";
    private final JTextField urlarea;
    private final String[] progressText;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JDialog progressDialog;
    private final String boundary = "----";
    String pomfurl;
    private final JButton browserBtn;
    
    public PomfUpload(JTextField ua, String[] pT, JLabel lbl, JProgressBar jpb, JDialog dlg, JButton btn){
        urlarea = ua;
        progressText = pT;
        progressLabel = lbl;
        progressBar = jpb;
        progressBar.setMaximum(progressText.length);
        progressDialog = dlg;
        browserBtn = btn;
    }
    
    public void upload(BufferedImage imgToUpload){
        System.out.println("Preparing for upload...");
        final BufferedImage img = imgToUpload; // was turned into 'final' when using JDK 7 to compile
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                publish(0);
                ByteArrayInputStream bais = writeImage(img); 
                publish(2);
                HttpURLConnection connection = connect();
                publish(3);
                sendImage(bais,connection);
                publish(4);
                String response = getResponse(connection);
                publish(5);
                getImageURL(response);
                return null;
            }
            @Override
            protected void done() {
                publish(6);
                copyToClipBoard();
                publish(7);
                urlarea.setText(pomfurl);
                urlarea.setEnabled(true);
                browserBtn.setEnabled(true);
                JOptionPane.showMessageDialog(null, "Uploaded!\n" + "The image link has been copied to your clipboard!");
                progressDialog.dispose();
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                progressLabel.setText(progressText[chunks.get(chunks.size()-1)]); // The last value in this array is all we care about.
                progressBar.setValue(chunks.get(chunks.size()-1) + 1);
            }
            
        };
        
        uploader.execute();
    }
    
    public ByteArrayInputStream writeImage(BufferedImage image){
        System.out.println("Writing image...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = null;
        try {
            ImageIO.write(image, "png", baos);
            bais = new ByteArrayInputStream(baos.toByteArray());
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(PomfUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bais;
    }
    
    public HttpURLConnection connect(){
        System.out.println("Connecting to pomf...");
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(POMF_POST_URI);
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
            conn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            System.out.println("ProtocolException in connect()");
            return null;
        }
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
	conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
        return conn;
    }
    
    public void sendImage(ByteArrayInputStream bais, HttpURLConnection conn){
        System.out.println("Sending data...");
        DataOutputStream outstream;
        try {
            outstream = new DataOutputStream(conn.getOutputStream());
            outstream.writeBytes("--" + boundary + "\r\n");
            outstream.writeBytes(String.format("Content-Disposition: form-data; name=\"files[]\";filename=\"" + tmpfilename + tmpfilextension +"\"\r\nContent-type:" + tmpfiletype + "\r\n")); // make into variables
            outstream.writeBytes("\r\n");
            int i;
            while ((i = bais.read()) > -1){
                outstream.write(i);
            }
            outstream.writeBytes("\r\n");
            outstream.writeBytes("--" + boundary + "--\r\n");
            bais.close();
            outstream.flush();
            outstream.close();
        } catch (IOException ex) {
            System.out.println("IOException in sendImage()");
        }
    }
    
    public String getResponse(HttpURLConnection conn){
        System.out.println("Waiting for response...");
        String response = "No response received, or something has gone wrong.";
        try {
            BufferedReader instream = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = "";
            String line;
            while(null != (line = instream.readLine())){
                response +=  line;
            }
            instream.close();
        } catch (IOException ex) {
            System.out.println("IOException in getResponse()");
        }
        return response;
    }
    
    private void getImageURL(String response){
        System.out.println("Parsing response...");
        String filetype = "";
        int startindex = response.indexOf("url") + 6;
        int endindex = 0;
        if(response.contains(".png")){
            endindex = response.indexOf(".png\",\"size\"");
            filetype = ".png";
        }else if(response.contains(".jpg")){
            endindex = response.indexOf(".jpg\",\"size\"");
            filetype = ".jpg";
        }
        char[] url = new char[endindex - startindex];
        int index = 0;
        for(int i = startindex; i < endindex; i++){
            url[index] = response.charAt(i);
            index++;
        }
        pomfurl = pomfix + String.valueOf(url) + filetype; // make .png a variable
        System.out.println("The URL is " + pomfurl);
    }
    
    private void copyToClipBoard(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(pomfurl);
        clipboard.setContents(selection,null);
        System.out.println("Image URL copied to clipboard.");
    }
}
