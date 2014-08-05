package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.Event;
import com.apama.event.IEventListener;
import com.apama.event.parser.BooleanFieldType;
import com.apama.event.parser.DictionaryFieldType;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.FloatFieldType;
import com.apama.event.parser.IntegerFieldType;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Evolution of SendAndReceiveUPGCEPEvents.
 * 
 * Sends a RequestStateSummary event, and receives an OutputStateSummary event.
 * 
 * The target correlator is assumed to contain the related event definitions and
 * monitors (i.e. it is running the UPG-CEP UC1 project).
 */
public class SendAndReceiveUPGCEPUC1Events2
		extends SendAndReceiveUPGCEPUC1Events {

	private String stateSummaryConsumerName =
			"pers.danisan00.apama.consumers.StateSummaryConsumer";
	
	private String stateSummaryChannelName =
			"pers.danisan00.apama.channels.StateSummaryChannel";
	
	public SendAndReceiveUPGCEPUC1Events2() {
	}
	
	public static void main(String[] args) {
		SendAndReceiveUPGCEPUC1Events2 self = new SendAndReceiveUPGCEPUC1Events2();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
//		String host = "localhost"; // local Apama Studio 5.0
		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_SendAndReceiveUPGCEPUC1Events2");

//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			startListeningForOutputStateSummaryEvents(engineClient);
			
			sendRequestStateSummaryEvent(engineClient, true, true, true, true,
					true);
			
			sleep(3);
			
			stopListeningForOutputStateSummaryEvents(engineClient);
			
//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			disconnect(engineClient);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
	protected void sendRequestStateSummaryEvent(
			EngineClientInterface engineClient, boolean connectedUPGs,
			boolean networkStatusMonitorConfiguration,
			boolean monitoredUsersCount, boolean monitoredUsers,
			boolean twitterSentiment) throws EngineException {
		System.out.println("Sending event...");
		
		EventType requestStateSummaryEventType = new EventType(
				"com.ericsson.cep.network.RequestStateSummary",
				new Field[] {
						new Field("outputChannel", StringFieldType.TYPE),
						new Field("connectedUPGs", BooleanFieldType.TYPE),
						new Field("networkStatusMonitorConfiguration",
								BooleanFieldType.TYPE),
						new Field("monitoredUsersCount", BooleanFieldType.TYPE),
						new Field("monitoredUsers", BooleanFieldType.TYPE),
						new Field("twitterSentiment", BooleanFieldType.TYPE)});
		Event event = new Event(requestStateSummaryEventType);
		event.setField("outputChannel", stateSummaryChannelName);
		event.setField("connectedUPGs", connectedUPGs);
		event.setField("networkStatusMonitorConfiguration",
				networkStatusMonitorConfiguration);
		event.setField("monitoredUsersCount", monitoredUsersCount);
		event.setField("monitoredUsers", monitoredUsers);
		event.setField("twitterSentiment", twitterSentiment);
				
		System.out.println("Event sent: " + event.toString() + " of type "
				+ event.getEventType().toString() + ".");
		
		engineClient.sendEvents(new Event[] {event});
	}
	
	protected void startListeningForOutputStateSummaryEvents(
			EngineClientInterface engineClient) throws EngineException {
		System.out.println("Attaching output event listener...");

		EventType connectedUPGsEventType = new EventType(
				"com.ericsson.cep.ConnectedUPGs",
				new Field[] {
						new Field("UPGs", new DictionaryFieldType(
								StringFieldType.TYPE, IntegerFieldType.TYPE))});

		EventType networkStatusMonitorConfigurationEventType = new EventType(
				"com.ericsson.cep.network.NetworkStatusMonitorConfiguration",
				new Field[] {
						new Field("timeLimit", IntegerFieldType.TYPE),
						new Field("noOfContexts", IntegerFieldType.TYPE),
						new Field("networkFailsLimit", IntegerFieldType.TYPE)});

		EventType monitoredUserDataEventType = new EventType(
				"com.ericsson.cep.network.MonitoredUserData",
				new Field[] {
						new Field("timeStamp", FloatFieldType.TYPE),
						new Field("contextName", StringFieldType.TYPE)});

		EventType twitterSentimentEventType = new EventType(
				"com.ericsson.cep.twitter.TwitterSentiment",
				new Field[] {
						new Field("currentSentiment", IntegerFieldType.TYPE),
						new Field("positivePcent", FloatFieldType.TYPE),
						new Field("negativePcent", FloatFieldType.TYPE),
						new Field("neutralPcent", FloatFieldType.TYPE)});
		
		EventType stateSummaryEventType = new EventType(
				"com.ericsson.cep.network.StateSummary",
				new Field[] {
						new Field("connectedUPGs", connectedUPGsEventType),
						new Field("networkStatusMonitorConfiguration",
								networkStatusMonitorConfigurationEventType),
						new Field("monitoredUsersCount",
								new DictionaryFieldType(StringFieldType.TYPE,
										IntegerFieldType.TYPE)),
						new Field("monitoredUsers", new DictionaryFieldType(
								StringFieldType.TYPE,
								monitoredUserDataEventType)),
						new Field("twitterSentiment",
								twitterSentimentEventType)});
		EventParser.getDefaultParser().registerEventType(stateSummaryEventType);
		ConsumerOperationsInterface consumer = engineClient.addConsumer(
				stateSummaryConsumerName,
				new String[] {stateSummaryChannelName});
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
				
				Event twitterSentimentEvent =
						(Event) event.getField("twitterSentiment");
				System.out.println("Current Twitter sentiment: "
						+ twitterSentimentEvent.getField("currentSentiment"));
			}
		});
		
		System.out.println("Output event listener attached.");
	}
	
	protected void stopListeningForOutputStateSummaryEvents(
			EngineClientInterface engineClient) throws EngineException {
		System.out.println("Detaching output event listener...");
		
		engineClient.removeConsumer(stateSummaryConsumerName);
		
		System.out.println("Output event listener detached.");
	}
	
}
