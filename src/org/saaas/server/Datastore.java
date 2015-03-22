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

import org.saaas.server.dataobjects.Booking;
import org.saaas.server.dataobjects.Contributor;
import org.saaas.server.dataobjects.Registrant;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of a data store using standard Java collections.
 * <p>
 * This class is thread-safe but not persistent (it will lost the data when the
 * app is restarted) - it is meant just as an example.
 */
public final class Datastore {

    private static final List<Registrant> devices = new ArrayList<Registrant>();
    private static final List<Contributor> contributors = new ArrayList<Contributor>();
    private static final List<Contributor> subscriptors = new ArrayList<Contributor>();
    private static final List<Registrant> developers = new ArrayList<Registrant>();
    private static final List<Booking> bookings = new ArrayList<Booking>();
    private static final Logger logger
            = Logger.getLogger(Datastore.class.getName());
    private static final long delay_between_updates = 20000;
    static String connectionURL = "jdbc:mysql://localhost:3306/saas";
    public static Map<String, Timestamp> map;
    private static Connection connection = null;
    int rs = 0;
    private static Datastore ds = new Datastore();
    private static final Map<String, Contributor> activeContributors = new HashMap<String, Contributor>();

    public static Datastore getInstance() {
        return ds;
    }
    /*
     private Datastore() {
     throw new UnsupportedOperationException();
     }*/

