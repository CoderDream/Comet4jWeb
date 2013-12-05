package org.comet4j.demo.helloworld;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.comet4j.core.CometContext;
import org.comet4j.core.CometEngine;

public class HelloWorldGoogleCode implements ServletContextListener {
	private static final String CHANNEL = "hello";

	public void contextInitialized(ServletContextEvent arg0) {
		CometContext cc = CometContext.getInstance();
		cc.registChannel(CHANNEL);// 注册应用的channel
		Thread helloAppModule = new Thread(new HelloAppModule(), "Sender App Module");
		helloAppModule.setDaemon(true);
		helloAppModule.start();

	}

	class HelloAppModule implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				CometEngine engine = CometContext.getInstance().getEngine();
				engine.sendToAll(CHANNEL, Runtime.getRuntime().freeMemory() / 1024);
			}
		}
	}

	public void contextDestroyed(ServletContextEvent arg0) {

	}
}