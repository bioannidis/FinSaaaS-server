/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.saaas.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that adds display number of devices and button to send a message.
 * <p>
 * This servlet is used just by the browser (i.e., not device) and contains the
 * main page of the demo app.
 */
@SuppressWarnings("serial")
public class HomeServlet extends BaseServlet {

  static final String ATTRIBUTE_STATUS = "status";

  /**
   * Displays the existing messages and offer the option to send a new one.
   */
  Datastore datastore;
 
  @Override
	  public void init(ServletConfig config) throws ServletException {
	    super.init(config);
            datastore=new Datastore();
	  }
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
     
 
    resp.setContentType("text/html");
    PrintWriter out = resp.getWriter();
    
    out.print("<html><body>");
    out.print("<head>");
    out.print("  <title>SAaaS Server</title>");
    out.print("  <link rel='icon' href='favicon.png'/>");
    out.print("</head>");
    String status = (String) req.getAttribute(ATTRIBUTE_STATUS);
    if (status != null) {
      out.print(status);
    }
    List<Registrant> devices = datastore.getDevicesFromDb();
    List<Contributor> subscriptors = datastore.getSubscriptors();//must change
    List<Contributor> contributors = datastore.getAvailableContributors();
    List<Registrant> developers = datastore.getDevelopers();//must change
    if (devices.isEmpty()) {
      out.print("<h2>No devices registered!</h2>");
    } else {
//      out.print("<h2>" + devices.size() + " device(s) registered!</h2>");
//	  out.print("<table border>");
//      for(int i=0; i<devices.size(); i++)
//	  {
//		if(i==0)
//		 {
//			 out.print("<tr>");
//			 out.print("<td>" + "Device number" + "</td>");
//			 out.print("<td>" + "Registration ID" + "</td>");
//			 out.print("</tr>");
//		  }
//		  out.print("<tr>");
//		  out.print("<td>" + (i+1) +"</td>");
//		  out.print("<td>" + devices.get(i).getRegId() +"</td>");
//		  out.print("</tr>");
//	  }
//	  out.print("</table>");
      if(subscriptors.isEmpty())
      {
    	  out.print("<h2>No devices registered as subscriptor!</h2>");
      }
      else{
//    	  out.print("<h2>" + subscriptors.size() + " device(s) registered as subscriptor!</h2>");
//		  out.print("<table border>");
//          for(int i=0; i<subscriptors.size(); i++)
//          {
//		    if(i==0)
//				{
//					out.print("<tr>");
//					out.print("<td>" + "Device number" + "</td>");
//					out.print("<td>" + "Registration ID" + "</td>");
//					out.print("</tr>");
//				}
//			out.print("<tr>");
//			out.print("<td>" + (i+1) + "</td>");
//			out.print("<td>" + subscriptors.get(i).getRegId() + "</td>");
//			out.print("</tr>");
//		  }	
//		  out.print("</table>");
      }
      
      if(contributors.isEmpty())
      {
    	  out.print("<h2>No devices registered as contributor!</h2>");
      }
	  else{
		  out.print("<h2>" + contributors.size() + " device(s) registered as contributor!</h2>");
          /*for(int i=0; i<contributors.size(); i++)
          {  //out.print("Id: " + contributors.get(i).getRegId()+ " LastReceiver: " + String.valueOf(contributors.get(i).isLastReceiver()));
        	  out.print(i +".  LastReceiver: " + String.valueOf(contributors.get(i).isLastReceiver()) + " Connection Time: " + (contributors.get(i).getTimestamp().getDay())+":"+ (contributors.get(i).getTimestamp().getHours())+":"+ (contributors.get(i).getTimestamp().getMinutes())+":"+ (contributors.get(i).getTimestamp().getSeconds()));
          }*/
		  out.print("<table border>");
		  for(int i=0; i<contributors.size(); i++)
          {
			  if(i==0)
				{
					out.print("<tr>");
					out.print("<td>" + "Device number" + "</td>");
					out.print("<td>" + "Registration ID" + "</td>");
					out.print("<td>" + "Last Receiver" + "</td>");
					out.print("<td>" + "IP Address" + "</td>");
                                        out.print("<td>" + "Reachable" + "</td>");
                                        out.print("<td>" + "Available" + "</td>");
                                        out.print("<td>" + "Booking ID" + "</td>");
                                        //bill
                                        out.print("<td>" + "Username" + "</td>");
					out.print("</tr>");
				}
			out.print("<tr>");
			out.print("<td>" + (i+1) + "</td>");
			out.print("<td>" + contributors.get(i).getRegId().substring(0, 80) + "<br>" + contributors.get(i).getRegId().substring(80) + "</td>");
			out.print("<td>" + (contributors.get(i).isLastReceiver()? 1 : 0) + "</td>");
			out.print("<td>" + contributors.get(i).getIpAddress() + "</td>");
                        out.print("<td>" + (contributors.get(i).getReachability()? 1 : 0)  + "</td>");
			out.print("<td>" + (contributors.get(i).getAvailability()? 1 : 0) + "</td>");
                        out.print("<td>" + contributors.get(i).getBookingId() + "</td>");
                        //bill
                        out.print("<td>" + contributors.get(i).getUserName() + "</td>");
			out.print("</tr>");
		  }	
		  out.print("</table>");
	  }
      if(developers.isEmpty())
      {
    	  out.print("<h2>No devices registered as developer!</h2>");
      }
      else{
//    	  out.print("<h2>" + developers.size() + " device(s) registered as developer!</h2>");
//		  out.print("<table border>");
//          for(int i=0; i<developers.size(); i++)
//          {
//			if(i==0)
//			{
//				out.print("<tr>");
//				out.print("<td>" + "Device number" + "</td>");
//				out.print("<td>" + "Registration ID" + "</td>");
//				out.print("<td>" + "IP Address" + "</td>");
//				out.print("</tr>");
//			}
//			out.print("<tr>");
//			out.print("<td>" + (i+1) +"</td>");
//			out.print("<td>" + developers.get(i).getRegId() + "</td>");
//			out.print("<td>" + developers.get(i).getIpAddress() + "</td>");
//			out.print("</tr>");
//		  }
//		  out.print("</table>");
		  
	  }
      
      out.print("<form name='form' method='POST' action='sendAll'>");
      out.print("<input type='submit' value='Send Message' />");
      out.print("</form>");
      
      out.print("<form name='form2' method='POST' action='sendAllSubscriptor'>");
      out.print("<input type='submit' value='Send Message to All Subscriptors' />");
      out.print("</form>");
      
      out.print("<form name='form3' method='POST' action='sendAllContributor'>");
      out.print("<input type='submit' value='Send Message to All Contributors' />");
      out.print("</form>");
      
      out.print("<form name='form4' method='POST' action='sendAllDeveloper'>");
      out.print("<input type='submit' value='Send Message to All Developers' />");
      out.print("</form>");
    }
    out.print("</body></html>");
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    doGet(req, resp);
  }

}
