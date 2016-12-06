/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup.newuploaders;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 *
 * @author Daniel Munkacsi
 */
public class Utils {

    /**
     * Copy upload link to clipboard.
     */
    public static void copyToClipBoard(String string){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(string);
        clipboard.setContents(selection,null);
        System.out.println("Image URL copied to clipboard.");
    }
}
