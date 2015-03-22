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

import org.saaas.server.selectionalgorithm.DBCalls;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that registers a device, whose registration id is identified by
 * {@link #PARAMETER_REG_ID}.
 *
 * <p>
 * The client app should call this servlet everytime it receives a
 * {@code com.google.android.c2dm.intent.REGISTRATION C2DM} intent without an
 * error or {@code unregistered} extra.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

    private static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_TYPE = "type";
    private static final String PARAMETER_IP = "ip";
    private static final String PARAMETER_TIME = "timestamp";

    private static final String PARAMETER_USERNAME = "user";
    Datastore datastore;
    DBCalls dbCalls;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        datastore = Datastore.getInstance();
        dbCalls = DBCalls.getInstance();
    }

    private final Logger clogger = Logger.getLogger(getClass().getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {
       /* try {
            if(datastore.getConnection().isClosed())
                datastore.setConnection();
            if(dbCalls.getConnection().isClosed())
                dbCalls.setConnection();
        } catch (SQLException ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        System.out.println("8a ginei register");
        String regId = getParameter(req, PARAMETER_REG_ID);
        String type = getParameter(req, PARAMETER_TYPE);
        String ip = getParameter(req, PARAMETER_IP);
        String timeS = getParameter(req, PARAMETER_TIME);
        String user = getParameter(req, PARAMETER_USERNAME);

        // System.out.println("egine to register" +regId);
        double lat = Double.parseDouble(getParameter(req, "latitude"));
        double lon = Double.parseDouble(getParameter(req, "longitude"));
        float local_cost = Float.parseFloat(getParameter(req, "local_cost"));
        float battery_level=Float.parseFloat(getParameter(req,"battery_level"));
        Timestamp time = Timestamp.valueOf(timeS);
        clogger.log(Level.INFO, "Registration ID: {0}, Type: {1} IP: {2} Timestamp: {3} ", new String[]{regId, type, ip, timeS});
    //pros8iki orismatos sto datastore
        //akoma kalitero na pernagame ena hashmap ws
        //orisma
        datastore.register(regId, type, ip, time, user);
        /*if (dbCalls.exist_in_db_us(regId)) {

            dbCalls.update_cost(regId, local_cost, lat, lon);
        } else {*/
            dbCalls.newUser(regId, local_cost, lat, lon,battery_level);
      //  }

        System.out.println("egine to register" + regId);
        setSuccess(resp);
    }

}
