/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gmerlino
 */
public class ResetTimerServlet extends BaseServlet {

  private static final String PARAMETER_REG_ID = "regId";
    
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
        if(regId!=null){
        float local_cost=Float.parseFloat(getParameter(req,"local_cost"));
        double lat=Double.parseDouble(getParameter(req,"latitude"));
        double lon=Double.parseDouble(getParameter(req,"longitude"));

        DBCalls.update_cost(regId, local_cost,lat,lon);
        //bill
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/setreachability?reach=true");
        dispatcher.include(req, resp);
         //Contributor toBeSet = Datastore.getContributor(regId);
         Contributor toBeSetDB = Datastore.getContributorFromDb(regId);
        if(toBeSetDB != null){
           // System.out.println("bikas");
            //toBeSet.resetTimer();
            //bill
            Timestamp mytime=new Timestamp(Calendar.getInstance().getTime().getTime());
                    
            Datastore.map.put(regId, mytime);
            Datastore.setAvailabilityDB(regId, true);
            //bill
            setSuccess(resp);
        }
        }
    }
}
