/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gmerlino
 */
public class SetReachabilityServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
  private static final String PARAMETER_AVAIL = "reach";
  private final Logger logger = Logger.getLogger(getClass().getName());
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param req
     * @param resp
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * 
     */
          Datastore datastore ;

  @Override
	  public void init(ServletConfig config) throws ServletException {
	    super.init(config);
            datastore=Datastore.getInstance();
	  }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String reach = getParameter(req, PARAMETER_AVAIL);
        Contributor toBeSet = datastore.getContributor(regId);
        if(toBeSet != null){
            toBeSet.setReachability(Boolean.valueOf(reach));
            //must inform DB
            //bill
            datastore.setReachabilityDB(regId,true);
            //bill
            setSuccess(resp);
        }
    }
}
