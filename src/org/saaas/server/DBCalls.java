/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public final class DBCalls {

    static String connectionURL = "jdbc:mysql://localhost:3306/saas_project";
    public static final Map<String, CostProfile> cost_map = new HashMap<String, CostProfile>();
    static Connection connection = null;
    public DBCalls() {
        
        try {
           Class.forName("com.mysql.jdbc.Driver").newInstance();
           connection = DriverManager.getConnection(connectionURL, "root", "");
        } catch (Exception ex) {
            Logger.getLogger(DBCalls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* @Override
    protected void finalize(){
    if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
    }*/
    public static void new_user(String regId, float local_cost, double lat, double lon) {
        sendToDbUser(regId, local_cost, 0, 0, lat, lon, 0);
        cost_map.put(regId, new CostProfile(regId, local_cost, getUserPart(regId), 0, lat, lon));

    }

    public static void update_cost(String regId, float local_cost, double lat, double lon) {

        cost_map.put(regId, new CostProfile(regId, local_cost, getUserPart(regId), 0, lat, lon));
        cost_map.get(regId).local_cost = local_cost;
        cost_map.get(regId).lat = lat;
        cost_map.get(regId).lon = lon;
        //updateDbUser(regId,local_cost,lat,lon);
    }
    
    private static void updateDbUser(String regId, float local_cost, double lat, double lon) {
        //Connection connection = null;
        try {

            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set local_cost=?,lat=?,lon=? where regId= ? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(4, regId);
            statement.setDouble(2, lat);
            statement.setDouble(3, lon);
            statement.setFloat(1, local_cost);
            statement.executeUpdate();

                       // System.out.println("updateDbUser");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
    }

    public static void pay_user(String regId, float payment) {
        cost_map.get(regId).pay = payment;
        //Connection connection = null;
        try {

            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set pay=? where regId= ? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(2, regId);
            statement.setFloat(1, payment);

            statement.executeUpdate();

            System.out.println("Pay user");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
    }

    public static boolean exist_in_db_us(String regId) {
        boolean found = false;
        //Connection connection = null;
        try {
            PreparedStatement statement = null;

           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from user_usage where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            found = rs.next();
            System.out.println("search in Db ");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
        return (found);

    }

    public static void deleteFromDbus(String regId) {

        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            String sqle = "delete from user_usage where regId=?;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("deleteFromDbuser ");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*/*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/

    }

    private static void sendToDbUser(String regId, float local_cost, int particepated, float round_pay, double lat, double lon, float total_pay) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            ///System.out.println("sinde8ika me basi");
            String sqle = "insert into user_usage (regId,local_cost,particepated,pay,lat,lon,total_pay)"
                    + " values(?,?,?,?,?,?,?);";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.setFloat(2, local_cost);
            statement.setInt(3, particepated);
            statement.setFloat(4, round_pay);
            statement.setDouble(5, lat);
            statement.setDouble(6, lon);
            statement.setFloat(7, total_pay);
            statement.executeUpdate();

                        //System.out.println("sendToDbUser");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
    }

    public static int getUserPart(String regId) {
        //Connection connection = null;
        CostProfile costprof = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from user_usage where regId=? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                costprof = new CostProfile(regId, rs.getFloat(2), rs.getInt(3), rs.getFloat(4), rs.getDouble(5), rs.getDouble(6));
            }
                       //System.out.println("sendToDbUser");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
        if (costprof != null) {
            return costprof.particepated;
        } else {
            return 0;
        }
    }

    public static CostProfile getUser(String regId) {
        CostProfile late_insert = cost_map.get(regId);
        updateDbUser(late_insert.regId, late_insert.local_cost, late_insert.lat, late_insert.lon);
        //Connection connection = null;
        CostProfile costprof = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "select * from user_usage where regId=? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                costprof = new CostProfile(regId, rs.getFloat(2), rs.getInt(3), rs.getFloat(4), rs.getDouble(5), rs.getDouble(6));
            }
                       //System.out.println("sendToDbUser");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/

        return costprof;
    }

    public static void informDbforSelect(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set particepated=particepated+1 where regId= ? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("inform Db for Select");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
    }

    public static void end_of_auction(String regId) {
        CostProfile prof = getUser(regId);
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set pay=0,total_pay=total_pay+? where regId= ? ;";
            statement = connection.prepareStatement(sqle);

            statement.setString(2, regId);
            statement.setFloat(1, prof.pay);
            statement.executeUpdate();

            System.out.println("prof.pay " + prof.pay);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/

    }

    public static void zero_particepated() {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set particepated=0,pay=0;";
            statement = connection.prepareStatement(sqle);

            statement.executeUpdate();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/

    }

    public static void informDbforEndOfTask(String regId) {
        //Connection connection = null;
        try {
            PreparedStatement statement = null;
           //            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connection = DriverManager.getConnection(connectionURL, "root", "");

            //System.out.println("sinde8ika me basi");
            String sqle = "update user_usage set particepated="
                    + " case when particepated>0 then particepated-1 "
                    + "      else 0 end"
                    + "where regId= ? ;";
            statement = connection.prepareStatement(sqle);
            statement.setString(1, regId);
            statement.executeUpdate();

            System.out.println("informDbforend of task");
            //System.out.println("egine to update");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } /*finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Datastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
    }
    /*
     * public static void informDbNotSelected (List<String> li){
     Iterator<String> itr= li.iterator();
     while (itr.hasNext()){
     String regId=itr.next();
     try {
     PreparedStatement statement = null;
    //            Class.forName("com.mysql.jdbc.Driver").newInstance();
     connection = DriverManager.getConnection(connectionURL, "root","");
			
     System.out.println("sinde8ika me basi");
     String sqle = "update user_usage set last_served=last_served+1 where regId= ? and last_served!=0;"
     ;	
     statement =  connection.prepareStatement(sqle);
     statement.setString(1, regId);
     statement.executeUpdate();
                        
     System.out.println("informDbNotSelected");
     System.out.println("egine to update");
     } catch (Exception e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
   
         
     }
     }
     */
}
