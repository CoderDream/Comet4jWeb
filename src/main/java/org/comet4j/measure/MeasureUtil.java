package org.comet4j.measure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.json.JSONObject;

import org.comet4j.core.CometContext;
import org.comet4j.core.CometEngine;

import com.tisco.lkmes.mme.entity.MeasureSite;

public class MeasureUtil implements ServletContextListener {
	private static final String CHANNEL = "measure";

	// private MeasureSiteService measureSiteService;

	CommPortIdentifier portId;
	Enumeration portList;

	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;
	String tempStr1 = "";
	String tempStr2 = "";

	public void contextInitialized(ServletContextEvent arg0) {
		CometContext cc = CometContext.getInstance();
		Map<String, String> rtnMap = new HashMap<String, String>();
		// 注册应用的channel
		cc.registChannel(CHANNEL);

		Map<String, MeasureSite> siteMap = new HashMap<String, MeasureSite>();
		// 获取当前机器所有的COM端口
		portList = CommPortIdentifier.getPortIdentifiers();
		List<MeasureSite> measureSiteList = findAllMeasureSite();
		for (Iterator<MeasureSite> iterator = measureSiteList.iterator(); iterator.hasNext();) {
			MeasureSite measureSite = iterator.next();
			siteMap.put(measureSite.getComPort(), measureSite);
		}

		if (null != measureSiteList) {
			System.out.println("measureSite.size: " + measureSiteList.size());
		}

		// 遍历所有的COM端口
		while (portList.hasMoreElements()) {
			// 取得第一个COM端口
			portId = (CommPortIdentifier) portList.nextElement();
			String portName = portId.getName();
			System.out.println("Port Name:" + portName);
			// 如果COM端口是串口，则执行下面的操作
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL && siteMap.containsKey(portName)) {
				// System.out.println("portId.getName(): " + portId.getName());
				// 如果端口名是对应计量秤的端口，则继续执行下面的操作

				// if (portName.equals("COM4")) {
				// 启动计量线程
				Thread measureAppModule = new Thread(new MeasureAppModule(portName, rtnMap), "Sender App Module " + portName);
				measureAppModule.setDaemon(true);
				measureAppModule.start();
				//break;
				// }

				// if (portName.equals("COM5")) {
				// // 启动计量线程
				// Thread measureAppModule = new Thread(new MeasureAppModule(portName, rtnMap), "Sender App Module " + portName);
				// measureAppModule.setDaemon(true);
				// measureAppModule.start();
				// break;
				// }
			}
		}
	}

	public static List<MeasureSite> findAllMeasureSite() {

		List<MeasureSite> measureSiteList = new ArrayList<MeasureSite>();
		MeasureSite measureSite = null;
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		try {

			// new oracle.jdbc.driver.OracleDriver();
			// jdbc.url=jdbc\:oracle\:thin\:@192.168.72.99\:1521\:orcl
			// jdbc.username=lkmes2
			// jdbc.password=lkmes2

			Properties prop = new Properties();// 属性集合对象
			// FileInputStream fis = new FileInputStream("jdbc.properties");// 属性文件流
			InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties");
			prop.load(fis);// 将属性文件流装载到Properties对象中
			String driverClassName = prop.getProperty("jdbc.driverClassName");
			// jdbc.url=jdbc\:oracle\:thin\:@10.1.11.222\:1521\:orcl
			String url = prop.getProperty("jdbc.url");
			// jdbc.username=lkmes2
			String username = prop.getProperty("jdbc.username");
			// jdbc.password=lkmes2
			String password = prop.getProperty("jdbc.password");

			Class.forName(driverClassName);
			conn = DriverManager.getConnection(url, username, password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from MES_MME_MEASURE_SITE");
			while (rs.next()) {
				System.out.println("#####");
				measureSite = new MeasureSite();
				// SID NUMBER(15) NOT NULL,
				long sid = rs.getLong("sid");
				System.out.println(sid);
				measureSite.setSid(sid);
				// METER_NAME VARCHAR2(40 BYTE),
				String meterName = rs.getString("METER_NAME");
				System.out.println(meterName);
				measureSite.setMeterName(meterName);
				// IP VARCHAR2(40 BYTE),
				String ip = rs.getString("IP");
				System.out.println(ip);
				measureSite.setIp(ip);
				// SITE_NO VARCHAR2(32 BYTE) NOT NULL,
				String siteNo = rs.getString("SITE_NO");
				System.out.println(siteNo);
				measureSite.setIp(ip);
				// SITE_NAME VARCHAR2(64 BYTE) NOT NULL,
				String siteName = rs.getString("SITE_NAME");
				System.out.println(siteName);
				measureSite.setIp(ip);
				// SITE_TYPE VARCHAR2(3 BYTE),
				// ZERO_STANDARD VARCHAR2(10 BYTE),
				// CREATED_BY VARCHAR2(32 BYTE),
				// CREATED_DT DATE,
				// UPDATED_BY VARCHAR2(32 BYTE),
				// UPDATED_DT DATE,
				// VERSION NUMBER(9),
				// COM_PORT VARCHAR2(10 BYTE),
				String comPort = rs.getString("COM_PORT");
				System.out.println(comPort);
				measureSite.setComPort(comPort);
				// COM_FREQUENCY NUMBER(8)
				int comFrequency = rs.getInt("COM_FREQUENCY");
				System.out.println(comFrequency);
				measureSite.setComFrequency(comFrequency);
				measureSiteList.add(measureSite);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return measureSiteList;
	}

	/**
	 * 计量模块，用于读取特定端口的数据
	 * 
	 * @author xulin
	 * 
	 * 
	 */
	class MeasureAppModule implements Runnable, SerialPortEventListener {
		private String comPort;
		private Map<String, String> map;

		public MeasureAppModule(String comPort, Map<String, String> map) {
			try {
				this.comPort = comPort;
				this.map = map;
				serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
				inputStream = serialPort.getInputStream();
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			}
			// 端口已被占用
			catch (PortInUseException e) {
				e.printStackTrace();
			}
			// 读取IO流失败
			catch (IOException e) {
				e.printStackTrace();
			}

			catch (TooManyListenersException e) {
				e.printStackTrace();
			}
			// 不支持的Comm操作异常
			catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 监听COM端口的监听器
		 */
		public void serialEvent(SerialPortEvent event) {
			switch (event.getEventType()) {
			case SerialPortEvent.BI:
			case SerialPortEvent.OE:
			case SerialPortEvent.FE:
			case SerialPortEvent.PE:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.RI:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.DATA_AVAILABLE:
				try {
					// 初始化服务器向浏览器推送消息的引擎实例 TODO
					CometEngine engine = CometContext.getInstance().getEngine();

					// 数据格式为17为，笑脸+90+空格+12位数据+回车换行符
					// 每次读取34个字节，这样可以保证至少可以取到一个完整的数据
					byte[] readBuffer = new byte[34];

					StringBuffer sb = null;
					while (inputStream.available() > 0) {
						// 为了保证每次读取完整的数据，让当前监听器休眠一段时间
						Thread.currentThread().sleep(100);
						sb = new StringBuffer("");
						int numBytes = inputStream.read(readBuffer);
						sb.append(new String(readBuffer).trim());
						// System.out.println("read: size: " + numBytes +
						// "; string:" + new String(readBuffer).trim());
						System.out.println("read: size: " + numBytes + "; string:" + sb);

						String string = new String(readBuffer);
						// 找到数据起始位置
						int i = string.indexOf("=.");
						// System.out.println("index 90: " + i);
						// 如果找到了，而且可以读取接下来的10位数据，则执行下面的取数据操作
						// if (-1 != i && i + 10 < string.length()) {
						// 取标志位后，第5位开始的6位字符
						// String num = string.substring(i + 1, i + 7);
						String num = string.substring(1, 7);
						System.out.println("new num: " + num);
						// 如果数据有变化，才往浏览器端推送新数据
						// if (!tempStr1.equals(num)) {
						// 将数据推送到浏览器
						tempStr1 = num;
						StringBuffer stringBuffer = new StringBuffer(num);
						stringBuffer.reverse();
						Integer number = Integer.parseInt(stringBuffer.toString());

						String rtnString = number.toString();
						System.out.println("push num: " + rtnString);
						map.put(comPort, rtnString);
						System.out.println(comPort + ":" + rtnString);
						System.out.println("map: " + map);
						// engine.sendToAll(CHANNEL, map);

						JSONObject json = JSONObject.fromObject(map);
						String jsonStr = json.toString();
						System.out.println(jsonStr);
						engine.sendToAll(CHANNEL, jsonStr);
						// }

						// }
						// break;
					}
				} catch (Exception e) {
				}
				break;
			}
		}

		public void run() {
		}

		public String getComPort() {
			return comPort;
		}

		public void setComPort(String comPort) {
			this.comPort = comPort;
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

	//
	// public MeasureSiteService getMeasureSiteService() {
	// return measureSiteService;
	// }
	//
	// public void setMeasureSiteService(MeasureSiteService measureSiteService) {
	// this.measureSiteService = measureSiteService;
	// }

	public static void main(String[] args) {

	}
}
