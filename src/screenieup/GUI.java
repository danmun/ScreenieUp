/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;
import java.awt.image.ImagingOpException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.net.URISyntaxException;
import javax.swing.TransferHandler;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.dnd.DnDConstants;
import javax.swing.JRadioButton;
import java.util.logging.Logger;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.awt.dnd.DropTarget;
import javax.swing.JComponent;
import java.awt.AWTException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.io.IOException;
import java.awt.SystemTray;
import org.imgscalr.Scalr;
import java.awt.PopupMenu;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.TrayIcon;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Image;
import java.net.URL;
import java.io.File;


/**
 * 
 * @author Daniel Munkacsi
 */
public class GUI extends javax.swing.JFrame implements Hosts{

    private static final String[] progressText = {"Writing image...", "Encoding...", "Connecting...", "Sending data...", "Getting response...", "Parsing response...", "Copying to clipboard...", "Link copied to clipboard!"}; 
    private static final String PASTE_ERROR = "An error occured while grabbing your image.";
    private final String IMGUR_STRING = "imgur";
    private final String UGUU_STRING = "uguu";
    private final String POMF_STRING = "pomf";
    private final String MIXTAPE_STRING = "mixtape";
    private final String[] HOST_ARRAY = {IMGUR_STRING,UGUU_STRING,POMF_STRING,MIXTAPE_STRING};
    private static final String CONFIG_FILENAME = "toggleValue.txt";
    private boolean toggleIsOn;
    private int HOST;

    /**
     * Creates new form GUI
     * ISSUES:
     * The GUI has way more tasks than it should have. There should be another class which handles the file and byte array conversion,
     * or at least do that in the appropriate uploading class.
     * The gui class itself should just be that, using only object declarations to utilise other classes following actions by the users.
     * It shouldn't actually do this much data processing.
     * @param toggleValue the last used value of the instant upload feature
     */
    public GUI(int toggleValue) {
        System.out.println("Initialising Graphical User Interface...");
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //HOST = 1; // default host is pomf.se, set from main class
        toggleIsOn = false;
        if(toggleValue == 1) toggleIsOn = true;
        Image img = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/screenieup/icon.png"));
        setIconImage(img);
        initSystemTray(img);
        initComponents();
        System.out.println("Enabling drag-n-drop...");
        enableDragAndDrop();
        System.out.println("GUI initialised!");
    }

