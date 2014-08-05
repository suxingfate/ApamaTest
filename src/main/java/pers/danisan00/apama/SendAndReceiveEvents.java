package pers.danisan00.apama;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.Event;
import com.apama.event.IEventListener;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.parser.Field;
import com.apama.event.parser.FloatFieldType;
import com.apama.event.parser.StringFieldType;
import com.apama.util.CompoundException;

/**
 * Class for testing interaction with a remote Progress Apama 4.3 correlator. <br/>
 * This class performs several sequential tasks:
 * <ul>
 * <li>Connect to a remote correlator.</li>
 * <li>Get status information from the remote correlator.</li>
 * <li>Attach an output event listener to the remote correlator.</li>
 * <li>Send an event to the remote correlator.</li>
 * <li>Wait for 5 seconds. An output event should be received.</li>
 * <li>Detach the output event listener from the remote correlator.</li>
 * <li>Disconnect from the remote correlator.</li>
 * </ul>
 * For the event injection and event listening tasks, it is assumed that the remote correlator already contains the related event definitions and monitors (i.e.
 * it is running one of the applications described in the Apama Studio 5.0 tutorials "Developing Applications in MonitorScript" or "Developing Applications in
 * Java").
 */
public class SendAndReceiveEvents extends BasicTest {

    public SendAndReceiveEvents() {
    }

    public static void main(String[] args) {
        SendAndReceiveEvents self = new SendAndReceiveEvents();
        self.runTest();
    }

    private void runTest() {
        // String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
        String host = "localhost"; // local Apama Studio 5.0
        // String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
        int port = 15903;

        try {
            EngineClientInterface engineClient = connect(host, port, "Apama43Tests_SendAndReceiveEvents");

            startListeningForOutputAlertEvents(engineClient);

            dumpEngineInfo(engineClient);
            dumpEngineStatus(engineClient);

            sendTemperatureEvent(engineClient, "S001", 133.3f);

            sleep(5);

            stopListeningForOutputAlertEvents(engineClient);

            dumpEngineInfo(engineClient);
            dumpEngineStatus(engineClient);

            disconnect(engineClient);

            System.out.println("Done.");

        } catch (CompoundException ce) {
            System.out.println("Error: " + ce.getMessage());
            ce.printStackTrace();
        }
    }

    protected void startListeningForOutputAlertEvents(EngineClientInterface engineClient) throws EngineException {
        System.out.println("Attaching output event listener...");

        EventType alertEventType = new EventType("pers.danisan00.apama.eventdefinitions.Alert", new Field[] { new Field("type", StringFieldType.TYPE),
                new Field("sensorId", StringFieldType.TYPE), new Field("temperature", FloatFieldType.TYPE), new Field("pressure", FloatFieldType.TYPE) });
        EventParser.getDefaultParser().registerEventType(alertEventType);
        ConsumerOperationsInterface consumer = engineClient.addConsumer("pers.danisan00.apama.consumers.AlertConsumer",
                new String[] { "pers.danisan00.apama.channels.AlertChannel" });
        consumer.addEventListener(new IEventListener() {

            @Override
            public void handleEvents(Event[] events) {
                for (Event event : events) {
                    handleEvent(event);
                }
            }

            @Override
            public void handleEvent(Event event) {
                System.out.println("Output event received: " + event.toString() + " of type " + event.getEventType().toString() + ".");
            }

        });

        System.out.println("Output event listener attached.");
    }

    protected void stopListeningForOutputAlertEvents(EngineClientInterface engineClient) throws EngineException {
        System.out.println("Detaching output event listener...");

        engineClient.removeConsumer("pers.danisan00.apama.consumers.AlertConsumer");

        System.out.println("Output event listener detached.");
    }

    protected void sendTemperatureEvent(EngineClientInterface engineClient, String sensorId, float temperature) throws EngineException {
        System.out.println("Sending event...");

        EventType temperatureEventType = new EventType("pers.danisan00.apama.eventdefinitions.Temperature", new Field[] {
                new Field("sensorId", StringFieldType.TYPE), new Field("temperature", FloatFieldType.TYPE) });
        Event event = new Event(temperatureEventType);
        event.setField("sensorId", sensorId);
        // event.setField("temperature", (double) temperature);
        event.setField("temperature", floatToDouble(temperature));

        System.out.println("Event sent: " + event.toString() + " of type " + event.getEventType().toString() + ".");

        engineClient.sendEvents(new Event[] { event });
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
