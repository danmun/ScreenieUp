/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import org.apache.commons.codec.binary.Base64;
import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
import java.awt.datatransfer.Clipboard;
import java.net.MalformedURLException;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import javax.swing.JProgressBar;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.BufferedReader;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.net.URLEncoder;
import javax.swing.JLabel;
import java.awt.Toolkit;
import java.util.List;
import java.net.URL;


/**
 *
 * @author Daniel Munkacsi
 */
public class ImgurUpload {
    private final String IMGUR_POST_URI = "https://api.imgur.com/3/image.json";
    private final String IMGUR_API_KEY = "YOUR API KEY GOES HERE";
    private final JProgressBar progressBar;
    private final JDialog progressDialog;
    private final String[] progressText;
    private final JLabel progressLabel;
    private final JTextField urlarea;
    private final JButton browserBtn;
    String imgurl;
    
    public ImgurUpload(JTextField ua, String[] pT, JLabel lbl, JProgressBar jpb, JDialog dlg, JButton btn){
        urlarea = ua;
        progressText = pT;
        progressLabel = lbl;
        progressBar = jpb;
        progressBar.setMaximum(progressText.length);
        progressDialog = dlg;
        browserBtn = btn;
    }
    
    /**
     * Upload image.
     * @param imgToUpload the image to upload
     */
    public void upload(BufferedImage imgToUpload){
        System.out.println("Preparing for upload...");
        final BufferedImage img = imgToUpload; // was turned into 'final' when using JDK 7 to compile
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                publish(0);
                ByteArrayOutputStream baos = writeImage(img);
                publish(1);
                String dataToSend = encodeImage(baos);
                publish(2);
                URLConnection connection = connect();
                publish(3);
                sendImage(connection,dataToSend);
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
                urlarea.setText(imgurl);
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
    
    
    /**
     * Write image to bytes.
     * @param imgToUpload the image to be written to bytes
     * @return 
     */
    private ByteArrayOutputStream writeImage(BufferedImage imgToUpload){
        // Creates Byte Array from picture
        System.out.println("Writing image..."); 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(imgToUpload, "png", baos);
        } catch (IOException ex) {
            Logger.getLogger(ImgurUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return baos;
    }
    
    /**
     * Encode the byte array stream for upload.
     * @param bs the stream to encode
     * @return the encoded data, ready to be sent
     */
    private String encodeImage(ByteArrayOutputStream bs){
        String data = "";
        try {
            System.out.println("Encoding...");
            //encodes picture with Base64 and inserts api key
            data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.encodeBase64String(bs.toByteArray()), "UTF-8");
            data += "&" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(IMGUR_API_KEY, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ImgurUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    /**
     * Connect to image host.
     * @return the connection
     */
    private URLConnection connect(){
        URLConnection conn = null;
        try {
            System.out.println("Connecting to imgur...");
            // opens connection and sends data
            URL url = new URL(IMGUR_POST_URI);
            conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", "Client-ID " + IMGUR_API_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(ImgurUpload.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImgurUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
    /**
     * Send the data.
     * @param cn    the connection used to send the image
     * @param data  the encoded image data to send
     */
    private void sendImage(URLConnection cn, String data){
        System.out.println("Sending data...");
        try{
            OutputStreamWriter wr = new OutputStreamWriter(cn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
        }catch(IOException ex){
        
        }
    }
    
    /**
     * Get a response from the image hoster.
     * @param cn the connection to receive a response from
     * @return the response
     */
    private String getResponse(URLConnection cn){
        System.out.println("Waiting for response...");
        String response = "";
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(cn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                response = line.replaceAll("\\\\", "");
            }
            in.close();
        }catch(IOException ex){
            
        }
        return response;
    }
    
    /**
     * Parse the response to get the image link.
     * @param response the image link resulting from the upload
     */
    private void getImageURL(String response){
        System.out.println("Parsing response...");
        String filetype = "";
        int startindex = response.indexOf("http");
        int endindex = 0;
        if(response.contains(".png")){
            endindex = response.indexOf(".png");
            filetype = ".png";
        }else if(response.contains(".jpg")){
            endindex = response.indexOf(".jpg");
            filetype = ".jpg";
        }
        char[] url = new char[endindex - startindex];
        int index = 0;
        for(int i = startindex; i < endindex; i++){
            url[index] = response.charAt(i);
            index++;
        }
        imgurl = String.valueOf(url) + filetype;
        System.out.println("The URL is " + imgurl);
    }
    
    /**
     * Copy image link to user's clipboard.
     */
    private void copyToClipBoard(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(imgurl);
        clipboard.setContents(selection,null);
        System.out.println("Image URL copied to clipboard.");
    }
}