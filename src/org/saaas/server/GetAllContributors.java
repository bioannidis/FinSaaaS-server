/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author Stark
 */
@SuppressWarnings("serial")
public class GetAllContributors extends BaseServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
      //fix ebala keni egrafi edw
     Datastore datastore;
      datastore=new Datastore();
      // dummy gcm endpoint
      datastore.register("123456789111111", "1", "0.0.0.0", Timestamp.valueOf("2013-10-10 12:12:12"),"");
      //
      
      
      // JSON all contributors. THINK: Will SAaaS server decide which contribs to expose to each app served by it?
      JSONObject json = new JSONObject();
      List<Contributor> contr = datastore.getContributors();
      for(int i = 0 ; i < contr.size() ; i++){
          json.put(i, contr.get(i).getRegId());
      }
      resp.setContentType("application/json");
      PrintWriter out = resp.getWriter();
      out.print(json);
      out.flush();
      setSuccess(resp);
  }
}
