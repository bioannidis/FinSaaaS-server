/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.util.TimerTask;

/**
 *
 * @author gmerlino
 */
public class ChurnMgmtTask extends TimerTask {
   final private Runnable doWhenTriggered;

    public ChurnMgmtTask() {
        this.doWhenTriggered = null;
    }

    /**
     *
     * @param churnmToBeStarted
     */
    public ChurnMgmtTask(Runnable churnmToBeStarted) {
      this.doWhenTriggered = churnmToBeStarted;
   }

   @Override
   public void run() {
         doWhenTriggered.run(); 
   }
}
