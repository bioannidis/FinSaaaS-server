/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.selectionalgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.saaas.server.dataobjects.Contributor;
import org.saaas.server.Datastore;
import org.saaas.server.selectionalgorithm.DBCalls;

/**
 *
 * @author Administrator
 */
public class AlgoChoise {

    private static Datastore datastore;
    private static DBCalls dbCalls;
    /*
     *USE : Delta helpts to obtain a slight underestimate in the limit 
     *so that the budjet will be not wasted
     * must be tested in the simulation
     */
    private static double delta_for_fixing_the_limit = 0.1;
    private static MapfromXmltrack map;
    private static int sensing_times = 3;
    private static double point_range = 0.018;
    protected static boolean point_coverBool;

    public AlgoChoise() {

    }

    private static List<CostProfile> select_available_users() {
        List<CostProfile> costprof = new ArrayList<CostProfile>();
        //testing List<Contributor> availableContributors=datastore.getAvailableContributorsFromDb();
        List<Contributor> availableContributors = datastore.getAvailableContributors();
        Iterator<Contributor> itr = availableContributors.iterator();
        while (itr.hasNext()) {
            Contributor current = itr.next();
            CostProfile costprofus;
            costprofus = dbCalls.getUser(current.getRegId());
            if (costprofus != null) {
                costprof.add(costprofus);
            }

        }
        return costprof;
    }

    private static float computeCost(CostProfile costprofus) {
        float cost = 0;
        if (costprofus.particepated != 0) {
            cost = (float) (costprofus.local_cost * Math.pow(2, (float) (costprofus.particepated)));
        } else {
            cost = costprofus.local_cost;
        }
        return cost;
    }