    private Datastore() {

        try {
            PreparedStatement statement = null;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
           // connection = DriverManager.getConnection(connectionURL, "root", "");
            map = new HashMap<String, Timestamp>();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void finalize() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection() {
        try {
            PreparedStatement statement = null;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(connectionURL, "root", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    /*
     finds availability through last available
     time updated may need fixing
     */

    private static boolean get_state(String regId) {
        boolean avail = false;
        if (map.get(regId) == null) {
            //prosoxi edw fix
            // SOS FIX THIS
            //System.out.println("bike edw gg");
            setReachabilityDB(regId, false);
            setAvailabilityDB(regId, false);

            return false;
        }
        long start = map.get(regId).getTime();
        Date date = new Date();
        long now = date.getTime();
        if (start + delay_between_updates > now) {
            avail = true;
            //System.out.println("available user");
        } else {
            System.out.println("bika edw : start : now " + start + "  " + now);
            setReachabilityDB(regId, false);
            setAvailabilityDB(regId, false);
        }

        return (avail);

    }

    /**
     * Registers a booking.
     *
     * @param bId
     * @param bResources
     * @param appName
     */
    private static void setbooking(String bid, String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update Contributors set bookingId=? where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, regId);
            statement.setString(1, bid);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            // System.out.println("updatebookingdb "+ statement.getResultSet().toString());
            //System.out.println("egine to updateRegDbcon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void sendToDbBook(String bId, List<String> RegIDs, String appName) {
        PreparedStatement statement = null;
        //Connection connection = null;
        try {
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "insert into bookings (bId,appName) values(?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, bId);
            statement.setString(2, appName);

            statement.executeUpdate();

            Iterator<String> contr_it = RegIDs.iterator();
            while (contr_it.hasNext()) {
                String current = contr_it.next();
                setbooking(bId, current);
            }

            //     System.out.println("sendToDbBook "+ statement.getResultSet().toString());
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void book(String bId, List<Contributor> bResources, String appName) {
        logger.log(Level.INFO, "Store booking request with booking ID: {0}", bId);
        //sendToDbBook(bId,bResources,appName);
        synchronized (bookings) {
            Booking bk = new Booking();
            bk.setBookingId(bId);
            bk.setResources(bResources);
            logger.log(Level.INFO, "APPNAME during booking: {0}", appName);
            bk.setAppName(appName);
            Iterator<Contributor> contr_it = bResources.iterator();
            while (contr_it.hasNext()) {
                Contributor current = contr_it.next();
                current.setBookingId(bId);
            }
            bookings.add(bk);
        }
    }

    public static boolean exist_in_db_Con(String regId) {
        boolean found = false;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;

            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");
            System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            found = rs.next();
            System.out.println("search in Db ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            if (connection != null) {
            }
        }
        return (found);

    }

    public static boolean exist_in_db_Sub(String regId) {
        boolean found = false;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;

            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");
            System.out.println("sinde8ika me basi");
            String sqle = "select * from Subscriptors where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            found = rs.next();
            System.out.println("search in Db ");
            // bn System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            if (connection != null) {
            }
        }
        return (found);

    }

    public static boolean exist_in_db_Devi(String regId) {
        boolean found = false;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;

            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");
            System.out.println("sinde8ika me basi");
            String sqle = "select * from devices where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            found = rs.next();
            System.out.println("search in Db ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            if (connection != null) {
            }
        }
        return (found);

    }

    public static boolean exist_in_db_Deve(String regId) {
        boolean found = false;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Developers where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            found = rs.next();
            System.out.println("search in Db ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            if (connection != null) {
            }
        }
        return (found);

    }

    private static void deleteFromDbCon(String regId) {

        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            String sqle = "delete from Contributors where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("deleteFromDbCon ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    private static void deleteFromDbSub(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "delete from Subscriptors where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("deleteFromDbSub ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    private static void deleteFromDbDevi(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "delete from Devices where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("deleteFromDbDevi ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    private static void deleteFromDbDeve(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "delete from Developers where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("deleteFromDbDeve ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    private static void sendToDbCon(Contributor contributor) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "insert into Contributors (regId,user,addressIp"
                    + ",availability,lastReceiver,reachability,bookingId,"
                    + "timestamp,ttl) values(?,?,?,?,?,?,?,?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, contributor.getRegId());
            statement.setString(2, contributor.getUserName());
            statement.setString(3, contributor.getIpAddress());
            statement.setBoolean(4, contributor.getAvailability());
            statement.setBoolean(5, contributor.isLastReceiver());
            statement.setBoolean(6, contributor.getReachability());
            statement.setString(7, contributor.getBookingId());
            statement.setString(8, contributor.getTimestamp().toString());
            statement.setInt(9, contributor.getTTL());
            statement.executeUpdate();

            System.out.println("sendToDbCon ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    private static void sendToDbSub(Contributor contributor) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "insert into Subscriptors (regId,user,addressIp"
                    + ",availability,lastReceiver,reachability,bookingId,"
                    + "timestamp,ttl) values(?,?,?,?,?,?,?,?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, contributor.getRegId());
            statement.setString(2, contributor.getUserName());
            statement.setString(3, contributor.getIpAddress());
            statement.setBoolean(4, contributor.getAvailability());
            statement.setBoolean(5, contributor.isLastReceiver());
            statement.setBoolean(6, contributor.getReachability());
            statement.setString(7, contributor.getBookingId());
            statement.setString(8, contributor.getTimestamp().toString());
            statement.setInt(9, contributor.getTTL());
            statement.executeUpdate();

            System.out.println("sendToDbSub ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    private static void sendToDbDevi(Registrant device) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "insert into Devices (regId,user,addressIp"
                    + ",availability,reachability,"
                    + "ttl) values(?,?,?,?,?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, device.getRegId());
            statement.setString(2, device.getUserName());
            statement.setString(3, device.getIpAddress());
            statement.setBoolean(4, device.getAvailability());
            statement.setBoolean(5, device.getReachability());
            statement.setInt(6, device.getTTL());
            statement.executeUpdate();

            System.out.println("sendToDbDev ");
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    private static void sendToDbDeve(Registrant developer) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "insert into Developers (regId,user,addressIp"
                    + ",availability,reachability,"
                    + ",ttl) values(?,?,?,?,?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, developer.getRegId());
            statement.setString(2, developer.getUserName());
            statement.setString(3, developer.getIpAddress());
            statement.setBoolean(4, developer.getAvailability());
            statement.setBoolean(5, developer.getReachability());
            statement.setInt(6, developer.getTTL());
            statement.executeUpdate();

            System.out.println("sendToDbDeve" + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
    }

    /**
     * Registers a device.
     *
     * @param regId
     * @param deviceType
     * @param addressIp
     * @param time
     */
    public static void register(String regId, String deviceType, String addressIp, Timestamp time, String user) {
        logger.log(Level.INFO, "Registering {0}", regId);
        synchronized (devices) {
            Registrant device = new Registrant();
            //bill

            Timestamp mytime = time;
            map.put(regId, mytime);
            System.out.println("size of map " + map.size());
            device.setUserName(user);
            device.setRegId(regId);
            device.setIpAddress(addressIp);
            device.setReachability(true);
            device.setAvailability(true);
            devices.add(device);
         //   if (exist_in_db_Devi(device.getRegId())) {
           //     deleteFromDbDevi(device.getRegId());
            //}
            //sendToDbDevi(device);
            if (deviceType.contentEquals("1")) {
                Contributor contributor = new Contributor(device);
                contributor.setLastReceiver(false);
                contributor.setTimestamp(time);
                contributor.setTimer(60);
                activeContributors.put(regId,contributor);
                boolean find = false;
                for (int i = 0; i < contributors.size(); i++) {
                    if (contributors.get(i).getRegId().equals(regId)) {
                        find = true;
                        contributors.remove(contributors.get(i));
                        contributors.add(contributor);

                    }
                }
              //  if (exist_in_db_Con(contributor.getRegId())) {
               //     deleteFromDbCon(contributor.getRegId());
                //    sendToDbCon(contributor);
               // } else {
                 //   sendToDbCon(contributor);
             //   }

                if (!find) {
                    contributors.add(contributor);
                    //an ftasei edw den iparxei stin basi tou contributor
              //      if (exist_in_db_Con(contributor.getRegId())) {
                //        deleteFromDbCon(contributor.getRegId());
                  //      sendToDbCon(contributor);
                  //  }
                    logger.log(Level.INFO, "IP {0}", contributor.getIpAddress());
                    logger.log(Level.INFO, "TTL {0}", contributor.getTTL());
                    boolean find2 = false;
                    for (int i = 0; i < subscriptors.size(); i++) {
                        if (subscriptors.get(i).getRegId().equals(regId)) {
                            find2 = true;
                            subscriptors.remove(subscriptors.get(i));
                            subscriptors.add(contributor);

                        }
                    }
               //     if (exist_in_db_Sub(contributor.getRegId())) {

                   //     deleteFromDbSub(contributor.getRegId());
                     //   sendToDbSub(contributor);
                 //   }
                    if (!find2) {
                        subscriptors.add(contributor);
                    }
                    //if (exist_in_db_Sub(contributor.getRegId())) {

                      //  deleteFromDbSub(contributor.getRegId());
                     //   sendToDbSub(contributor);
                    //}
                }

            }
            if (deviceType.contentEquals("2")) {
                //fix maybe username if needed
                Registrant developer = new Registrant();
                developer.setRegId(regId);
                developer.setIpAddress(addressIp);
                boolean find = false;
                for (int i = 0; i < developers.size(); i++) {
                    if (developers.get(i).getRegId().equals(regId)) {
                        find = true;
                        developers.remove(developers.get(i));
                        developers.add(developer);

                    }
                }
                if (exist_in_db_Deve(developer.getRegId())) {

                    deleteFromDbDeve(developer.getRegId());
                    sendToDbDeve(developer);

                }
                if (!find)//must implement this with database search
                {
                    developers.add(developer);
                }
                if (exist_in_db_Deve(developer.getRegId())) {

                    deleteFromDbDeve(developer.getRegId());
                    sendToDbDeve(developer);

                }
            }
        }
    }

    /**
     * Unregisters a device.
     *
     * @param regId
     */
    public static void unregister(String regId) {
        logger.log(Level.INFO, "Unregistering {0}", regId);
        synchronized (devices) {
            Iterator<Registrant> dev_it = devices.iterator();
            while (dev_it.hasNext()) {
                Registrant current = dev_it.next();
                if (current.getRegId().equals(regId)) {
                    dev_it.remove();
                }
            }
            deleteFromDbDevi(regId);
        }
        synchronized (contributors) {
            deleteFromDbCon(regId);
            Iterator<Contributor> con_it = contributors.iterator();
            while (con_it.hasNext()) {
                Contributor current = con_it.next();
                if (current.getRegId().equals(regId)) {
                    con_it.remove();
                }
            }
        }
        synchronized (subscriptors) {
            deleteFromDbSub(regId);
            Iterator<Contributor> sub_it = subscriptors.iterator();
            while (sub_it.hasNext()) {
                Contributor current = sub_it.next();
                if (current.getRegId().equals(regId)) {
                    sub_it.remove();
                }
            }
        }
        synchronized (developers) {
            deleteFromDbDeve(regId);
            Iterator<Registrant> dvl_it = developers.iterator();
            while (dvl_it.hasNext()) {
                Registrant current = dvl_it.next();
                if (current.getRegId().equals(regId)) {
                    dvl_it.remove();
                }
            }
        }
    }

    public static Registrant getDeviceFromDb(String regId) {
        Registrant current = null;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;

            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");
            System.out.println("sinde8ika me basi");
            String sqle = "select * from Devices where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                current = (new Registrant());
                current.setAvailability(get_state(regId));
                current.setRegId(regId);
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
            }
            System.out.println("getDeviceFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return current;
    }

    public static Registrant getDevice(String regId) {

        logger.log(Level.INFO, "Getting handle of device with reg. ID: {0}", regId);

        Registrant current = null;
        synchronized (devices) {
            Iterator<Registrant> dev_it = devices.iterator();
            while (dev_it.hasNext()) {
                current = dev_it.next();
                if (current.getRegId().equals(regId)) {
                    break;
                }
            }
        }
        return current;
    }

    public static Contributor getContributorFromDb(String regId) {
        Contributor current = null;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors where regId = ?; ";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(regId);
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
            }
            //System.out.println("getContributorFromDb "+ statement.getResultSet().toString());
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return current;
    }

    public static Contributor getContributor(String regId) {
        //logger.log(Level.INFO, "Getting handle of contributor with reg. ID: {0}", regId);
        Contributor avail=activeContributors.get(regId);
/*
        synchronized (contributors) {
            Iterator<Contributor> con_it = contributors.iterator();
            while (con_it.hasNext()) {
                current = con_it.next();
                if (current.getRegId().equals(regId)) {
                    avail = current;
                    break;
                }
            }
        }
*/
        return avail;
    }

    /**
     *
     * @param bookingId
     * @return
     */
    public static Booking getBookingFromDb(String bookingId) {
        Booking book = null;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from bookings where bId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, bookingId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                book = new Booking();
                book.setBookingId(bookingId);
                book.setAppName(rs.getString(2));
                book.setResources(getAllSubscriptorsFromDbBookingId(bookingId));
            }
            System.out.println("getContributorFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return book;
    }

    public static List<Contributor> getAllSubscriptorsFromDbBookingId(String bookingId) {
        List<Contributor> allSubscriptors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Subscriptors where bookingId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, bookingId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(bookingId);
                rs.previous();

                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                allSubscriptors.add(current);
            }

            System.out.println("getallContributorsFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (allSubscriptors);
    }

    public static Booking getBooking(String bookingId) {
        logger.log(Level.INFO, "Getting handle of booking with booking ID: {0}", bookingId);
        Booking current = null;
        synchronized (bookings) {
            Iterator<Booking> book_it = bookings.iterator();
            while (book_it.hasNext()) {
                current = book_it.next();
                if (current.getBookingId().equals(bookingId)) {
                    break;
                }
            }
        }
        return current;
    }

//  /**
//   * Sets availability of a device.
//     * @param regId
//     * @param available
//   */
//  public static void setAvailability(String regId, boolean available) {
//    logger.log(Level.INFO, "Setting availability of {0}", regId);
//    synchronized (devices) {
//        
//        Iterator<Registrant> dev_it = devices.iterator();
//        while(dev_it.hasNext())
//        {
//            Registrant current = dev_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.setReachability(available);
//            }
//        }
//
//        Iterator<Contributor> con_it = contributors.iterator();
//        while(con_it.hasNext())
//        {
//            Contributor current = con_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.setReachability(available);
//            }
//        }
//        
// 
//        Iterator<Contributor> sub_it = subscriptors.iterator();
//        while(sub_it.hasNext())
//        {
//            Contributor current = sub_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.setReachability(available);
//            }
//        }
//        
//        Iterator<Registrant> dvl_it = developers.iterator();
//        while(dvl_it.hasNext())
//        {
//            Registrant current = dvl_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.setReachability(available);
//            }
//        }
//    }
//  }
//    /**
//   * Resets availability timeout counter of a device.
//     * @param regId
//   */
//  public static void resetTimer(String regId) {
//    logger.log(Level.INFO, "Resetting availability timeout counter of {0}", regId);
//    synchronized (devices) {
//        
//        Iterator<Registrant> dev_it = devices.iterator();
//        while(dev_it.hasNext())
//        {
//            Registrant current = dev_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.resetTimer();
//            }
//        }
//
//        Iterator<Contributor> con_it = contributors.iterator();
//        while(con_it.hasNext())
//        {
//            Contributor current = con_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.resetTimer();
//            }
//        }
//        
// 
//        Iterator<Contributor> sub_it = subscriptors.iterator();
//        while(sub_it.hasNext())
//        {
//            Contributor current = sub_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.resetTimer();
//            }
//        }
//        
//        Iterator<Registrant> dvl_it = developers.iterator();
//        while(dvl_it.hasNext())
//        {
//            Registrant current = dvl_it.next();
//            if(current.getRegId().equals(regId))
//            {
//                current.resetTimer();
//            }
//        }
//    }
//  }
    /**
     * Updates the registration id of a device.
     *
     * @param oldId
     * @param newId
     */
    public static void updateRegDbCon(String oldId, String newId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "update Contributors set regId=? where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, oldId);
            statement.setString(1, newId);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            System.out.println("updateRegDbcon " + statement.getResultSet().toString());
            System.out.println("egine to updateRegDbcon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void updateRegDbSub(String oldId, String newId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "update Subscriptors set regId=? where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, oldId);
            statement.setString(1, newId);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            System.out.println("updateRegDbsub " + statement.getResultSet().toString());
            System.out.println("egine to updateRegDbsub");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void updateRegDbDeve(String oldId, String newId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "update Developers set regId=? where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, oldId);
            statement.setString(1, newId);
            statement.executeUpdate();

            //fi current.setTimestamp(rs.getString(8));
            System.out.println("updateRegDbdeve " + statement.getResultSet().toString());
            System.out.println("egine to updateRegDbdeve ");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void updateRegDbDevi(String oldId, String newId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "update Devices set regId=? where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, oldId);
            statement.setString(1, newId);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            System.out.println("updateRegDbdevi " + statement.getResultSet().toString());
            System.out.println("egine to updateRegDbdevl");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    public static void updateRegistration(String oldId, String newId) {

        logger.log(Level.INFO, "Updating {0} to {1}", new Object[]{oldId, newId});
        synchronized (devices) {
            Iterator<Registrant> dev_it = devices.iterator();
            while (dev_it.hasNext()) {
                Registrant current = dev_it.next();
                if (current.getRegId().equals(oldId)) {
                    dev_it.remove();
                    current.setRegId(newId);
                    devices.add(current);
                }
            }
            Iterator<Contributor> con_it = contributors.iterator();
            while (con_it.hasNext()) {
                Contributor current = con_it.next();
                if (current.getRegId().equals(oldId)) {
                    con_it.remove();
                    current.setRegId(newId);
                    contributors.add(current);
                    updateRegDbCon(oldId, newId);
                }
            }
            Iterator<Contributor> sub_it = subscriptors.iterator();
            while (sub_it.hasNext()) {
                Contributor current = sub_it.next();
                if (current.getRegId().equals(oldId)) {
                    sub_it.remove();
                    current.setRegId(newId);
                    subscriptors.add(current);
                    updateRegDbSub(oldId, newId);
                }
            }
            Iterator<Registrant> dvl_it = developers.iterator();
            while (dvl_it.hasNext()) {
                Registrant current = dvl_it.next();
                if (current.getRegId().equals(oldId)) {
                    dvl_it.remove();
                    current.setRegId(newId);
                    developers.add(current);
                }
            }
        }
    }

    /**
     * Gets all registered devices.
     *
     * @return
     */
    public static List<Registrant> getDevicesFromDb() {
        List<Registrant> Devices = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Devices;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                Devices.add(current);
            }
            //System.out.println("getDevicesFromDb "+ statement.getResultSet().toString());
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return Devices;
    }

    public static List<Registrant> getDevices() {
        synchronized (devices) {
            return new ArrayList<Registrant>(devices);
        }
    }

//sets reachability
    public static void setReachabilityDB(String regId, boolean reach) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update Contributors set reachability=? where regId=?;";
            statement = connection.prepareStatement(sqle);

            statement.setString(2, regId);
            statement.setBoolean(1, reach);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            //System.out.println("updateRegDbcon "+ statement.getResultSet().toString());
            //System.out.println("egine to updateRegDbcon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    //sets time
    public static void setTime(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("alla3a xrono"+ regId);
            String sqle = "update Contributors set lasttime=now() where regId=? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);

            int i = statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            // System.out.println("updateRegDbcon "+ i);
            //System.out.println("egine to updateRegDbcon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    //sets availability
    public static void setAvailabilityDB(String regId, boolean reach) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update Contributors set availability=? where regId=?;";
            statement = connection.prepareStatement(sqle);

            statement.setString(2, regId);
            statement.setBoolean(1, reach);
            statement.executeUpdate();

            //fix current.setTimestamp(rs.getString(8));
            //System.out.println("updateRegDbcon "+ statement.getResultSet().toString());
            //System.out.println("egine to updateRegDbcon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

    }

    /**
     * Gets reachable devices.
     *
     * @return
     */
    public static List<Registrant> getReachableDevicesFromDb() {
        List<Registrant> Devices = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Devices where reachability =1 ;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                Devices.add(current);
            }
            System.out.println("getrechabDevicesFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return Devices;
    }

    public static List<Registrant> getReachableDevices() {
        synchronized (devices) {
            List<Registrant> reachableDevices = new ArrayList<Registrant>();
            Iterator<Registrant> dev_it = devices.iterator();
            while (dev_it.hasNext()) {
                Registrant current = dev_it.next();
                if (current.getReachability()) {
                    reachableDevices.add(current);
                }
            }
            return reachableDevices;
        }
    }

    /**
     * Gets available devices.
     *
     * @return
     */
    public static List<Registrant> getAvailableDevicesFromDb() {
        List<Registrant> Devices = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from Devices where availability=1;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                if (current.getAvailability()) {
                    Devices.add(current);
                }
            }
            //System.out.println("getavailable contr");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return Devices;
    }

    public static List<Registrant> getAvailableDevices() {
        synchronized (devices) {
            List<Registrant> availableDevices = new ArrayList<Registrant>();
            Iterator<Registrant> dev_it = devices.iterator();
            while (dev_it.hasNext()) {
                Registrant current = dev_it.next();
                if (current.getAvailability()) {
                    availableDevices.add(current);
                }
            }
            return availableDevices;
        }
    }

    public static List<Contributor> getAllContributorsFromDb() {
        List<Contributor> allContributors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                allContributors.add(current);
            }
            System.out.println("getallContributorsFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (allContributors);
    }

    /**
     * Gets all contributing devices.
     *
     * @return
     */
    public static List<Contributor> getContributors() {
        synchronized (contributors) {
            //edit
            return (getReachableContributorsFromDb());
        }
    }

    /**
     * Gets reachable contributing devices.
     *
     * @return
     */
    public static List<Contributor> getReachableContributorsFromDb() {
        List<Contributor> reachableContributors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors where reachability=1;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                reachableContributors.add(current);
            }
            System.out.println("getreachableContributorFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (reachableContributors);
    }

    public static List<Contributor> getReachableContributors() {
        synchronized (contributors) {
            List<Contributor> reachableContributors = new ArrayList<Contributor>();
            Iterator<Contributor> con_it = contributors.iterator();
            while (con_it.hasNext()) {
                Contributor current = con_it.next();
                if (current.getReachability()) {
                    reachableContributors.add(current);
                }
            }

            //edit
            return reachableContributors;
        }
    }

    /**
     * Gets available contributing devices.
     *
     * @return
     */
    public static List<Contributor> getAvailableContributorsFromDbTesting() {
        List<Contributor> availableContributors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(rs.getBoolean(4));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                if (current.getAvailability()) {
                    availableContributors.add(current);
                }
            }
            System.out.println("getravailableContributorFromDb " + statement.getResultSet());
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (availableContributors);
    }

    public static List<Contributor> getAvailableContributorsFromDb() {
        List<Contributor> availableContributors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from Contributors;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                if (current.getAvailability()) {
                    availableContributors.add(current);
                }
            }
            rs.last();
            System.out.println("getravailableContributorFromDb size " + rs.getRow() + " size of map " + map.size());
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (availableContributors);
    }
    //TODO change the map that is iterated it should be the activeContributors
    public static List<Contributor> getAvailableContributors() {
        List<Contributor> availableContributors = new ArrayList<Contributor>();
        Iterator itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Entry entry = (Entry) itr.next();
            String id = (String) entry.getKey();
            long start = ((Timestamp) entry.getValue()).getTime();
            Date date = new Date();
            long now = date.getTime();
            if (start + delay_between_updates > now) {
                Contributor cont = getContributor(id);
                if (cont != null) {
                    availableContributors.add(cont);
                } else {
                   /*
                    //removed slow this only needed for restarts
                    System.out.println("psaxnw stin dB kai to bazw stin lista twn cont ");
                    cont = getContributorFromDb(id);
                    activeContributors.put(id, cont);
                    availableContributors.add(cont); */
                }
            }
            /*else
                map.remove(entry.getKey());*/
        }
        System.out.println("available contributors"
                + " size" + availableContributors.size());

        return availableContributors;
        /*
         synchronized (contributors) {
         List<Contributor> availableContributors = new ArrayList<Contributor>();
         Iterator<Contributor> con_it = contributors.iterator();
         while(con_it.hasNext())
         {
         Contributor current = con_it.next();
         if(current.getAvailability())
         {
         availableContributors.add(current);
         }
         }

         return availableContributors;
         }
         */
    }

    /**
     * Gets all subscripted devices.
     *
     * @return
     */
    public static List<Contributor> getAllSubscriptorsFromDb() {
        List<Contributor> allSubscriptors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Subscriptors;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(rs.getBoolean(4));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                allSubscriptors.add(current);
            }
            System.out.println("getallContributorsFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (allSubscriptors);
    }

    public static List<Contributor> getSubscriptors() {
        synchronized (subscriptors) {
            return new ArrayList<Contributor>(subscriptors);
        }
    }

    /**
     * Gets reachable subscripted devices.
     *
     * @return
     */
    public static List<Contributor> getReachableSubscriptorsFromDb() {
        List<Contributor> reachableSubscriptors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Subsciptors where reachability=1;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery(sqle);

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                reachableSubscriptors.add(current);
            }
            System.out.println("getreachableSubscrFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (reachableSubscriptors);
    }

    public static List<Contributor> getReachableSubscriptors() {
        synchronized (subscriptors) {
            List<Contributor> reachableSubscriptors = new ArrayList<Contributor>();
            Iterator<Contributor> sub_it = subscriptors.iterator();
            while (sub_it.hasNext()) {
                Contributor current = sub_it.next();
                if (current.getReachability()) {
                    reachableSubscriptors.add(current);
                }
            }
            getReachableSubscriptorsFromDb();
            return reachableSubscriptors;
        }
    }

    /**
     * Gets avaiable subscripted devices.
     *
     * @return
     */
    public static List<Contributor> getAvailableSubscriptorsFromDb() {
        List<Contributor> availableSubscriptors = new ArrayList<Contributor>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Subsciptors where reachability=1;";
            statement = connection.prepareStatement(sqle);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                Contributor current = new Contributor(new Registrant());
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setBookingId(rs.getString(7));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));
                current.setLastReceiver(rs.getBoolean(5));
                current.setReachability(rs.getBoolean(6));
                current.setTTL(rs.getInt(9));
                current.setTimestamp(Timestamp.valueOf(rs.getString(8)));
                if (current.getAvailability()) {
                    availableSubscriptors.add(current);
                }
            }
            System.out.println("getavailSubscrFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return (availableSubscriptors);
    }

    public static List<Contributor> getAvailableSubscriptors() {
        synchronized (subscriptors) {
            List<Contributor> availableSubscriptors = new ArrayList<Contributor>();
            Iterator<Contributor> sub_it = subscriptors.iterator();
            while (sub_it.hasNext()) {
                Contributor current = sub_it.next();
                if (current.getAvailability()) {
                    availableSubscriptors.add(current);
                }
            }
            return availableSubscriptors;
        }
    }

    /**
     * Gets all developer devices.
     *
     * @return
     */
    public static List<Registrant> getDevelopersFromDb() {
        List<Registrant> Developers = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Developers;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                Developers.add(current);
            }
            System.out.println("getDevicesFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return Developers;
    }

    public static List<Registrant> getDevelopers() {
        synchronized (developers) {
            return new ArrayList<Registrant>(developers);
        }
    }

    /**
     * Gets reachable subscripted devices.
     *
     * @return
     */
    public static List<Registrant> getReachableDevelopersFromDb() {
        List<Registrant> Developers = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Developers where reachability =1 ;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                Developers.add(current);
            }
            System.out.println("getDevicesFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }

        return Developers;
    }

    public static List<Registrant> getReachableDevelopers() {
        synchronized (developers) {
            List<Registrant> reachableDevelopers = new ArrayList<Registrant>();
            Iterator<Registrant> dvl_it = developers.iterator();
            while (dvl_it.hasNext()) {
                Registrant current = dvl_it.next();
                if (current.getReachability()) {
                    reachableDevelopers.add(current);
                }
            }
            return reachableDevelopers;
        }
    }

    /**
     * Gets avaiable subscripted devices.
     *
     * @return
     */
    public static List<Registrant> getAvailableDevelopersFromDb() {
        List<Registrant> Developers = new ArrayList<Registrant>();
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            System.out.println("sinde8ika me basi");
            String sqle = "select * from Developers where availability =1 ;";
            statement = connection.prepareStatement(sqle);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Registrant current = new Registrant();
                current.setAvailability(get_state(rs.getString(1)));
                current.setRegId(rs.getString(1));
                current.setIpAddress(rs.getString(3));
                current.setUserName(rs.getString(2));

                current.setReachability(rs.getBoolean(5));
                current.setTTL(rs.getInt(6));
                if (current.getAvailability()) {
                    Developers.add(current);
                }
            }
            System.out.println("getDevicesFromDb " + statement.getResultSet().toString());
            System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
            }
        }
        return Developers;
    }

    public static List<Registrant> getAvailableDevelopers() {
        synchronized (developers) {
            List<Registrant> availableDevelopers = new ArrayList<Registrant>();
            Iterator<Registrant> dvl_it = developers.iterator();
            while (dvl_it.hasNext()) {
                Registrant current = dvl_it.next();
                if (current.getAvailability()) {
                    availableDevelopers.add(current);
                }
            }
            return availableDevelopers;
        }
    }

}
