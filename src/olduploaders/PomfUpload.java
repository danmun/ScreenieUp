/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package olduploaders;

import screenieup.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Deprecated.
 * @author Dani
 */
public class PomfUpload {
    private final String POMF_POST_URI = "https://pomf.cat/upload.php";
    private final String pomfix = "http://a.pomf.cat/";
    private final String tmpfilename = "upload";
    private final String tmpfilextension = ".png";
    private final String tmpfiletype = "image/png";
    private String filename;
    private String extension;
    private final JButton browserBtn;
    private final JTextField urlarea;
    private final String[] progressText;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JDialog progressDialog;
    private final String boundary = "----" + System.currentTimeMillis();
    String pomfurl;

    
    public PomfUpload(JTextField ua, String[] pT, JLabel lbl, JProgressBar jpb, JDialog dlg, JButton btn){
        urlarea = ua;
        progressText = pT;
        progressLabel = lbl;
        progressBar = jpb;
        progressBar.setMaximum(progressText.length);
        progressDialog = dlg;
        browserBtn = btn;
    }
   
    /**
     * Create a BufferedImage from the pasted or dropped file.
     * @param f the file to create the image from
     * @return the resulting image
     */
    private BufferedImage imageFromFile(File f){
        Image img = null;
        try {
            img = ImageIO.read(f);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return (BufferedImage) img;
    }
    
    /**
     * Create a byte array from the dropped or pasted image.
     * @param img   the image to be converted to byte array
     * @return the resulting byte array
     */
    private byte[] bytesFromImage(BufferedImage img){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write( img, "png", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
	return null;
    }    
    
    public void upload(File f){
        System.out.println("Preparing for upload...");
        String fullname = "screenshot.png";
        if(f != null) fullname = f.getName();
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws IOException { // not handled anywhere, do fix
                publish(0);
                byte[] bytes = fileToBytes(f); 
                publish(2);
                HttpURLConnection connection = connect();
                publish(3);
                try{
                    send(bytes,connection);
                }catch(SSLHandshakeException ex){
                    JOptionPane.showMessageDialog(null, "You need to add Let's Encrypt's certificates to your Java CA Certificate store.");
                    return null;
                }
                publish(4);
                String response = getResponse(connection);
                System.out.println("response received: " + response);
                parseResponse(response);
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
    
    public void upload(BufferedImage imgToUpload){
        System.out.println("Preparing for upload...");
        final BufferedImage img = imgToUpload; // was turned into 'final' when using JDK 7 to compile
        filename = "screenshot.png";
        extension = ".png";
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws IOException {
                publish(0);
                byte[] bytes = bytesFromImage(img);
                publish(2);
                HttpURLConnection connection = connect();
                publish(3);
                send(bytes,connection);
                publish(4);
                String response = getResponse(connection);
                publish(5);
                parseResponse(response);
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
    
    public HttpURLConnection connect() throws IOException{
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(POMF_POST_URI);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PomfUpload.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            throw ex;
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            throw ex;
        }
        conn.setRequestProperty("Connection", "keep-alive"); 
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
        conn.setRequestProperty("Content-Type","multipart/form-data; boundary=----" + boundary);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");  
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        return conn;
    }
    
    public void send(byte[] b, HttpURLConnection conn) throws IOException{
        System.err.println("entered sendfile");
        String introline = "------"+boundary;
        String padder = String.format("Content-Disposition: form-data; name=\"files[]\"; filename=\"" + filename + "." + extension +"\"\r\nContent-type: " + tmpfiletype + "\r\n");
        String outroline = "------"+boundary+"--";
               
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataOutputStream outstream;
        try {
            outstream = new DataOutputStream(conn.getOutputStream());
            outstream.writeBytes(introline);
            outstream.writeBytes("\r\n");
            outstream.writeBytes(padder);
            outstream.writeBytes("\r\n");
            
            int i;
            while ((i = bais.read()) > -1){
                outstream.write(i);
                
            }
            bais.close();
            
            outstream.writeBytes("\r\n");
            outstream.writeBytes("\r\n");
            outstream.writeBytes(outroline);
            outstream.flush();
            outstream.close();
        }catch(IOException ex){
            if(ex instanceof SSLHandshakeException){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "You need to add Let's Encrypt's certificates to your Java CA Certificate store.");
            }else{
                throw ex;
            }
        }
    }
    
    public String getResponse(HttpURLConnection conn) throws IOException{
        System.out.println("Waiting for response...");
        String response = "No response received, or something has gone wrong.";
        String charset = "UTF-8";
        InputStream gzippedResponse = conn.getInputStream();
        InputStream ungzippedResponse = new GZIPInputStream(gzippedResponse);
        Reader reader = new InputStreamReader(ungzippedResponse, charset);
        StringWriter writer = new StringWriter();
        char[] buffer = new char[10240];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            writer.write(buffer, 0, length);
        }
        response = writer.toString();        
        writer.close();
        reader.close();
        reader.close();
        return response;
    }
    
    private void copyToClipBoard(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(pomfurl);
        clipboard.setContents(selection,null);
        System.out.println("Image URL copied to clipboard.");
    }
    /**
     * Convert the file to byte array.
     * This could be done in the GUI though, so the uploader can just have ONE upload method,
     * which takes a byte array, instead of having 2 methods , one of which takes a file, the other a byte array.
     * Improvements needed.
     * @param f the file to be written to a byte array
     * @return byte array containing file's data
     * @throws java.io.FileNotFoundException if file is not found while writing to bytes
     */ 
    public byte[] fileToBytes(File f) throws FileNotFoundException, IOException{
        byte[] filebytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            int read;
            byte[] buff = new byte[2048];
            while ((read = in.read(buff)) > 0){
                out.write(buff, 0, read);
            }   filebytes = out.toByteArray();
            out.flush();
        }
        return filebytes;
    }
    
    /**
     * Parse the response to get the image link.
     * @param response the image link resulting from the upload
     */
    private void parseResponse(String response){
        JSONObject outerjson = new JSONObject(response);
        JSONArray jsnarray = (JSONArray) outerjson.get("files");
        JSONObject innerjson = (JSONObject) jsnarray.get(0);
        pomfurl = pomfix + innerjson.get("url").toString();
    }
}