    /**
     * Put the app on the system tray.
     * @param img the icon of the app in the system tray
     */
    private void initSystemTray(Image img){
        if(!SystemTray.isSupported()){
            System.out.println("System tray is not supported !!! ");
            return ;
        }
        
        //get the systemTray of the system
        SystemTray systemTray = SystemTray.getSystemTray();
        PopupMenu trayPopupMenu = new PopupMenu();
        //1st menuitem for popupmenu
        MenuItem opener = new MenuItem("Open");
        opener.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                requestFocus();
                requestFocusInWindow();
                toFront();
                repaint();
                
            }
        });     
        
        trayPopupMenu.add(opener);
        
        //2nd menuitem of popupmenu
        MenuItem uploader = new MenuItem("Upload");
        uploader.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePaste();
            }
        });     
        
        trayPopupMenu.add(uploader);

        //3rd menuitem of popupmenu
        MenuItem closer = new MenuItem("Close");
        closer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);             
            }   
        });
        trayPopupMenu.add(closer);

        //setting tray icon
        TrayIcon trayIcon = new TrayIcon(img, "ScreenieUp", trayPopupMenu);
        //adjust to default size as per system recommendation 
        trayIcon.setImageAutoSize(true);

        try{
            systemTray.add(trayIcon);
        }catch(AWTException awtException){
            awtException.printStackTrace();
        }
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressDialog = new javax.swing.JDialog();
        progressBar = new javax.swing.JProgressBar();
        progressLabel = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        instructionsLabel = new javax.swing.JLabel();
        linkarea = new javax.swing.JTextField();
        imgurRadioBtn = new javax.swing.JRadioButton();
        uguuRadioBtn = new javax.swing.JRadioButton();
        browserBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        toggleLabel = new javax.swing.JLabel();
        tipsBtn = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        pomfRadioBtn = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();

        progressDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        progressDialog.setSize(450, 100);
        progressDialog.setTitle("Progress");
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setResizable(false);

        progressBar.setSize(400, 10);
        progressBar.setMaximumSize(new java.awt.Dimension(400, 5));
        progressBar.setMinimumSize(new java.awt.Dimension(400, 5));
        progressBar.setPreferredSize(new java.awt.Dimension(400, 35));

        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        progressLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout progressDialogLayout = new javax.swing.GroupLayout(progressDialog.getContentPane());
        progressDialog.getContentPane().setLayout(progressDialogLayout);
        progressDialogLayout.setHorizontalGroup(
            progressDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(progressDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        progressDialogLayout.setVerticalGroup(
            progressDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, progressDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ScreenieUp");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        //MyDragDropListener myDropListener = new MyDragDropListener();
        instructionsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        instructionsLabel.setText("<html><table> <tr><td align=\"center\">PASTE OR DROP YOUR IMAGE HERE</td></tr> <tr><td align=\"center\">(Ctrl + V)</td></tr> </table><html>");
        instructionsLabel.setPreferredSize(new java.awt.Dimension(970, 580));
        instructionsLabel.setTransferHandler(new TransferHandler("image"));

        linkarea.setEditable(false);
        linkarea.setColumns(30);
        linkarea.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        linkarea.setEnabled(false);
        linkarea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        buttonGroup1.add(imgurRadioBtn);
        imgurRadioBtn.setText("Imgur");
        imgurRadioBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                imgurRadioBtnFocusGained(evt);
            }
        });
        imgurRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgurRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(uguuRadioBtn);
        uguuRadioBtn.setText("Uguu");
        uguuRadioBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                uguuRadioBtnFocusGained(evt);
            }
        });
        uguuRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uguuRadioBtnActionPerformed(evt);
            }
        });

        browserBtn.setText("Open in browser");
        browserBtn.setEnabled(false);
        browserBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browserBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("Grab image on app start:");
        jLabel1.setToolTipText("Grab image in clipboard and upload as soon as you start the app. No need to paste the image.");

        if(!toggleIsOn){
            toggleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/screenieup/toggleOff.png")));
        }else{
            toggleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/screenieup/toggleOn.png")));
        }

        toggleLabel.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                ListWriter lw = new ListWriter(CONFIG_FILENAME);
                if(!toggleIsOn){
                    System.out.println("Instant uploading enabled for next app start.");
                    toggleIsOn = true;
                    toggleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/screenieup/toggleOn.png")));
                    lw.replaceInfo("insta-upload", "insta-upload " + Integer.toString(1));
                    JOptionPane.showMessageDialog(null, "You have turned on the instant upload feature.\nThe next time you start the app, the image in your clipboard will be uploaded straight away, without you having to paste it into the window.\n"
                        +"Please make sure your clipboard doesn't contain a sensitive image (e.g.: image that contains personal info) before starting the app.");
                }else if(toggleIsOn){
                    System.out.println("Instant uploading disabled.");
                    toggleIsOn = false;
                    toggleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/screenieup/toggleOff.png")));
                    lw.replaceInfo("insta-upload", "insta-upload " + Integer.toString(0));
                }
            }
        });

        tipsBtn.setText("Tips");
        tipsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipsBtnActionPerformed(evt);
            }
        });

        jButton1.setText("Check for update");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(pomfRadioBtn);
        pomfRadioBtn.setText("Pomf");
        pomfRadioBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pomfRadioBtnFocusGained(evt);
            }
        });
        pomfRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pomfRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Mixtape");
        jRadioButton1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jRadioButton1FocusGained(evt);
            }
        });
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(toggleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(browserBtn)
                .addGap(22, 22, 22)
                .addComponent(linkarea, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(imgurRadioBtn)
                .addGap(4, 4, 4)
                .addComponent(uguuRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pomfRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tipsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(instructionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toggleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tipsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(linkarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(imgurRadioBtn)
                        .addComponent(uguuRadioBtn)
                        .addComponent(jLabel1)
                        .addComponent(browserBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pomfRadioBtn)
                        .addComponent(jRadioButton1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instructionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 662, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        instructionsLabel.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent evt){
                JComponent comp = (JComponent) evt.getSource();
                TransferHandler th = comp.getTransferHandler();
                th.exportAsDrag(comp,evt,TransferHandler.COPY);
            }

        });

        // The above can be removed I think , as "enableDragndrop()" sorts it all out.

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Handle the paste event.
     * When CTRL+V is pressed, the paste action is triggered.
     * Data is taken from the user's clipboard and casted to a file or a buffered image based on the current hoster.
     * @param evt the event to handle
     */
    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if(evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_V){
            handlePaste();
        }
    }//GEN-LAST:event_formKeyReleased
    
    /**
     * Handle the event triggered by the paste action.
     * Depending on the currently set host, handle transfer of data from the clipboard into the program.
     */
    public void handlePaste(){
        System.out.println("Paste detected.");
        BufferedImage img = null;
        if(HOST == IMGUR){
            System.out.print("handling paste to imgur");
            img = (BufferedImage) getImageFromClipboard();
            if(img == null){
                JOptionPane.showMessageDialog(null, "The pasted file was not an image. You can only upload images to Imgur.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            displayThumbnail(img);
            uploadToImgur(img);
        }else if(HOST == UGUU){
            System.out.print("handling paste to uguu");
            File file = null;
            byte[] bytes = null;
            Object obj = getObjectFromClipboard();
            if(obj instanceof File){
                System.err.println("file entered");
                file = (File) obj;
                img = imageFromFile(file);
                if(img != null) displayThumbnail(img);
                uploadToUguu(file);
            }else if(obj instanceof BufferedImage){
                System.err.println("bufferedimage entered");
                img = (BufferedImage) obj;
                displayThumbnail(img);
                uploadToUguu(img);
            }
        }else if(HOST == POMF || HOST == MIXTAPE){
            System.out.print("handling paste to pomf");
            File file = null;
            byte[] bytes = null;
            Object obj = getObjectFromClipboard();            
            if(obj instanceof File){
                System.out.println("instance of file");
                file = (File) obj;
                img = imageFromFile(file);
                if(img != null) displayThumbnail(img);
                uploadToPomf(file);
            }else if(obj instanceof BufferedImage){
                img = (BufferedImage) obj;
                //bytes = bytesFromImage(img);
                displayThumbnail(img);
                uploadToPomf(img);
            }           
        }        
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
     * Display the thumbnail of the image being uploaded.
     * @param img the image to display
     */
    public void displayThumbnail(BufferedImage img){
        System.out.println("Creating and setting thumbnail...");
        try{
            Dimension size = instructionsLabel.getSize();
            BufferedImage thumbnail = Scalr.resize(img, size.width, size.height);//Scalr.resize(pastedImage, 900);
            ImageIcon icon = new ImageIcon(thumbnail);
            instructionsLabel.setText("");
            instructionsLabel.setIcon(icon);
        }catch(IllegalArgumentException | ImagingOpException e){
            instructionsLabel.setIcon(null);
            instructionsLabel.setText("Preview is not available for this file!");
        }finally{
            progressDialog.setVisible(true);
        }
    }    
    
    /**
     * Upload image to image hoster.
     * @param bim the image to upload
     */
    public void uploadToImgur(BufferedImage bim){
        ImgurUpload imgur = new ImgurUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
        imgur.upload(bim);
    }
 
     /**
     * Upload file to hoster.
     * @param b the bytes to upload
     */
    private void uploadToUguu(File f){
        UguuUpload uguu = new UguuUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
        try {
            uguu.upload(f);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Item failed to upload. Please check your internet connection and that you are uploading the right file type for the given host.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
     /**
     * Upload file to hoster.
     * @param b the bytes to upload
     */
    private void uploadToUguu(BufferedImage img){
        UguuUpload uguu = new UguuUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
        try {
            uguu.upload(img);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Item failed to upload. Please check your internet connection and that you are uploading the right file type for the given host.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
     /**
     * Upload file to hoster.
     * @param b the bytes to upload
     */
    private void uploadToPomf(File f){
        PomfUpload pomf = null;
        MixtapeUpload mixtape = null;        
        try {
            if(HOST == POMF){
                pomf = new PomfUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
                pomf.upload(f);
            }else if(HOST == MIXTAPE){
                mixtape = new MixtapeUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
                mixtape.upload(f);
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Item failed to upload. Please check your internet connection and that you are uploading the right file type for the given host.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void uploadToPomf(BufferedImage img){
        PomfUpload pomf = null;
        MixtapeUpload mixtape = null;        
        try {
            if(HOST == POMF){
                pomf = new PomfUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
                pomf.upload(img);
            }else if(HOST == MIXTAPE){
                mixtape = new MixtapeUpload(linkarea,progressText,progressLabel,progressBar,progressDialog,browserBtn);
                mixtape.upload(img);
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Item failed to upload. Please check your internet connection and that you are uploading the right file type for the given host.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void uguuRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uguuRadioBtnActionPerformed
        setHost(UGUU);
    }//GEN-LAST:event_uguuRadioBtnActionPerformed

    private void imgurRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgurRadioBtnActionPerformed
        setHost(IMGUR);
    }//GEN-LAST:event_imgurRadioBtnActionPerformed

    private void uguuRadioBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_uguuRadioBtnFocusGained
        super.requestFocusInWindow();
    }//GEN-LAST:event_uguuRadioBtnFocusGained

    private void imgurRadioBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imgurRadioBtnFocusGained
        super.requestFocusInWindow();
    }//GEN-LAST:event_imgurRadioBtnFocusGained

    private void browserBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browserBtnActionPerformed
        System.out.println("Opening image link in browser...");
        try {
            try {
                Desktop.getDesktop().browse(new URL(linkarea.getText()).toURI());
            } catch (URISyntaxException ex) {
                System.out.println("URISyntaxException when grabbing URL.");
                JOptionPane.showMessageDialog(null, "That URL doesn't seem to be valid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            System.out.println("IOException when opening browser.");
        }
        super.requestFocusInWindow();
    }//GEN-LAST:event_browserBtnActionPerformed

    private void tipsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipsBtnActionPerformed
        super.requestFocusInWindow();
        String tips = "If you're using Windows, you can use the built-in snipping tool to take a screenshot of a specific area of your screen.\n" +
                      "The picture will be copied into your clipboard, so all you have to do is open this app and paste it in, it will be uploaded to imgur \n" +
                      "and the resulting link will be copied to your clipboard! \n \n \n" + "Designed and created by Daniel Munkacsi";
        JOptionPane.showMessageDialog(null, tips);
    }//GEN-LAST:event_tipsBtnActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        System.out.println("Exiting program...");
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Updater update = new Updater(progressDialog,progressBar,progressLabel);
        update.execute();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        setVisible(false);
    }//GEN-LAST:event_formWindowIconified

    private void pomfRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pomfRadioBtnActionPerformed
        setHost(POMF);
    }//GEN-LAST:event_pomfRadioBtnActionPerformed

    private void pomfRadioBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pomfRadioBtnFocusGained
        super.requestFocusInWindow();
    }//GEN-LAST:event_pomfRadioBtnFocusGained

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        setHost(MIXTAPE);
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jRadioButton1FocusGained
        super.requestFocusInWindow();
    }//GEN-LAST:event_jRadioButton1FocusGained
    
    /**
     * Grab image from clipboard.
     * @return the image found in the clipboard
     */
    public Image getImageFromClipboard(){
        System.out.println("Getting your clipboard content...");
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)){ //DataFlavor.imageFlavor
            try{
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            }catch (UnsupportedFlavorException | IOException e){
                JOptionPane.showMessageDialog(null, PASTE_ERROR, "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }else if(transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
            try{
                java.util.List list=(java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if(list.size() > 1){JOptionPane.showMessageDialog(null,"You pasted than one image. Currently you can only upload one image at a time.", "Error", JOptionPane.ERROR_MESSAGE); System.out.println("Multiple file upload not yet supported. Process halted."); return null;}
                return ImageIO.read((File) list.get(0)); // cast the first element in the list to a file, then read it into an image
            }catch (UnsupportedFlavorException | IOException e){
                JOptionPane.showMessageDialog(null, PASTE_ERROR, "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }
    
    /**
     * Grab file from clipboard.
     * @return the image found in the clipboard
     */
    public Object getObjectFromClipboard(){
        System.out.println("Getting your clipboard content...");
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)){ //DataFlavor.imageFlavor
            try{
                return transferable.getTransferData(DataFlavor.imageFlavor);
            }catch (UnsupportedFlavorException | IOException e){
                System.out.println("Unsupported flavour exception or IOException in getImageFromClipBoard()");
            }
        }else if(transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
            try{
                java.util.List list=(java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if(list.size() > 1){JOptionPane.showMessageDialog(null,"You pasted than one image. Currently you can only upload one item at a time.", "Error", JOptionPane.ERROR_MESSAGE); System.out.println("Multiple file upload not yet supported. Process halted."); return null;}
                return list.get(0); // cast the first element in the list to a file, then read it into an image
            }catch (UnsupportedFlavorException | IOException e){
                System.out.println("Unsupported flavour exception or IOException in getImageFromClipBoard()");
            }
        }else{
            JOptionPane.showMessageDialog(null, "Couldn't find an image in your clipboard.\nMaybe it was text or a non-image file?", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return null;
    }    
    
    /**
     * Enable the drag and drop feature.
     */
    private void enableDragAndDrop(){
        DropTarget target;
        target = new DropTarget(instructionsLabel,new DropTargetListener(){
            // <editor-fold defaultstate="collapsed" desc=" unwanted implemented methods ">
            @Override
            public void dragEnter(DropTargetDragEvent e) {
            }
            
            @Override
            public void dragExit(DropTargetEvent e) {
            }
            
            @Override
            public void dragOver(DropTargetDragEvent e) {
            }
            
            @Override
            public void dropActionChanged(DropTargetDragEvent e) {
                
            }

// </editor-fold>
            public void drop(DropTargetDropEvent e){
                try
                {
                    // Accept the drop first, important!
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    System.out.println("Drag-n-drop detected.");
                    // Get the files that are dropped as java.util.List
                    java.util.List list=(java.util.List) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if(list.size() > 1){JOptionPane.showMessageDialog(null,"You dragged more than one image into the app. Currently you can only upload one image at a time.", "Error", JOptionPane.ERROR_MESSAGE); System.out.println("Multiple file drop not yet supported. Process halted."); return;}
                    // Now get the first file from the list,
                    System.out.println("Reading dropped file...");
                    File file=(File)list.get(0);
                    BufferedImage img = ImageIO.read(file); 
                    if(img == null){
                        instructionsLabel.setText("Preview not available for dragged file.");
                        instructionsLabel.setIcon(null);
                        System.out.println("File is not an image.");
                        // at this point, we could also set HOST to UGUU so the file uploads anyway, if the current host is Imgur
                    }else{
                        displayThumbnail(img);
                    }
                    System.out.println("File accepted, proceeding to upload...");
                    if(HOST == UGUU){
                        uploadToUguu(file);
                    }else if(HOST == IMGUR){
                        if(img == null){ JOptionPane.showMessageDialog(null, "File dropped was not an image.", "Error", JOptionPane.ERROR_MESSAGE); return;}
                        uploadToImgur(img);
                    }else if(HOST == POMF){
                        uploadToPomf(file);
                    }
                    progressDialog.setVisible(true);
                }catch(UnsupportedFlavorException | IOException ex){
                    System.out.println("Filetype not supported.");
                }
            }
        });
    }
    
    /**
     * Set the image hoster.
     * @param hst the host to be set
     */
    public void setHost(int hst){
        System.out.println("Setting host to " + HOST_ARRAY[hst]);
        HOST = hst;
        new ListWriter(CONFIG_FILENAME).replaceInfo("host", "host " + hst);
    }
    
    /**
     * Get access to a radio button.
     * @param host the radio button to get
     * @return the radio button
     */
    public JRadioButton getRadioBtn(int host){
        if(host == UGUU){
            return uguuRadioBtn;
        }else if(host == POMF){
            return pomfRadioBtn;
        }
        return imgurRadioBtn;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browserBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton imgurRadioBtn;
    private javax.swing.JLabel instructionsLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JTextField linkarea;
    private javax.swing.JRadioButton pomfRadioBtn;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JDialog progressDialog;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton tipsBtn;
    private javax.swing.JLabel toggleLabel;
    private javax.swing.JRadioButton uguuRadioBtn;
    // End of variables declaration//GEN-END:variables

   
}
