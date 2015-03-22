/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import org.saaas.server.selectionalgorithm.DBCalls;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gmerlino
 */
public class ResetTimerServlet extends BaseServlet {
     Datastore datastore;
     DBCalls dbCalls;
  private static final String PARAMETER_REG_ID = "regId";
  @Override
	  public void init(ServletConfig config) throws ServletException {
	    super.init(config);
            datastore=Datastore.getInstance();
             dbCalls =DBCalls.getInstance();
	  }
     
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param req
     * @param resp
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String regId = getParameter(req, PARAMETER_REG_ID);
       // System.out.println("reset timer");
        //bill
       /* try {
            if(datastore.getConnection().isClosed())
                datastore.setConnection();
            if(dbCalls.getConnection().isClosed())
                dbCalls.setConnection();
        } catch (SQLException ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        
        if(regId!=null){
        float local_cost=Float.parseFloat(getParameter(req,"local_cost"));
        double lat=Double.parseDouble(getParameter(req,"latitude"));
        double lon=Double.parseDouble(getParameter(req,"longitude"));
        float battery_level=Float.parseFloat(getParameter(req,"battery_level"));
        //System.out.println("about to reset");
        dbCalls.updateCost(regId, local_cost,lat,lon,battery_level);
        //bill
        //no need i do it from hereRequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/setreachability?reach=true");
        //dispatcher.include(req, resp);
         //Contributor toBeSet = datastore.getContributor(regId);
        // Contributor toBeSetDB = datastore.getContributorFromDb(regId);
         //if(toBeSetDB==null)
          //   datastore.getContributor(regId);
         //System.out.println(tester);
        //if(toBeSetDB != null){
            
            //toBeSet.resetTimer();
            //bill
            Timestamp mytime=new Timestamp(Calendar.getInstance().getTime().getTime());
                    
            datastore.map.put(regId, mytime);
            //System.out.println(datastore.map.get(regId));
            //datastore.setAvailabilityDB(regId, true);
            //bill
            setSuccess(resp);
        /*
        else
            System.out.println("not registered user");*/
        }
    }
}
