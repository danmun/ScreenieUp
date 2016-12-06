/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup.newuploaders;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import org.apache.commons.io.FilenameUtils;
import java.net.MalformedURLException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import screenieup.GUI;


/**
 *
 * @author Daniel Munkacsi
 */
public class UguuUpload {
    private final String UGUU_URI = "https://uguu.se/api.php?d=upload-tool";
    private String filename;
    private String extension;
    private final String tmpfiletype = "file/"; //this always works, regardless of file type / extension
    private final String boundary = "---------------------------" + System.currentTimeMillis();
    String uguurl;
    
    public UguuUpload(){
    }
    
    public void upload(File f) throws IOException{
        System.out.println("Preparing for upload...");
        String fullname = f.getName();
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        HttpURLConnection connection = connect();
        send(fileToBytes(f),connection);
        String link = getResponse(connection);
    }
    
    public void upload(BufferedImage screenshot) throws IOException{
        System.out.println("Preparing for upload...");
        String fullname = "screenshot.png";
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        HttpURLConnection connection = connect();
        send(imageToBytes(screenshot),connection); 
        String link = getResponse(connection);
    }
    
    /**
     * Connect to Uguu.
     */
    private HttpURLConnection connect(){
        System.out.println("Connecting to uguu...");
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(UGUU_URI);
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
            ex.printStackTrace();
            return null;
        }
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
	
        return conn;
    }
    
    /**
     * Send the file to Uguu.
     * @param b the contents of the file in a byte array
     * @param conn the connection to use
     */
    private void send(byte[] b, HttpURLConnection conn){
        String first = String.format("Content-Type: multipart/form-data; boundary=" + boundary +"\"\r\nContent-Length: 30721\r\n");
        String second = String.format("Content-Disposition: form-data; name=\"MAX_FILE_SIZE\"\r\n\r\n" + "100000000\r\n");
        String data = String.format("Content-Disposition: form-data; name=\"file\";filename=\"" + filename + "." + extension +"\"\r\nContent-type:" + tmpfiletype + "\r\n");
        String last = String.format("Content-Disposition: form-data; name=\"name\"");
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        System.out.println("Sending data...");
        DataOutputStream outstream;
        try {
            outstream = new DataOutputStream(conn.getOutputStream());
            outstream.writeBytes(first);
            outstream.writeBytes("\r\n");
            outstream.writeBytes("\r\n");
            
            outstream.writeBytes("--" + boundary);
            outstream.writeBytes(second);
            outstream.writeBytes("--" + boundary + "\r\n");
            
            outstream.writeBytes(data);
            outstream.writeBytes("\r\n");
            
            int i;
            while ((i = bais.read()) > -1){
                outstream.write(i);
            }
            bais.close();
            outstream.writeBytes("\r\n");
            outstream.writeBytes("--" + boundary + "\r\n");
            outstream.writeBytes(last);
            outstream.writeBytes("\r\n");
            outstream.writeBytes("\r\n");
            outstream.writeBytes("\r\n");
            outstream.writeBytes( "--" + boundary + "--");
            outstream.flush();
            outstream.close();
        } catch (IOException ex) {
            System.out.println("IOException in sendImage()");
        }
    }
    /**
     * Get a response from Uguu.
     * @param conn the connection to use to listen to response.
     * @return 
     * @throws IOException during reading GZip response
     */
    private String getResponse(HttpURLConnection conn) throws IOException{
        System.out.println("Waiting for response...");
        String charset = "UTF-8"; // You should determine it based on response header.
        InputStream gzippedResponse = conn.getInputStream();
        InputStream ungzippedResponse = new GZIPInputStream(gzippedResponse);
        Reader reader = new InputStreamReader(ungzippedResponse, charset);
        StringWriter writer = new StringWriter();
        char[] buffer = new char[10240];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            writer.write(buffer, 0, length);
        }
        String response = writer.toString();        
        writer.close();
        reader.close();
        return response;
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