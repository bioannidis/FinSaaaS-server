/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.util.TimerTask;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author gmerlino
 */
public class CountdownTask extends TimerTask {
   private int counterToZero;
   final private Runnable doWhenTriggered;
//   private final boolean alive = true;
//   private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     *
     */
    public CountdownTask() {
        this.doWhenTriggered = null;
    }
   
    /**
     *
     * @param counterStart
     * @param countdownIsOver
     */
    public CountdownTask(int counterStart, Runnable countdownIsOver) {
      this.counterToZero = counterStart;
      this.doWhenTriggered = countdownIsOver;
   }

    @Override
    public void run() {
//        while (alive) {
//            try {
//                logger.log(Level.INFO, "counter set at {0}", counterToZero);
                counterToZero--;
                if (counterToZero == 0) {
                    cancel();
                    doWhenTriggered.run();
//                    stopCountdownTask();
                }
//            }
//            catch (InterruptedException ie) {
//                alive = false;
//            }
//        }
    }

    /**
     *
     */
//    public void stopCountdownTask()
//    {
//        this.alive = false;
//    }
}

