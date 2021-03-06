/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.client.main;

import java.util.Arrays;
import java.util.Map;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import de.scoopgmbh.copper.monitoring.client.context.ApplicationContext;

public class MonitorMain extends Application {
	
	static Logger logger = LoggerFactory.getLogger(MonitorMain.class);

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		ApplicationContext applicationContext = new ApplicationContext();
		primaryStage.titleProperty().bind(new SimpleStringProperty("Copper Monitor (server: ").concat(applicationContext.serverAdressProperty().concat(")")));
		new Button(); // Trigger loading of default stylesheet
		final Scene scene = new Scene(applicationContext.getMainPane(), 1300, 900, Color.WHEAT);

		scene.getStylesheets().add(this.getClass().getResource("/de/scoopgmbh/copper/gui/css/base.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//"--name=value".
		Map<String, String> parameter = getParameters().getNamed();
		String monitorServerAdress = parameter.get("monitorServerAdress");
		String monitorServerUser = parameter.get("monitorServerUser");
		String monitorServerPassword = parameter.get("monitorServerPassword");

		if (!Strings.isNullOrEmpty(monitorServerAdress)){
			applicationContext.setHttpGuiCopperDataProvider(monitorServerAdress,monitorServerUser,monitorServerPassword);
		} else {
			applicationContext.createLoginForm().show();
        }

//        new Thread(){
//             {
//                 setDaemon(true);
//             }
//             @Override
//             public void run() {
//                 while(true){
//                     PerformanceTracker sceneTracker = PerformanceTracker.getSceneTracker(primaryStage.getScene());
//                     System.out.println(sceneTracker.getAverageFPS());
//                     sceneTracker.resetAverageFPS();
//
//                     try {
//                         Thread.sleep(1000);
//                     } catch (InterruptedException e) {
//                         throw new RuntimeException(e);
//                     }
//
//                 }
//             }
//         }.start();
//		ScenicView.show(scene);
	}

	public static void main(final String[] arguments) {
//        System.setProperty("javafx.animation.fullspeed","true");
        logger.info("Parameter: "+Arrays.asList(arguments));
        Application.launch(arguments);
	}
}