    public static Map<String, Float> sortByValue(Map<String, Float> map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        Collections.reverse(list);
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void output_selected(List<String> li, String type, int time, int val, String active, String available, String sumofpay, String value) {
        try {
            Iterator<String> itr = li.iterator();
            Logger logger = Logger.getLogger("MyLog");
            FileHandler fh;
            fh = new FileHandler("C:\\Users\\palinka\\Dropbox\\test\\" + type + "results.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info(active);
            logger.info(available);
            logger.info(sumofpay);
            logger.info(value);
            logger.info("execution time : " + time);

            logger.info("value of platform = " + val);
            logger.info("number of times a point has to be sensed " + sensing_times);
            logger.info("+++++++++++++++++++list of selected users+++++++++++++++++++++++++");
            while (itr.hasNext()) {
                CostProfile cp = dbCalls.getUser(itr.next());
                logger.info(cp.toString());

            }
            logger.info("+++++++++++++++++++count of selected+++++++++++++++++++++++++ : " + li.size());
        } catch (IOException ex) {
            Logger.getLogger(AlgoChoise.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AlgoChoise.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int compute_value_selected(List<String> li) {
        point_coverBool = true;
        MapfromXmltrack my_map = new MapfromXmltrack(sensing_times);
        int val = 0;
        Iterator<String> itr = li.iterator();
        while (itr.hasNext()) {
            CostProfile cp = dbCalls.getUser(itr.next());
            val = (int) (val + computeValOnline(cp, my_map));
        }
        return val;
    }

    //TODO must compute the value at the end and add time
    public static List<String> select_winners_to_deploy_offline(int users_to_deploy, int value_of_task, int Time) {
        List<String> regIds = new ArrayList<String>();
        String availableUsers = null, activeUsers = null, sumofpayments = null, value = null;
        point_coverBool = true;
        float payment = 0;
        int active = 0, val = 0, t = 0, selected = 0;
        map = new MapfromXmltrack(sensing_times);
        long start = System.currentTimeMillis();
        while (t < Time) {
            HashMap<String, Float> costs = new HashMap<String, Float>();
            List<CostProfile> costprof = select_available_users();
            availableUsers = availableUsers + "\n" + costprof.size();
            sumofpayments = sumofpayments + "\n" + payment;
            value = value + "\n" + val;
            active = 0;
            Iterator<CostProfile> itr = costprof.iterator();
            while (itr.hasNext()) {
                CostProfile costprofus = itr.next();
                if (!regIds.contains(costprofus.regId)) {
                    costs.put(costprofus.regId, computeCost(costprofus));
                } else {
                    active++;
                }
            }
            activeUsers = activeUsers + "\n" + active;
            Map<String, Float> sortedCosts = sortByValue(costs);
            selected = 0;
            for (Map.Entry<String, Float> entry : sortedCosts.entrySet()) {
                if (value_of_task >= entry.getValue()) {
                    if (selected >= users_to_deploy - active) {
                        break;
                    }
                    regIds.add(entry.getKey());
                    selected++;
                    payment += entry.getValue();
                    dbCalls.payUser(entry.getKey(), entry.getValue());
                    SendApk(entry.getKey());
                    CostProfile cp = dbCalls.getUser(entry.getKey());
                    int valu = (int) computeValOnline(cp, map);
                    ValueEstimator.addtoMap(cp, map, point_range);
                    val = (int) (val + valu);
                    dbCalls.informMapforSelect(entry.getKey());
                    
                }
                if (selected >= users_to_deploy - active) {
                    break;
                }

            }
            t++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AlgoChoise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int time = (int) ((System.currentTimeMillis() - start) / 1000);
        if (selected < users_to_deploy) {

        }
        output_selected(regIds, "offline" + (users_to_deploy * value_of_task) + "" + users_to_deploy + "" + Time, time, val, activeUsers, availableUsers, sumofpayments, value);
        return regIds;
    }
    //TODO ERASE LIST !!! To not needed
    /*private static float compute_value(CostProfile costprofus, List<CostProfile> To, mapfromXmltrack map) {
     float value = 0;
     if (point_coverBool) {
     value = ValueEstimator.Value_est(costprofus, map, point_range);
     } else {
     value = ValueEstimator.Value_est(costprofus, map);
     }
     return (value);
     }*/

    private static float computeCostOnline(CostProfile costprofus, MapfromXmltrack map) {
        float cost = computeCost(costprofus);
        float value = computeValOnline(costprofus, map);
        cost = value / cost;
        System.out.println("marginal value :" + cost);
        return (cost);
    }

    private static float computeValOnline(CostProfile costprofus, MapfromXmltrack map) {
        float value = 0;
        if (point_coverBool) {
            value = ValueEstimator.valueEstimation(costprofus, map, point_range);
        } else {
            value = ValueEstimator.valueEstimation(costprofus, map);
        }
        return (value);
    }

    private static float computeValSet(List<CostProfile> From, MapfromXmltrack map) {
        float value = (float) 0.000001;
        Iterator<CostProfile> itr = From.iterator();
        ArrayList<CostProfile> copy = new ArrayList<CostProfile>(From);
        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            copy.remove(costprofus);
            value += computeValOnline(costprofus, map);
            copy.add(costprofus);
            //allregIds.add(costprofus.regId);
        }
        return value;
    }

    static float log(int x, int base) {
        return (float) (Math.log(x) / Math.log(base));
    }

    private static CostProfile find_max(List<CostProfile> From, List<CostProfile> To, MapfromXmltrack map) {
        float greatest_value = 0;
        Iterator<CostProfile> itr = From.iterator();
        CostProfile selected = null;
        float val;
        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            val = computeValOnline((costprofus), map);
            if (val > greatest_value) {
                greatest_value = val;
                selected = costprofus;
            }
            if (val == 0) {
                selected = costprofus;
            }
            //allregIds.add(costprofus.regId);
        }
        return (selected);
    }

    private static CostProfile find_max_density(List<CostProfile> From, MapfromXmltrack map) {
        float greatest_value = 0;
        Iterator<CostProfile> itr = From.iterator();
        CostProfile selected = null;
        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            float val = computeCostOnline((costprofus), map);
            if (val > greatest_value) {
                greatest_value = val;
                selected = costprofus;
            }
            //allregIds.add(costprofus.regId);
        }
        return (selected);
    }

    private static float sum_of_payments(List<CostProfile> selected) {
        float sum = 0;
        Iterator<CostProfile> itr = selected.iterator();

        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            sum += costprofus.pay;            //allregIds.add(costprofus.regId);
        }
        return sum;
    }

    private static void pay_user(CostProfile user, double pay) {
        float payment = (float) pay;
        user.pay = payment;
        //TODO remove the calls to the database
        dbCalls.payUser(user.regId, payment);
    }

    private static float get_limit(List<CostProfile> left, float Budget, double limit) {
        MapfromXmltrack my_map = new MapfromXmltrack(sensing_times);
        float lim = 0;
        if (left == null) {
            return ((float) limit);
        }
        List<CostProfile> checked = new ArrayList<CostProfile>();
        List<CostProfile> help;
        CostProfile i = find_max_density(left, my_map);

        help = new ArrayList<CostProfile>();
        help.add(i);

        //computeValSet(help);
        if (i == null) {
            return (float) (limit);
        }
        while ((!left.isEmpty()) && (computeCost(i) <= (computeValOnline(i, my_map) * Budget / computeValSet(help, my_map)))) {
            ValueEstimator.addtoMap(i, my_map);
            checked.add(i);
            left.removeAll(checked);
            i = find_max_density(left, my_map);
            if (i == null) {
                break;
            }
            help.add(i);

        }

        if (computeValSet(checked, my_map) == 0.001) {
            return (float) limit;
        }
        System.out.println(left.size());
        if (left.size() == 0) {
            return (float) limit;
        }
        float lim1 = (/*computeValSet(checked, my_map)*/left.size() / Budget);
        if ((lim1 < 0.0001) || lim1 > 1) {
            return (float) limit;
        }
        lim = lim1;
        return (float) (lim / delta_for_fixing_the_limit);
    }

    private static List<String> ListConverter(List<CostProfile> To) {
        List<String> regIds = new ArrayList<String>();
        Iterator<CostProfile> itr1 = To.iterator();
        CostProfile cos;
        while (itr1.hasNext()) {
            cos = itr1.next();
            regIds.add(cos.regId);
        }
        return (regIds);

    }

    private static void SendApks(List<CostProfile> To) {
        //GetContributorsServlet ListConverter(To);
    }

    private static void SendApk(String reg) {

    }

    public static List<String> select_winners_to_deploy_online(int user_to_deploy, int Time, int Budget, boolean point_cover) {
        try {
            datastore = Datastore.getInstance();
            dbCalls = DBCalls.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        List<CostProfile> selected = new ArrayList<CostProfile>();
        List<CostProfile> to_select = new ArrayList<CostProfile>();
        List<CostProfile> left = new ArrayList<CostProfile>();
        List<CostProfile> selected_online = null, left_this_stage = new ArrayList<CostProfile>();
        String availableUsers = null, activeUsers = null, sumofpayments = null, value = null;
        List<CostProfile> help = new ArrayList<CostProfile>();
        List<CostProfile> selected_prev_round = new ArrayList<CostProfile>();
        int activeSelected = 0;
        //arrival time is timestamp time stored in DB
        float Start_time;
        point_coverBool = point_cover;
        int t = 1; //in sec as Time
        double lim = 0.35; //to be changed
        float time_st = (float) (Time / Math.pow(2, Math.ceil(log(Time, 2))));
        float budget_st = (float) (Budget / Math.pow(2, Math.ceil(log(Time, 2))));
        Date date = new Date();
        Start_time = date.getTime();
        CostProfile costprof;
        List<CostProfile> online_users = select_available_users();
        map = new MapfromXmltrack(sensing_times);

        //FOR TESTING ONLY ERASE WHEN FINISH
        //dbCalls.zero_particepated();
        // Iterator<CostProfile> itr;
        while (t < Time) {
            long stageStart = System.currentTimeMillis();
            int size_before = online_users.size();
            System.out.println("selected size" + selected.size());
            left_this_stage = online_users;
            online_users = select_available_users();
            left_this_stage.removeAll(online_users);
            Iterator it = selected.iterator();
            while (it.hasNext()) {

                if (left_this_stage.contains((CostProfile) it.next())) {
                    activeSelected--;
                }

            }

            availableUsers = availableUsers + "\n" + online_users.size();
            sumofpayments = sumofpayments + "\n" + sum_of_payments(selected);
            activeUsers = activeUsers + "\n" + activeSelected;
            value = value + "\n" + selected.size();

            /*itr=Online_users.iterator();
             while(itr.hasNext()){
             CostProfile cp=itr.next();
             if(left_this_stage.contains(cp))
             left_this_stage.remove(cp);
             }*/
            System.out.println("left this stage : " + left_this_stage.size());
            left.addAll(left_this_stage);
            to_select = new ArrayList<CostProfile>(online_users);
            to_select.removeAll(selected);
            if (selected.size() >= user_to_deploy) {
                //  break;
            }
            while (!to_select.isEmpty()) {

                //finds max value
                costprof = find_max(to_select, selected, map);
                if (costprof == null) {
                    break;
                }
                float val = computeValOnline(costprof, map);
                if (((computeCostOnline(costprof, map) >= lim) && (val / lim < (budget_st - sum_of_payments(selected)))) && activeSelected < user_to_deploy) {
                    System.out.println("8a plirw8ei o " + costprof.regId + " me a3ia " + val);
                    pay_user(costprof, (val / lim));
                    selected.add(costprof);
                    activeSelected++;
                    SendApk(costprof.regId);
                    if (point_cover) {
                        ValueEstimator.addtoMap(costprof, map, point_range);
                    } else {
                        ValueEstimator.addtoMap(costprof, map);
                    }
                    //TODO remove this 
                    dbCalls.informMapforSelect(costprof.regId);
                    if (selected.size() >= user_to_deploy) {
                        // break;
                    }

                }
                /* else sum of payments maybe ?? round payment and total payment FIX
                 pay_user(costprof,0);
                 */

                to_select.remove(costprof);
            }

            // left.addAll(help);
            //if(t<2)
            //left.add(dbCalls.getUser("2"));
            //Online_users = select_available_users();
            help = new ArrayList<CostProfile>();
            help.addAll(selected);
            help.removeAll(selected_prev_round);
            SendApks(help);
            selected_prev_round.addAll(help);
            try {
                long sleep_time = 1000;
                long stageEnd = System.currentTimeMillis();
                System.out.println("time of stage " + t + " : " + -(stageStart - stageEnd));
                if (-(stageStart - stageEnd) < 1000) {
                    Thread.sleep(sleep_time + (stageStart - stageEnd));
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (selected.size() == user_to_deploy) {
                // break;
            }
            if (t >= time_st) {

                if (left.size() > 0) {

                    lim = get_limit(left, budget_st, lim);
                    System.out.println("limit : " + lim);

                }
                time_st = 2 * time_st;
                budget_st = 2 * budget_st;
                //2h fasi
                to_select = new ArrayList<CostProfile>(online_users);

                while (!to_select.isEmpty()) {
                    //fix//
                    if (selected.size() >= user_to_deploy) {
                        //  break;
                    }
                    System.out.println("budget Debug " + budget_st);
                    costprof = find_max(to_select, selected, map);
                    if (costprof == null) {
                        break;
                    }
                    float sum = sum_of_payments(selected);
                    System.out.println("sum of payments " + sum);
                    if (selected.size() == user_to_deploy) {
                        //    break;
                    }
                    float val = computeValOnline(costprof, map);
                    if (selected.contains(costprof)) {

                        if (point_cover) {
                            ValueEstimator.extractFromMap(costprof, map, point_range);
                        } else {
                            ValueEstimator.extractFromMap(costprof, map);
                        }
                    }
                    if (((computeCostOnline(costprof, map) >= lim) && (val / lim < (budget_st - sum)) && ((val / lim) > costprof.pay)) && activeSelected < user_to_deploy) {
                        pay_user(costprof, (val / lim));
                        if (!selected.contains(costprof)) {
                            SendApk(costprof.regId);
                            selected.add(costprof);
                            activeSelected++;
                            dbCalls.informMapforSelect(costprof.regId);
                        }
                        if (point_cover) {
                            ValueEstimator.addtoMap(costprof, map, point_range);
                        } else {
                            ValueEstimator.addtoMap(costprof, map);
                        }
                        System.out.println("allazw plirwmi xristi st " + budget_st);
                    }

                    to_select.remove(costprof);
                }
                help = new ArrayList<CostProfile>();
                help.addAll(selected);
                help.removeAll(selected_prev_round);
                SendApks(help);
                selected_prev_round.addAll(help);
            }
            System.out.println("Time : " + t);
            t = t + 1;
        }
        List<String> reg = ListConverter(selected);
        //update_total_payments(reg);
        //System.out.println(reg);
        int timeExec = (int) ((System.currentTimeMillis() - start) / 1000);
        output_selected(reg, "online" + Time + "" + Budget + "" + user_to_deploy, timeExec, reg.size(), activeUsers, availableUsers, sumofpayments, value);
        return (reg);
    }

    static void update_total_payments(List<String> reg) {
        Iterator<String> itr = reg.iterator();
        while (itr.hasNext()) {
            dbCalls.endOfAuction(itr.next());
        }
    }
}
