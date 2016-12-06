/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenieup.newuploaders;

import java.net.HttpURLConnection;

/**
 *
 * @author Daniel Munkacsi
 */
public interface UploadOps {
    public HttpURLConnection connect();
    public void send();
    public void receive();
}
