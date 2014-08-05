package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.Event;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Similar to SendAndReceiveEvents, but customized for testing the UPG-CEP
 * integration.
 * 
 * Sends three Tweet events, and receives nothing.
 * 
 * The target correlator is assumed to contain the related event definitions and
 * monitors (i.e. it is running the UPG-CEP UC1 project).
 */
public class SendAndReceiveUPGCEPUC1Events extends InjectEPL {

	public SendAndReceiveUPGCEPUC1Events() {
	}
	
	public static void main(String[] args) {
		SendAndReceiveUPGCEPUC1Events self = new SendAndReceiveUPGCEPUC1Events();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
//		String host = "localhost"; // local Apama Studio 5.0
		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_SendAndReceiveUPGCEPUC1Events");

//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			// First event: positive tweet
			sendTweetEvent(engineClient, "love", "whenever", "whoever",
					"40.00000", "-3.00000", "33");
			
			sleep(3);
			
			// Second event: negative tweet (within 10 seconds time window)
			sendTweetEvent(engineClient, "hate", "whenever", "whoever",
					"40.00000", "-3.00000", "34");
			
			sleep(3);
			
			// Third event: neutral tweet (within 10 seconds time window)
			sendTweetEvent(engineClient, "dunno", "whenever", "whoever",
					"40.00000", "-3.00000", "35");
			
			sleep(5);
			
			// Fourth event: negative tweet (out of 10 seconds time window)
			// Since more than 10 seconds have passed since the first event, it
			// has already fallen out of the 10 seconds window, and only the
			// latest 3 events remain: 2 negative tweets and 1 neutral tweet.
			sendTweetEvent(engineClient, "more hate", "whenever", "whoever",
					"40.00000", "-3.00000", "36");
			
//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			disconnect(engineClient);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
	protected void sendTweetEvent(EngineClientInterface engineClient,
			String text, String createdAt, String userId, String longitude,
			String latitude, String id) throws EngineException {
		System.out.println("Sending event...");
		
		EventType tweetEventType = new EventType(
				"com.ericsson.cep.twitter.Tweet",
				new Field[] {
						new Field("Text", StringFieldType.TYPE),
						new Field("CreatedAt", StringFieldType.TYPE),
						new Field("UserId", StringFieldType.TYPE),
						new Field("longitude", StringFieldType.TYPE),
						new Field("latitude", StringFieldType.TYPE),
						new Field("id", StringFieldType.TYPE)});
		Event event = new Event(tweetEventType);
		event.setField("Text", text);
		event.setField("CreatedAt", createdAt);
		event.setField("UserId", userId);
		event.setField("longitude", longitude);
		event.setField("latitude", latitude);
		event.setField("id", id);
				
		System.out.println("Event sent: " + event.toString() + " of type "
				+ event.getEventType().toString() + ".");
		
		engineClient.sendEvents(new Event[] {event});
	}
	
}
