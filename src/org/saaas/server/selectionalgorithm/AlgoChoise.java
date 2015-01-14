/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.selectionalgorithm;

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
import java.util.TreeMap;
import org.saaas.server.Contributor;
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
    private static double delta_for_fixing_the_limit = 0.8;
    private static mapfromXmltrack map;
    private static int sensing_times = 10;
    private static double point_range = 0.018;
    protected static boolean point_coverBool;
    public AlgoChoise(){
        
    }
    private static List<CostProfile> select_available_users() {
        List<CostProfile> costprof = new ArrayList<CostProfile>();
        //testing List<Contributor> availableContributors=datastore.getAvailableContributorsFromDb();
        List<Contributor> availableContributors = datastore.getAvailableContributors();
        // System.out.println(availableContributors.size());
        Iterator<Contributor> itr = availableContributors.iterator();
        while (itr.hasNext()) {
            Contributor current = itr.next();
            CostProfile costprofus;
            costprofus = dbCalls.getUser(current.getRegId());
            //  System.out.println("local coost "+costprofus.local_cost);
            if (costprofus != null) {
                costprof.add(costprofus);
            }

        }
        return costprof;
    }

    //for testing only
 
    /*for testing
     private static List <CostProfile> select_available_users(){
     List <CostProfile> costprof=new ArrayList<CostProfile>();
     CostProfile costprofus;
     for(int i=1;i<8;i++){
          
     costprofus = dbCalls.getUser(""+i);
     if (costprofus!=null)
     costprof.add(costprofus);
     // System.out.println(""+costprof);
          
     }
     return costprof;
     }
     */

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
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    public static List<String> select_winners_to_deploy_offline(int users_to_deploy, int value_of_task) {
        List<String> regIds = new ArrayList<String>();
        List<CostProfile> costprof = select_available_users();
        //List <String> not_selectedregIds=new ArrayList<String>();
        //List <String> allregIds=new ArrayList<String>();
        HashMap<String, Float> costs = new HashMap<String, Float>();
        Iterator<CostProfile> itr = costprof.iterator();
        //TODO FOR TESTING ONLY ERASE WHEN FINISH
        dbCalls.zero_particepated();
        ////erase TODO
        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            System.out.println(computeCost(costprofus));
            costs.put(costprofus.regId,computeCost(costprofus));
            //allregIds.add(costprofus.regId);
        }
        Map<String,Float> sortedCosts=sortByValue(costs);
        int selected = 0;
        for (Map.Entry<String, Float> entry : sortedCosts.entrySet()) {
            //System.out.println("cost"+cost);
            if (value_of_task >= entry.getValue()) {
                regIds.add(entry.getKey());
                selected++;
                dbCalls.informDbforSelect(entry.getKey());
            }
            if (selected == users_to_deploy) {
                break;
            }

        }
        // allregIds.removeAll(regIds);
        //not_selectedregIds=allregIds;
        //ComputeCost.informDbNotSelected(not_selectedregIds);
        if (selected < users_to_deploy);
        //do something not enough users ?;

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

    private static float computeCostOnline(CostProfile costprofus, mapfromXmltrack map) {
        float cost = computeCost(costprofus);
        float value = computeValOnline(costprofus, map);
        cost = value / cost;
        System.out.println("marginal value :" + cost);
        return (cost);
    }

    private static float computeValOnline(CostProfile costprofus, mapfromXmltrack map) {
        float value = 0;
        if (point_coverBool) {
            value = ValueEstimator.Value_est(costprofus, map, point_range);
        } else {
            value = ValueEstimator.Value_est(costprofus, map);
        }
        return (value);
    }

    private static float computeValSet(List<CostProfile> From, mapfromXmltrack map) {
        float value = (float) 0.0001;
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

    private static CostProfile find_max(List<CostProfile> From, List<CostProfile> To, mapfromXmltrack map) {
        float greatest_value = 0;
        Iterator<CostProfile> itr = From.iterator();
        CostProfile selected = null;
        float val;
        while (itr.hasNext()) {
            CostProfile costprofus = itr.next();
            val = computeValOnline((costprofus), map);
            System.out.println("val :" + val);
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

    private static CostProfile find_max_density(List<CostProfile> From, mapfromXmltrack map) {
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
        dbCalls.pay_user(user.regId, payment);
    }

    private static float get_limit(List<CostProfile> selected, float Budget, double limit) {
        mapfromXmltrack my_map = new mapfromXmltrack(sensing_times);
        float lim = 0;
        if (selected == null) {
            return ((float) limit);
        }
        List<CostProfile> checked = new ArrayList<CostProfile>();
        List<CostProfile> help;
        CostProfile i = find_max_density(selected, my_map);

        help = new ArrayList<CostProfile>();
        help.add(i);

        //computeValSet(help);
        if (i == null) {
            return (float) (limit);
        }
        while ((!selected.isEmpty()) && (computeCost(i) <= (computeValOnline(i, my_map) * Budget / computeValSet(help, my_map)))) {
            ValueEstimator.addtoMap(i, my_map);
            checked.add(i);
            selected.removeAll(checked);
            i = find_max_density(selected, my_map);
            help.add(i);

        }
        //  System.out.println("get limit on "+checked);
        lim = computeValSet(checked, my_map) / Budget;
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
    private static void SendApks(List<CostProfile> To){
        //GetContributorsServlet ListConverter(To);
    }
    
    public static List<String> select_winners_to_deploy_online(int user_to_deploy, int Time, int Budget, boolean point_cover) {
        datastore=Datastore.getInstance();
        dbCalls=DBCalls.getInstance();
        long start = System.currentTimeMillis();
        List<CostProfile> selected = new ArrayList<CostProfile>();
        List<CostProfile> to_select = new ArrayList<CostProfile>();
        List<CostProfile> left = new ArrayList<CostProfile>();
        List<CostProfile> help = new ArrayList<CostProfile>();
        List<CostProfile> selected_prev_round = new ArrayList<CostProfile>();
        //arrival time is timestamp time stored in DB
        float Start_time;
        point_coverBool = point_cover;
        int t = 1; //in sec as Time
        double lim = 0.2; //to be changed
        float time_st = (float) (Time / Math.pow(2, Math.ceil(log(Time, 2))));
        float budget_st = (float) (Budget / Math.pow(2, Math.ceil(log(Time, 2))));
        Date date = new Date();
        Start_time = date.getTime();
        CostProfile costprof;
        List<CostProfile> Online_users = select_available_users();
        map = new mapfromXmltrack(sensing_times);
    
        //FOR TESTING ONLY ERASE WHEN FINISH
        dbCalls.zero_particepated();

        Iterator<CostProfile> itr;

        while (t < Time) {
            Online_users = select_available_users();
            to_select = Online_users;
            to_select.removeAll(selected);
            if (selected.size() == user_to_deploy) {
                        break;
                    }
            // System.out.println(selected);
            //System.out.println(to_select);       
            while (!to_select.isEmpty()) {
               
                //finds max value
                costprof = find_max(to_select, selected, map);
                if (costprof == null) {
                    break;
                }
                float val = computeValOnline(costprof, map);
                if ((computeCostOnline(costprof, map) >= lim) && (val / lim < (budget_st - sum_of_payments(selected)))) {
                    System.out.println("8a plirw8ei o " + costprof.regId + " me " + val / lim);
                    pay_user(costprof, (val / lim));
                    selected.add(costprof);
                    if (point_cover) {
                        ValueEstimator.addtoMap(costprof, map, point_range);
                    } else {
                        ValueEstimator.addtoMap(costprof, map);
                    }
                    dbCalls.informDbforSelect(costprof.regId);
                    //if (costprof.regId.equals("4"));
                    //  System.out.println("epilex8ei 1");
                    if (selected.size() == user_to_deploy) {
                        break;
                    }

                }
                /* else sum of payments maybe ?? round payment and total payment FIX
                 pay_user(costprof,0);
                 */

                to_select.remove(costprof);
            }
            try {

                int sleep_time = 1000;
                Thread.sleep(sleep_time);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            left = Online_users;
            Online_users = select_available_users();
            left.removeAll(Online_users);
            // left.addAll(help);
            //if(t<2)
            //left.add(dbCalls.getUser("2"));
            //Online_users = select_available_users();
            help = new ArrayList<CostProfile>();
            help.addAll(selected);
            help.removeAll(selected_prev_round);
            SendApks(help);
            selected_prev_round.addAll(help);
            if (selected.size() == user_to_deploy) {
                break;
            }
            if (t >= time_st) {
                //    System.out.println("left: "+left);

                if (!left.isEmpty()) {

                    lim = get_limit(left, budget_st, lim);
                    //  System.out.println("limit : "+lim );

                }
                time_st = 2 * time_st;
                budget_st = 2 * budget_st;
                //2h fasi
                to_select = Online_users;

                while (!to_select.isEmpty()) {
                    //fix//
                    System.out.println("budget Debug " + budget_st);
                    costprof = find_max(to_select, selected, map);
                    if (costprof == null) {
                        break;
                    }
                    float sum = sum_of_payments(selected);
                    System.out.println("sum of payments " + sum);
                    if (selected.size() == user_to_deploy) {
                        break;
                    }
                    float val = computeValOnline(costprof, map);
                    if (selected.contains(costprof)) {

                        if (point_cover) {
                            ValueEstimator.extractFromMap(costprof, map, point_range);
                        } else {
                            ValueEstimator.extractFromMap(costprof, map);
                        }
                    }
                    if ((computeCostOnline(costprof, map) >= lim) && (val / lim < (budget_st - sum)) && ((val / lim) > costprof.pay)) {
                        pay_user(costprof, (val / lim));
                        // System.out.println("pay "+ val/lim);
                        if (!selected.contains(costprof)) {
                            // if (costprof.regId.equals("4"));
                            //    System.out.println("epilex8ei");
                            selected.add(costprof);

                            dbCalls.informDbforSelect(costprof.regId);
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
        update_total_payments(reg);
        System.out.println(reg);
        System.out.println(" Time : " + (System.currentTimeMillis() - start) / 1000);
        return (reg);
    }

    static void update_total_payments(List<String> reg) {
        Iterator<String> itr = reg.iterator();
        while (itr.hasNext()) {
            dbCalls.end_of_auction(itr.next());
        }
    }
}
