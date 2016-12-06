/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package olduploaders;

import screenieup.*;
import java.awt.datatransfer.StringSelection;
import org.apache.commons.io.FilenameUtils;
import java.awt.datatransfer.Clipboard;
import java.net.MalformedURLException;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.InputStreamReader;
import javax.swing.JProgressBar;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.StringWriter;
import java.io.InputStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.io.IOException;
import javax.swing.JLabel;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.List;
import java.io.Reader;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


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
    
    private final JProgressBar progressBar;
    private final JDialog progressDialog;
    private final String[] progressText;
    private final JLabel progressLabel;
    private final JTextField urlarea;
    private final JButton browserBtn;
    String uguurl;
    
    public UguuUpload(JTextField ua, String[] pT, JLabel lbl, JProgressBar jpb, JDialog dlg, JButton btn){
        urlarea = ua;
        progressText = pT;
        progressLabel = lbl;
        progressBar = jpb;
        progressBar.setMaximum(progressText.length);
        progressDialog = dlg;
        browserBtn = btn;
        
    }
    
    
    public void upload(File f){
        System.out.println("Preparing for upload...");
        String fullname = f.getName();
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
                sendFile(bytes,connection);
                publish(4);
                getResponse(connection);
                return null;
            }
            @Override
            protected void done() {
                publish(6);
                copyToClipBoard();
                publish(7);
                urlarea.setText(uguurl);
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
     * Create a byte array from the dropped or pasted image.
     * @param img   the image to be converted to byte array
     * @return the resulting byte array
     */
    private byte[] bytesFromImage(BufferedImage img){
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
    
    public void upload(BufferedImage image){
        System.out.println("Preparing for upload...");
        String fullname = "screenshot.png";
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws IOException {
                publish(0);
                publish(2);
                HttpURLConnection connection = connect();
                publish(3);
                sendFile(bytesFromImage(image),connection);
                publish(4);
                getResponse(connection);
                return null;
            }
            @Override
            protected void done() {
                publish(6);
                copyToClipBoard();
                publish(7);
                urlarea.setText(uguurl);
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
    
    public void upload(byte[] image){
        System.out.println("Preparing for upload...");
        String fullname = "screenshot.png";
        extension = FilenameUtils.getExtension(fullname);
        filename = FilenameUtils.getBaseName(fullname);
        SwingWorker uploader;
        uploader = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws IOException {
                publish(0);
                publish(2);
                HttpURLConnection connection = connect();
                publish(3);
                sendFile(image,connection);
                publish(4);
                getResponse(connection);
                return null;
            }
            @Override
            protected void done() {
                publish(6);
                copyToClipBoard();
                publish(7);
                urlarea.setText(uguurl);
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
     * Connect to Uguu.
     */
    public HttpURLConnection connect(){
        System.out.println("Connecting to pomf...");
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
    public void sendFile(byte[] b, HttpURLConnection conn){
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
     * @throws IOException during reading GZip response
     */
    public void getResponse(HttpURLConnection conn) throws IOException{
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
        uguurl = response;
    }

    /**
     * Copy upload link to clipboard.
     */
    private void copyToClipBoard(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(uguurl);
        clipboard.setContents(selection,null);
        System.out.println("Image URL copied to clipboard.");
    }
    
}