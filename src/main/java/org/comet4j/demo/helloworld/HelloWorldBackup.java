package org.comet4j.demo.helloworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.comet4j.core.CometContext;
import org.comet4j.core.CometEngine;

public class HelloWorldBackup implements ServletContextListener {
	private static final String CHANNEL = "hello";

	private static final String COM1 = "COM1";

	private static final String COM2 = "COM2";

	private static final String COM3 = "COM3";

	// private static final String COM4 = "COM4";

	public void contextInitialized(ServletContextEvent arg0) {
		CometContext cc = CometContext.getInstance();
		cc.registChannel(CHANNEL);// 注册应用的channel
		// List<String> portNameList = new ArrayList<String>();
		// portNameList.add(COM1);
		// portNameList.add(COM2);
		// portNameList.add(COM3);
		// Map<String, String> rtnMap = new HashMap<String, String>();
		// for (Iterator<String> iterator = portNameList.iterator(); iterator.hasNext();) {
		// String portName = iterator.next();
		// Thread helloAppModule = new Thread(new HelloAppModule(portName, rtnMap), "Sender App Module");
		// helloAppModule.setDaemon(true);
		// helloAppModule.start();
		// }

		Thread helloAppModule = new Thread(new HelloAppModule(null, null), "Sender App Module");
		helloAppModule.setDaemon(true);
		helloAppModule.start();

	}

	class HelloAppModule implements Runnable {
		private String portName;
		private Map<String, String> map;

		public HelloAppModule(String portName, Map<String, String> map) {
			// this.portName = portName;
			// this.map = map;
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				CometEngine engine = CometContext.getInstance().getEngine();

				// if (COM1.equals(portName)) {
				// long totalMemory = Runtime.getRuntime().totalMemory();
				//
				// // System.out.println("totalMemory: " + totalMemory);
				// map.put(portName, portName + " : " + totalMemory);
				// }
				//
				// if (COM2.equals(portName)) {
				// long freeMemory = Runtime.getRuntime().freeMemory();
				//
				// System.out.println("freeMemory: " + freeMemory);
				// map.put(portName, portName + " : " + freeMemory);
				// }
				//
				// if (COM3.equals(portName)) {
				// long maxMemory = Runtime.getRuntime().maxMemory();
				//
				// System.out.println("maxMemory: " + maxMemory);
				// map.put(portName, portName + " : " + maxMemory);
				// }

				// System.out.println("###: " + Runtime.getRuntime().freeMemory() / 1024);
				// engine.sendToAll(CHANNEL, map);

				HealthDTO healthDto = new HealthDTO();
				long startup = System.currentTimeMillis();
				long totalMemory = Runtime.getRuntime().totalMemory();
				long freeMemory = Runtime.getRuntime().freeMemory();
				long maxMemory = Runtime.getRuntime().maxMemory();
				long usedMemory = totalMemory - freeMemory;
				Integer connectorCount = engine.getConnections().size();
				healthDto.setConnectorCount(connectorCount.toString());
				healthDto.setFreeMemory(freeMemory);
				healthDto.setMaxMemory(maxMemory);
				healthDto.setTotalMemory(totalMemory);
				healthDto.setUsedMemory(usedMemory);
				long dif = System.currentTimeMillis() - startup;
				long day_mill = 86400000;// 一天的毫秒数 60*60*1000*24
				long hour_mill = 3600000;// 一小时的毫秒数 60*60*1000
				Long day = dif / day_mill;
				Long hour = (dif % day_mill) / hour_mill;
				String str = day.toString() + "天 " + hour.toString() + "小时";
				healthDto.setStartup(str);
				System.out.println("freeMemory: " + healthDto.getFreeMemory());
				engine.sendToAll("talker", healthDto.getFreeMemory());
			}
		}

		public String getPortName() {
			return portName;
		}

		public void setPortName(String portName) {
			this.portName = portName;
		}

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}

	}

	public void contextDestroyed(ServletContextEvent arg0) {
	}
}