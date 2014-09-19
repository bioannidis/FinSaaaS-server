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

import com.google.android.gcm.server.Sender;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context initializer that loads the API key from a
 * {@value #PATH} file located in the classpath (typically under
 * {@code WEB-INF/classes}).
 */
@SuppressWarnings("serial")
public class ApiKeyInitializer extends BaseServlet implements ServletContextListener{

  static final String ATTRIBUTE_ACCESS_KEY = "apiKey";
  private static final String PATH = "/api.key";
  private final Logger logger = Logger.getLogger(getClass().getName());
  private ContributorsThread tc;
  private DevelopersThread td;
  private Sender sender;


    /**
     *
     * @param event
     */
    @Override
  public void contextInitialized(ServletContextEvent event) {
    logger.info("Reading " + PATH + " from resources (probably from " +
        "WEB-INF/classes");
    String key = getKey();
    event.getServletContext().setAttribute(ATTRIBUTE_ACCESS_KEY, key);
    sender = new Sender(key);
    logger.info(sender.toString());
	//Run thread Contributors which has to send message (every 2.30 min) to all contributors to check their availability
    tc = new ContributorsThread(this.sender);
	//Run thread Developers which has to send message (every 5 sec) to all developers containing the number of contributors and subscriptors
    td = new DevelopersThread(this.sender);
    logger.info("Threads Start");
  }

  /**
   * Gets the access key.
     * @return 
   */
  protected String getKey() {
    InputStream stream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(PATH);
    if (stream == null) {
      throw new IllegalStateException("Could not find file " + PATH +
          " on web resources)");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try {
      String key = reader.readLine();
      return key;
    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + PATH, e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Exception closing " + PATH, e);
      }
    }
  }

    /**
     *
     * @param event
     */
    @Override
  public void contextDestroyed(ServletContextEvent event) {
//    CountdownTask countdownTask = (CountdownTask) event.getServletContext().getAttribute("countdowntask");
//    if (countdownTask != null)
//    {
//        countdownTask.stopCountdownTask();
//        countdownTask.interrupt();
//        countdownTask = null;
//    }	  
    tc.interrupt();
    td.interrupt();
}

//  public void contextInitialized(ServletContextEvent event)
//{
//    final CountdownTask countdownTask = new CountdownTask();
//    countdownTask.start();
//    event.getServletContext().setAttribute("countdowntask", countdownTask);
//}
}
