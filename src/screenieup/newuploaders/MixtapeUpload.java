/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup.newuploaders;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import screenieup.GUI;

/**
 * @author Daniel Munkacsi
 */
public class MixtapeUpload {
    private final String MIXTAPE_POST_URI = "https://mixtape.moe/upload.php";
    private final String tmpfiletype = "image/png";
    private String filename;
    private String extension;
    private final String boundary = "----";
    String mixtapeurl;
    
    public MixtapeUpload(){
    }
    
    public void upload(File f) throws IOException{
        System.out.println("Preparing for upload...");
        String fullname = f.getName();
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        HttpURLConnection connection = connect();
        try{
            send(fileToBytes(f),connection);
        }catch(SSLHandshakeException ex){
            JOptionPane.showMessageDialog(null, "You need to add Let's Encrypt's certificates to your Java CA Certificate store.");
            return;
        }        
        String response = getResponse(connection);
        parseResponse(response); 
    }
    
    public void upload(BufferedImage imgToUpload) throws IOException{
        System.out.println("Preparing for upload...");
        String fullname = "screenshot.png";
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        HttpURLConnection connection = connect();
        send(imageToBytes(imgToUpload),connection);
        String response = getResponse(connection);
        parseResponse(response);
    }
    

    
    private HttpURLConnection connect() throws IOException{
        System.out.println("Connecting to pomf...");
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(MIXTAPE_POST_URI);
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
    
    private void send(byte[] b, HttpURLConnection conn) throws IOException{
        System.out.println("entered sendfile");
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
    
    private String getResponse(HttpURLConnection conn) throws IOException{
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

    
    /**
     * Parse the response to get the image link.
     * @param response the image link resulting from the upload
     */
    private void parseResponse(String response){
        JSONObject outerjson = new JSONObject(response);
        JSONArray jsnarray = (JSONArray) outerjson.get("files");
        JSONObject innerjson = (JSONObject) jsnarray.get(0);
        mixtapeurl = innerjson.get("url").toString();
    }
    /**
     * Create a byte array from the dropped or pasted image.
     * @param img   the image to be converted to byte array
     * @return the resulting byte array
     */
    private byte[] imageToBytes(BufferedImage img){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write( img, "jpg", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
	return null;
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
    private byte[] fileToBytes(File f) throws FileNotFoundException, IOException{
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
}
