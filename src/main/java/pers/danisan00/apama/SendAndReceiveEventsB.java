package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.EngineManagement;
import com.apama.event.Event;
import com.apama.event.EventConsumer;
import com.apama.event.EventSupplier;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.FloatFieldType;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Same as SendAndReceiveEvents, but using a different part of the Apama 4.3
 * API.
 * <br/>
 * In this implementation, EngineManagement is used instead of
 * EngineClientInterface as the main interface for the remote correlator.
 */
public class SendAndReceiveEventsB extends BasicTestB {

	public SendAndReceiveEventsB() {
	}
	
	public static void main(String[] args) {
		SendAndReceiveEventsB self = new SendAndReceiveEventsB();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
		String host = "localhost"; // local Apama Studio 5.0
//		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineManagement engineMgmt = connect(host, port,
					"Apama43Tests_SendAndReceiveEventsB");

			EventSupplier eventSupplier = startListeningForOutputAlertEvents(
					engineMgmt);

			dumpEngineInfo(engineMgmt);
			dumpEngineStatus(engineMgmt);
			
			sendTemperatureEvent(engineMgmt, "S001", 133.3f);
			
			sleep(5);
			
			stopListeningForOutputAlertEvents(eventSupplier);
			
			dumpEngineInfo(engineMgmt);
			dumpEngineStatus(engineMgmt);
			
			disconnect(engineMgmt);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
	protected EventSupplier startListeningForOutputAlertEvents(
			EngineManagement engineMgmt) throws EngineException {
		System.out.println("Attaching output event listener...");

		EventType alertEventType = new EventType(
				"pers.danisan00.apama.eventdefinitions.Alert",
				new Field[] {
						new Field("type", StringFieldType.TYPE),
						new Field("sensorId", StringFieldType.TYPE),
						new Field("temperature", FloatFieldType.TYPE),
						new Field("pressure", FloatFieldType.TYPE)});
		EventParser.getDefaultParser().registerEventType(alertEventType);
		EventConsumer consumer = new EventConsumer() {

			@Override
			public void sendEvents(Event[] events) throws EngineException {
				for (Event event : events) {
					System.out.println("Output event received: "
							+ event.toString() + " of type "
							+ event.getEventType().toString() + ".");
				}
			}
			
		};
		EventSupplier eventSupplier = engineMgmt.connectEventConsumer(
				consumer, new String[] {
						"pers.danisan00.apama.channels.AlertChannel"});
		
		System.out.println("Output event listener attached.");
		
		return eventSupplier;
	}
	
	protected void stopListeningForOutputAlertEvents(
			EventSupplier eventSupplier) throws EngineException {
		System.out.println("Detaching output event listener...");
		
		eventSupplier.disconnect();
		
		System.out.println("Output event listener detached.");
	}
	
	protected void sendTemperatureEvent(EngineManagement engineMgmt,
			String sensorId, float temperature)
	throws EngineException {
		System.out.println("Sending event...");
		
		EventType temperatureEventType = new EventType(
				"pers.danisan00.apama.eventdefinitions.Temperature",
				new Field[] {
						new Field("sensorId", StringFieldType.TYPE),
						new Field("temperature", FloatFieldType.TYPE)});
		Event event = new Event(temperatureEventType);
		event.setField("sensorId", sensorId);
//		event.setField("temperature", (double) temperature);
		event.setField("temperature", floatToDouble(temperature));
				
		System.out.println("Event sent: " + event.toString() + " of type "
				+ event.getEventType().toString() + ".");
		
		engineMgmt.sendEvents(new Event[] {event});
	}
	
	protected void sleep(int seconds) {
		System.out.println("Waiting for " + seconds + " seconds...");
		
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		
		System.out.println("Wait time is over.");
	}
	
	// Quick workaround for the float-to-double conversion precision issue.
	// See http://stackoverflow.com/questions/916081/convert-float-to-double-without-losing-precision
	protected double floatToDouble(float value) {
		return Double.parseDouble(Float.toString(value));
	}
	
}
