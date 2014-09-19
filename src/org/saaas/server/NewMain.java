package org.saaas.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //DBCalls.zero_particepated();
        //AlgoChoise.select_winner_to_deploy_online(5,18,130);
        Datastore.getAllContributorsFromDb();
        
    }
}
