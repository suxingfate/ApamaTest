package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.EngineInfo;
import com.apama.engine.EngineManagement;
import com.apama.engine.EngineManagementFactory;
import com.apama.engine.EngineStatus;
import com.apama.engine.NamedAggregateInfo;
import com.apama.engine.NamedContextInfo;
import com.apama.engine.NamedEventTypeInfo;
import com.apama.engine.NamedJavaApplicationInfo;
import com.apama.engine.NamedMonitorInfo;
import com.apama.engine.NamedTimerInfo;
import com.apama.util.CompoundException;

/**
 * Same as BasicTest, but using a different part of the Apama 4.3 API.
 * <br/>
 * In this implementation, EngineManagement is used instead of
 * EngineClientInterface as the main interface for the remote correlator.
 */
public class BasicTestB {

	public BasicTestB() {
	}
	
	public static void main(String[] args) {
		BasicTestB self = new BasicTestB();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
		String host = "localhost"; // local Apama Studio 5.0
//		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineManagement engineMgmt = connect(host, port,
					"Apama43Tests_BasicTestB");

			dumpEngineInfo(engineMgmt);
			dumpEngineStatus(engineMgmt);
			
			disconnect(engineMgmt);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}

	protected EngineManagement connect(String host, int port,
			String processName) throws CompoundException {
		System.out.println("Connecting to remote correlator...");
		
		EngineManagement engineMgmt = EngineManagementFactory.connectToEngine(
				host, port, processName);

		System.out.println("Connected.");
		
		return engineMgmt;
	}

	protected void disconnect(EngineManagement engineMgmt)
			throws CompoundException {
		System.out.println("Disconnecting...");

		engineMgmt.disconnect();
		
		System.out.println("Disconnected.");
	}
	
	protected void dumpEngineInfo(EngineManagement engineMgmt)
			throws EngineException {
		EngineInfo engineInfo = engineMgmt.inspectEngine();
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
	
	protected void dumpEngineStatus(EngineManagement engineMgmt)
			throws EngineException {
		EngineStatus engineStatus = engineMgmt.getStatus();
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
