package pers.danisan00.apama;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.Event;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.FloatFieldType;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Class for testing interaction with a remote Progress Apama 4.3 correlator.
 * <br/>
 * This class performs several sequential tasks:
 * <ul>
 *   <li>Connect to a remote correlator.</li>
 *   <li>Inject event definitions and monitors into the remote correlator. These
 *       are read from a pre-packaged jar file generated with Apama Studio.</li>
 *   <li>Attach an output event listener to the remote correlator.</li>
 *   <li>Send two events to the remote correlator.</li>
 *   <li>Wait for 5 seconds. An output event should be received.</li>
 *   <li>Detach the output event listener from the remote correlator.</li>
 *   <li>Remove the previously added event definitions and monitors from the
 *       remote correlator.</li>
 *   <li>Disconnect from the remote correlator.</li>
 * </ul>
 * It is assumed that the remote correlator does not contain the related
 * event definitions and monitors beforehand.
 */
public class InjectJavaApplication extends SendAndReceiveEvents {

	public InjectJavaApplication() {
	}
	
	public static void main(String[] args) {
		InjectJavaApplication self = new InjectJavaApplication();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
		String host = "localhost"; // local Apama Studio 5.0
//		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_InjectJavaApplication");

			dumpEngineInfo(engineClient);
			dumpEngineStatus(engineClient);
			
			injectPackagedJavaApp(engineClient,
					"EventDefinitionsAndMonitors.jar");
			
			startListeningForOutputAlertEvents(engineClient);

			dumpEngineInfo(engineClient);
			dumpEngineStatus(engineClient);
			
			sendNewSensorEvent(engineClient, "S001", 100.0f, 800.0f);
			
			sendTemperatureEvent(engineClient, "S001", 133.3f);
			
			sleep(5);

			stopListeningForOutputAlertEvents(engineClient);
			
			deletePackagedJavaApp(engineClient,
					"pers.danisan00.apama.applications.EventDefinitionsAndMonitors");
			deleteEventDefinition(engineClient,
					"pers.danisan00.apama.eventdefinitions.NewSensor");
			deleteEventDefinition(engineClient,
					"pers.danisan00.apama.eventdefinitions.Temperature");
			deleteEventDefinition(engineClient,
					"pers.danisan00.apama.eventdefinitions.Pressure");
			deleteEventDefinition(engineClient,
					"pers.danisan00.apama.eventdefinitions.Alert");
			
			dumpEngineInfo(engineClient);
			dumpEngineStatus(engineClient);
			
			disconnect(engineClient);
			
			System.out.println("Done.");
			
		} catch (IOException ioe) {
			System.out.println("I/O error: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (CompoundException ce) {
			System.out.println("Apama error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
	protected void injectPackagedJavaApp(EngineClientInterface engineClient,
			String javaAppPath) throws IOException, EngineException {
		System.out.println("Injecting packaged Java app " + javaAppPath
				+ "...");

		InputStream inputStream = null;
		byte[] bytes = new byte[0];
		
		try {
			inputStream = getClass().getResourceAsStream(
					"/packagedJavaApp/" + javaAppPath);
			
			int bufferSize = 1024;
			while (true) {
				byte[] buffer = new byte[bufferSize];
				int bytesRead = inputStream.read(buffer);
				if (bytesRead < 0) {
					break;
				} else if (bytesRead < bufferSize) {
					buffer = Arrays.copyOf(buffer, bytesRead);	
				}
				
				byte[] oldBytes = bytes;
				bytes = new byte[oldBytes.length + bytesRead];
				System.arraycopy(oldBytes, 0, bytes, 0, oldBytes.length);
				System.arraycopy(buffer, 0, bytes, oldBytes.length, bytesRead);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();		
				} catch (IOException ioe) {
					// Intentionally ignored
				}
			}
		}
		
		System.out.println("Packaged Java app injected.");
		
		engineClient.injectJavaApplication(bytes);
	}

	protected void sendNewSensorEvent(EngineClientInterface engineClient,
			String sensorId, float meanTemperature, float meanPressure)
			throws EngineException {
		System.out.println("Sending event...");
		
		EventType newSensorEventType = new EventType(
				"pers.danisan00.apama.eventdefinitions.NewSensor",
				new Field[] {
						new Field("sensorId", StringFieldType.TYPE),
						new Field("meanTemperature", FloatFieldType.TYPE),
						new Field("meanPressure", FloatFieldType.TYPE)});
		Event event = new Event(newSensorEventType);
		event.setField("sensorId", sensorId);
//		event.setField("meanTemperature", (double) meanTemperature);
		event.setField("meanTemperature", floatToDouble(meanTemperature));
//		event.setField("meanPressure", (double) meanPressure);
		event.setField("meanPressure", floatToDouble(meanPressure));
		engineClient.sendEvents(new Event[] {event});
		
		System.out.println("Event sent: " + event.toString() + " of type "
				+ event.getEventType().toString() + ".");
	}
	
	protected void deletePackagedJavaApp(EngineClientInterface engineClient,
			String name) throws EngineException {
		System.out.println("Deleting packaged Java app " + name
				+ "...");
		
		engineClient.deleteName(name, false);
		
		System.out.println("Packaged Java app deleted.");
	}
	
	protected void deleteEventDefinition(EngineClientInterface engineClient,
			String name) throws EngineException {
		System.out.println("Deleting event definition " + name + "...");
		
		engineClient.deleteName(name, false);
		
		System.out.println("Event definition deleted.");
	}
	
}
