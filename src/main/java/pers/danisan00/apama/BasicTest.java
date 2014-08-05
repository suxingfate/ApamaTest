package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.EngineInfo;
import com.apama.engine.EngineStatus;
import com.apama.engine.NamedAggregateInfo;
import com.apama.engine.NamedContextInfo;
import com.apama.engine.NamedEventTypeInfo;
import com.apama.engine.NamedJavaApplicationInfo;
import com.apama.engine.NamedMonitorInfo;
import com.apama.engine.NamedTimerInfo;
import com.apama.engine.beans.EngineClientBean;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.util.CompoundException;

/**
 * Class for testing interaction with a remote Progress Apama 4.3 correlator.
 * <br/>
 * This class performs several sequential tasks:
 * <ul>
 *   <li>Connect to a remote correlator.</li>
 *   <li>Get status information from the remote correlator.</li>
 *   <li>Detach the output event listener from the remote correlator.</li>
 *   <li>Disconnect from the remote correlator.</li>
 * </ul>
 */
public class BasicTest {

	public BasicTest() {
	}
	
	public static void main(String[] args) {
		BasicTest self = new BasicTest();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
		String host = "localhost"; // local Apama Studio 5.0
//		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_BasicTest");

			dumpEngineInfo(engineClient);
			dumpEngineStatus(engineClient);
			
			disconnect(engineClient);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
	protected EngineClientInterface connect(String host, int port,
			String processName) throws CompoundException {
		System.out.println("Connecting to remote correlator...");
		
		EngineClientInterface engineClient = new EngineClientBean();
		engineClient.setHost(host);
		engineClient.setPort(port);
		engineClient.setProcessName(processName);
		engineClient.connectNow();

		System.out.println("Connected.");
		
		return engineClient;
	}

	protected void disconnect(EngineClientInterface engineClient)
			throws CompoundException {
		System.out.println("Disconnecting...");

		engineClient.disconnect();
		
		System.out.println("Disconnected.");
	}
	
	protected void dumpEngineInfo(EngineClientInterface engineClient)
			throws EngineException {
		EngineInfo engineInfo = engineClient.getRemoteEngineInfo();
		if (engineInfo == null) {
			return;
		}
		
		System.out.println();
		System.out.println("Remote correlator info dump (start)");

		System.out.println("* Number of contexts: "
				+ engineInfo.getNumContexts());
		NamedContextInfo[] contextInfos = engineInfo.getContexts();
		for (int i = 0; i < contextInfos.length; i++) {
			System.out.println("    Context " + (i + 1) + " of "
					+ contextInfos.length + ": "
					+ contextInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("* Number of Java applications: "
				+ engineInfo.getNumJavaApplications());
		NamedJavaApplicationInfo[] javaAppInfos =
				engineInfo.getJavaApplications();
		for (int i = 0; i < javaAppInfos.length; i++) {
			System.out.println("    Java application " + (i + 1) + " of "
					+ javaAppInfos.length + ": "
					+ javaAppInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("* Number of monitors: "
				+ engineInfo.getNumMonitors());
		NamedMonitorInfo[] monitorInfos = engineInfo.getMonitors();
		for (int i = 0; i < monitorInfos.length; i++) {
			System.out.println("    Monitor " + (i + 1) + " of "
					+ monitorInfos.length + ": "
					+ monitorInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("* Number of event types: "
				+ engineInfo.getNumEventTypes());
		NamedEventTypeInfo[] eventTypeInfos =
				engineInfo.getEventTypes();
		for (int i = 0; i < eventTypeInfos.length; i++) {
			System.out.println("    Event type " + (i + 1) + " of "
					+ eventTypeInfos.length + ": "
					+ eventTypeInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("* Number of timers: "
				+ engineInfo.getNumTimers());
		NamedTimerInfo[] timerInfos = engineInfo.getTimers();
		for (int i = 0; i < timerInfos.length; i++) {
			System.out.println("    Timer " + (i + 1) + " of "
					+ timerInfos.length + ": "
					+ timerInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("* Number of aggregate functions: "
				+ engineInfo.getNumAggregates());
		NamedAggregateInfo[] aggregateInfos = engineInfo.getAggregates();
		for (int i = 0; i < aggregateInfos.length; i++) {
			System.out.println("    Aggregate function " + (i + 1) + " of "
					+ aggregateInfos.length + ": "
					+ aggregateInfos[i].getFullyQualifiedName());
		}
		
		System.out.println("Remote correlator info dump (end)");
		System.out.println();
	}
	
	protected void dumpEngineStatus(EngineClientInterface engineClient)
			throws EngineException {
		EngineStatus engineStatus = engineClient.getRemoteStatus();
		if (engineStatus == null) {
			return;
		}

		System.out.println();
		System.out.println("Remote correlator status dump (start)");
		
		String engineStatusStr = engineStatus.toString();
		engineStatusStr = "* " + engineStatusStr;
		engineStatusStr = engineStatusStr.replaceAll("\n", "\n* ");
		engineStatusStr = engineStatusStr.substring(0,
				engineStatusStr.length() - 3);
		System.out.println(engineStatusStr);
		
		System.out.println("Remote correlator status dump (end)");
		System.out.println();
	}
	
}
