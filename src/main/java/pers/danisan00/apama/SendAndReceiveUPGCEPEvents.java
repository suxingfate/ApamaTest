package pers.danisan00.apama;

import java.io.IOException;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.Event;
import com.apama.event.IEventListener;
import com.apama.event.parser.DictionaryFieldType;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.IntegerFieldType;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Similar to SendAndReceiveEvents, but customized for testing the UPG-CEP
 * integration.
 * 
 * Sends a RequestConnectedUPGs event, and receives a ConnectedUPGs event.
 * 
 * The channel into which ConnectedUPG events are emitted is unknown, so it is
 * not possible to listen to it.
 * In order to be able to receive such events, a monitor is injected now that
 * listens for them end emits them in a known channel. The monitor is removed at
 * the end.
 * 
 * The target correlator is assumed to contain the related event definitions and
 * monitors (i.e. it is running the UPG-CEP Integration project).
 */
public class SendAndReceiveUPGCEPEvents extends InjectEPL {

	public SendAndReceiveUPGCEPEvents() {
	}
	
	public static void main(String[] args) {
		SendAndReceiveUPGCEPEvents self = new SendAndReceiveUPGCEPEvents();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
		String host = "localhost"; // local Apama Studio 5.0
//		String host = "10.42.102.194"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15904;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_SendAndReceiveUPGCEPEvents");

//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			injectEpl(engineClient,	"monitors/ConnectedUPGsReceiver.mon");
			
			startListeningForConnectedUPGsEvents(engineClient);
			
			sendRequestConnectedUPGsEvent(engineClient);
			
			sleep(3);
			
			stopListeningForConnectedUPGsEvents(engineClient);
			
			deleteMonitor(engineClient,
					"pers.danisan00.apama.monitors.ConnectedUPGsReceiver");
			
//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
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

	protected void startListeningForConnectedUPGsEvents(
			EngineClientInterface engineClient) throws EngineException {
		System.out.println("Attaching output event listener...");

		EventType connectedUPGsEventType = new EventType(
				"com.ericsson.cep.ConnectedUPGs",
				new Field[] {
						new Field("UPGs", new DictionaryFieldType(
								StringFieldType.TYPE, IntegerFieldType.TYPE))});
		EventParser.getDefaultParser().registerEventType(
				connectedUPGsEventType);
		ConsumerOperationsInterface consumer = engineClient.addConsumer(
				"pers.danisan00.apama.consumers.ConnectedUPGsConsumer",
				new String[] {
						"pers.danisan00.apama.channels.ConnectedUPGsChannel"});
		consumer.addEventListener(new IEventListener() {
			
			@Override
			public void handleEvents(Event[] events) {
				for (Event event : events) {
					handleEvent(event);
				}
			}
			
			@Override
			public void handleEvent(Event event) {
				System.out.println("Output event received: "
						+ event.toString() + " of type "
						+ event.getEventType().toString() + ".");
				
				Event connectedUPGsEvent = (Event) event;
				System.out.println("Connected UPGs: "
						+ connectedUPGsEvent.getField("UPGs"));
			}
			
		});
		
		System.out.println("Output event listener attached.");
	}
	
	protected void stopListeningForConnectedUPGsEvents(
			EngineClientInterface engineClient) throws EngineException {
		System.out.println("Detaching output event listener...");
		
		engineClient.removeConsumer(
				"pers.danisan00.apama.consumers.ConnectedUPGsConsumer");
		
		System.out.println("Output event listener detached.");
	}
	
	protected void sendRequestConnectedUPGsEvent(
			EngineClientInterface engineClient)
			throws EngineException {
		System.out.println("Sending event...");
		
		EventType requestConnectedUPGsEventType = new EventType(
				"com.ericsson.cep.RequestConnectedUPGs",
				new Field[0]);
		Event event = new Event(requestConnectedUPGsEventType);
				
		System.out.println("Event sent: " + event.toString() + " of type "
				+ event.getEventType().toString() + ".");
		
		engineClient.sendEvents(new Event[] {event});
	}
	
}
