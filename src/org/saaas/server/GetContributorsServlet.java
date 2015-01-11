package org.saaas.server;

import org.saaas.server.selectionalgorithm.AlgoChoise;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class GetContributorsServlet extends BaseServlet {

    private static final String PARAMETER_NB_CONTRIBUTORS = "nb_contributors";
    private static final String PARAMETER_budget = "budget";
    private static final String PARAMETER_evaluation_time = "evaluation_time";
    private static final String PARAMETER_Type_algo = "algorithm";
    private static final String PARAMETER_APP_NAME = "application_name";
    private static final String PARAMETER_APP_CONTENT = "application_content";
    private Sender sender;
    private Datastore datastore;
    private AlgoChoise algoChoise;
    int number_of_contributors,evaluation_time,budget;
    String appName,algorithm;
    String appContent;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        sender = newSender(config);
        datastore=Datastore.getInstance();
        algoChoise=new AlgoChoise();
          
    }

    /**
     * Creates the {@link Sender} based on the servlet settings.
     *
     * @param config
     * @return
     */
    protected Sender newSender(ServletConfig config) {
        String key = (String) config.getServletContext()
                .getAttribute(ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
        return new Sender(key);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if(datastore.getConnection().isClosed())
                datastore.setConnection();
            
        } catch (SQLException ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.info("Reception requete getcontributors");
        String nb = getParameter(req, PARAMETER_NB_CONTRIBUTORS);
        number_of_contributors = Integer.valueOf(nb);
        budget=Integer.valueOf(getParameter(req, PARAMETER_budget));
        evaluation_time=Integer.valueOf(getParameter(req, PARAMETER_evaluation_time));
        algorithm=(getParameter(req, PARAMETER_Type_algo));
        appName = getParameter(req, PARAMETER_APP_NAME);
        logger.log(Level.INFO, "number: {0} appName : {1}", new Object[]{number_of_contributors, appName});
        appContent = getParameter(req, PARAMETER_APP_CONTENT);
        logger.log(Level.INFO, "number: {0} appName : {1}", new Object[]{number_of_contributors, appName});

////	    List<Contributor> avail = datastore.getContributors();
        //edw 8a adikatastisw to available Contributors me klisi stin dikia m klasi
        List<Contributor> avail = datastore.getAvailableContributors();
        List<Contributor> selected = new ArrayList<Contributor>();
        JSONObject json = new JSONObject();
//	    boolean start = false;
//	    int count = 0;
//		for(int i= 0 ; i<contributors.size();i++)
//		{
//			if(start)
//			{
//				if(count != number)
//				{
//					boolean send = sendMessage(contributors.get(i), count);
//					if(send == true)
//					{		
//						count++;
//						selectedId.add(contributors.get(i).getRegId());
//						if(count==number)
//						{
//							contributors.get(i).setLastReceiver(true);
//						}
//					}
//				}
//				
//			}
//			if(contributors.get(i).isLastReceiver())
//			{
//				start=true;
//				contributors.get(i).setLastReceiver(false);
//			}
//		}
//		if(count!=number)
//		{
//			for(int i=0 ; i<contributors.size();i++)
//			{
//				
//				if(count != number)
//				{
//					boolean send = sendMessage(contributors.get(i), count);
//					if(send == true)
//					{		
//						count++;
//						selectedId.add(contributors.get(i).getRegId());
//						if(count==number)
//						{
//							contributors.get(i).setLastReceiver(true);
//						}
//					}
//				}
//			}
//		}

        //if (number <= avail.size()) { no need for this
       /* int count = 0;
        Iterator<Contributor> con_it = avail.iterator();
        while (count < number_of_contributors && con_it.hasNext()) {
            Contributor current = con_it.next();
////                    boolean status = sendMessage(current);
////                    if(status){
            if (current.getReachability()) ////
            {                                                   ////
////                        current.setReachability(true);
////                        current.resetTimer();
                current.setAvailability(false);
                selected.add(current);
                json.put(count, current.getRegId());
                count++;
////                    } else {
////                        current.setReachability(false);
////                        current.unsetTimer();
////                    }
            }                                                   ////
        }
        
        datastore.book(uuid.toString(), selected, appName);
        //  Datastore.getBooking(uuid.toString()).setTimer();

        
        Iterator<Contributor> select_it = selected.iterator();
        while (select_it.hasNext()) {
            Contributor curr = select_it.next();
            selected_strings.add(curr.getRegId());
        }*/
        Long uuid = System.currentTimeMillis();
        List<String> selected_strings = new ArrayList<String>();
        //bill algo select  
        int value_of_task = 10;
        if(algorithm.equals("online"))
        selected_strings =algoChoise.select_winner_to_deploy_online(number_of_contributors,evaluation_time,budget,true);
        System.out.println("epilegmenoi xristes" + selected_strings.toString());
        //bill
        datastore.sendToDbBook(uuid.toString(), selected_strings, appName);
        sendAPK(selected_strings);

        json.put("booking ID", uuid.toString());
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
        setSuccess(resp);
        //}
    }

    private void sendAPK(List<String> selectedId) {
        Message message;
        message = new Message.Builder().addData("title", appName).build();
        try {
            MulticastResult multiResult = sender.send(message, selectedId, 10);
            List<Result> results = multiResult.getResults();
            for (int j = 0; j < results.size(); j++) {
                Result result = results.get(j);

                if (result.getMessageId() != null) {
                    // Success
                    String canonicalRegId = result.getCanonicalRegistrationId();
                    if (canonicalRegId != null) {
                        // same device has more than one registration id.Update it
                    }
                } else {
                    // Error occurred
                    String error = result.getErrorCodeName();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.info("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*  
     ///	private void sendAPK(List<Contributor> pselection)
     private void sendAPK(List<String> pselection)
     {
     Message message;
     byte[] bytes = appContent.getBytes();
     int partnumber = 0;
     int totalpart = (bytes.length)/3000;
     int start = 0;
     int end = 0;
     int i =0;
     List<String> selectedId = new ArrayList<String>();
     ///                Iterator<Contributor> contr_it = pselection.iterator();
     ///                while(contr_it.hasNext())
     ///                {
     ///                    Contributor current = contr_it.next();
     ///                    selectedId.add(current.getRegId());
     ///                }
     Iterator<String> contr_it = pselection.iterator();
     while(contr_it.hasNext())
     {
     String current = contr_it.next();
     selectedId.add(current);
     }
     List<String> list = new ArrayList<String>();
     int index = 0;
     int total = 0;
     while (index<appContent.length()) {
     list.add(appContent.substring(index, Math.min(index+3000,appContent.length())));
     index=index+3000;
     total++;
     }
     while(i<total)
     {
     try{
     Thread.sleep(2000);
     }
     catch(InterruptedException ie){
     //If this thread was interrupted by another thread 
     }
     end = start + 3000;
     byte[] temp = Arrays.copyOfRange(bytes, start , end);
     logger.log(Level.INFO, "start : {0} end : {1} i : {2} partnumber : {3}", new Object[]{String.valueOf(start), String.valueOf(end-1), String.valueOf(i), String.valueOf(partnumber)});
     if(i==0)
     {
     message = new Message.Builder().addData("title", appName).addData("totalpart", String.valueOf(totalpart)).addData("partnumber", String.valueOf(partnumber)).addData("apk", list.get(i)).build();
     logger.log(Level.INFO, "Part number : {0}", partnumber);
     try {
     MulticastResult multiResult = sender.send(message, selectedId, 10);
     List<Result> results = multiResult.getResults();
     for (int j = 0; j < results.size(); j++) {
     Result result = results.get(j);

     if (result.getMessageId() != null) {
     // Success
     String canonicalRegId = result.getCanonicalRegistrationId();
     if (canonicalRegId != null) { 
     // same device has more than one registration id.Update it
     }
     } else {
     // Error occurred
     String error = result.getErrorCodeName();
     }
     }
     } catch (IOException e) {
     // TODO Auto-generated catch block
     logger.log(Level.INFO, "Error : {0}", e.getMessage()); 
     }
     }
     else
     {
     if(i==total-1)
     {
     message = new Message.Builder().addData("end", "END").addData("partnumber", String.valueOf(partnumber)).addData("apk", list.get(i)).build();
     logger.log(Level.INFO, "Part number : {0}", partnumber);
     try {
     MulticastResult multiResult = sender.send(message, selectedId, 10);
     List<Result> results = multiResult.getResults();
     for (int j = 0; j < results.size(); j++) {
     Result result = results.get(j);

     if (result.getMessageId() != null) {
     // Success
     String canonicalRegId = result.getCanonicalRegistrationId();
     if (canonicalRegId != null) { 
     // same device has more than one registration id.Update it
     }
     } else {
     // Error occurred
     String error = result.getErrorCodeName();
     }
     }
     } catch (IOException e) {
     // TODO Auto-generated catch block
     logger.log(Level.INFO, "Error : {0}", e.getMessage()); 
     }
     }
     else
     {
     message = new Message.Builder().addData("partnumber", String.valueOf(partnumber)).addData("apk", list.get(i)).build();
     logger.log(Level.INFO, "Part number : {0}", partnumber);
     try {
     MulticastResult multiResult = sender.send(message, selectedId, 10);
     List<Result> results = multiResult.getResults();
     for (int j = 0; j < results.size(); j++) {
     Result result = results.get(j);

     if (result.getMessageId() != null) {
     // Success
     String canonicalRegId = result.getCanonicalRegistrationId();
     if (canonicalRegId != null) { 
     // same device has more than one registration id.Update it
     }
     } else {
     // Error occurred
     String error = result.getErrorCodeName();
     }
     }
     } catch (IOException e) {
     // TODO Auto-generated catch block
     logger.log(Level.INFO, "Error : {0}", e.getMessage()); 
     }
     }
     }
     i++;
     partnumber++;
     start = end;
     }
     }*/
//    private boolean sendMessage(Contributor contributor) {
//        // TODO Auto-generated method stub
//        String registrationId = contributor.getRegId();
//        logger.info("Contributor selected");
//
//        /*
//         if(count == 0)
//         message = new Message.Builder().addData("start", String.valueOf(number)).addData("IP", contributor.getIpAddress()).build();
//         else
//         {
//         message = new Message.Builder().addData("IP", contributor.getIpAddress()).build();
//         }*/
//        return send(registrationId);
//    }
    private boolean send(String registrationId) {
        Message message;
        message = new Message.Builder().build();
        try {
            Result result = sender.send(message, registrationId, 10);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.log(Level.INFO, "Error : {0}", e.getMessage());
            return false;
        }
    }
}
