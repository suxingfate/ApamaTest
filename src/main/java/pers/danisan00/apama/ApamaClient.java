package pers.danisan00.apama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.apama.EngineException;
import com.apama.engine.EngineInfo;
import com.apama.engine.EngineStatus;
import com.apama.engine.MonitorScript;
import com.apama.engine.NamedAggregateInfo;
import com.apama.engine.NamedContextInfo;
import com.apama.engine.NamedEventTypeInfo;
import com.apama.engine.NamedJavaApplicationInfo;
import com.apama.engine.NamedMonitorInfo;
import com.apama.engine.NamedTimerInfo;
import com.apama.engine.beans.EngineClientBean;
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

public class ApamaClient {

    private static final Logger logger = Logger.getLogger(ApamaClient.class);

    public ApamaClient(String host, int port, String processName) throws CompoundException {

        this.connect(host, port, processName);
        // logger.info("ApamaClient is crated.");
    }

    EngineClientInterface engineClient = new EngineClientBean();

    public void connect(String host, int port, String processName) throws CompoundException {
        engineClient.setHost(host);
        engineClient.setPort(port);
        engineClient.setProcessName(processName);
        engineClient.connectNow();

        logger.info("ApamaClient is connected.");
    }

    protected void disconnect() throws CompoundException {
        // logger.info("Disconnecting...");

        engineClient.disconnect();

        logger.info("ApamaClient is disconnected.");
    }

    protected void dumpEngineInfo() throws EngineException {
        EngineInfo engineInfo = engineClient.getRemoteEngineInfo();
        if (engineInfo == null) {
            return;
        }

        // logger.info();
        // logger.info("Remote correlator info dump (start)");
        logger.info("Remote correlator info dump (start).");

        logger.info("* Number of contexts: " + engineInfo.getNumContexts());
        NamedContextInfo[] contextInfos = engineInfo.getContexts();
        for (int i = 0; i < contextInfos.length; i++) {
            logger.info("    Context " + (i + 1) + " of " + contextInfos.length + ": " + contextInfos[i].getFullyQualifiedName());
        }

        logger.info("* Number of Java applications: " + engineInfo.getNumJavaApplications());
        NamedJavaApplicationInfo[] javaAppInfos = engineInfo.getJavaApplications();
        for (int i = 0; i < javaAppInfos.length; i++) {
            logger.info("    Java application " + (i + 1) + " of " + javaAppInfos.length + ": " + javaAppInfos[i].getFullyQualifiedName());
        }

        logger.info("* Number of monitors: " + engineInfo.getNumMonitors());
        NamedMonitorInfo[] monitorInfos = engineInfo.getMonitors();
        for (int i = 0; i < monitorInfos.length; i++) {
            logger.info("    Monitor " + (i + 1) + " of " + monitorInfos.length + ": " + monitorInfos[i].getFullyQualifiedName());
        }

        logger.info("* Number of event types: " + engineInfo.getNumEventTypes());
        NamedEventTypeInfo[] eventTypeInfos = engineInfo.getEventTypes();
        for (int i = 0; i < eventTypeInfos.length; i++) {
            logger.info("    Event type " + (i + 1) + " of " + eventTypeInfos.length + ": " + eventTypeInfos[i].getFullyQualifiedName());
        }

        logger.info("* Number of timers: " + engineInfo.getNumTimers());
        NamedTimerInfo[] timerInfos = engineInfo.getTimers();
        for (int i = 0; i < timerInfos.length; i++) {
            logger.info("    Timer " + (i + 1) + " of " + timerInfos.length + ": " + timerInfos[i].getFullyQualifiedName());
        }

        logger.info("* Number of aggregate functions: " + engineInfo.getNumAggregates());
        NamedAggregateInfo[] aggregateInfos = engineInfo.getAggregates();
        for (int i = 0; i < aggregateInfos.length; i++) {
            logger.info("    Aggregate function " + (i + 1) + " of " + aggregateInfos.length + ": " + aggregateInfos[i].getFullyQualifiedName());
        }

        logger.info("Remote correlator info dump (end)");
    }

    protected void dumpEngineStatus() throws EngineException {
        EngineStatus engineStatus = engineClient.getRemoteStatus();
        if (engineStatus == null) {
            return;
        }

        logger.info("Remote correlator status dump (start)");

        String engineStatusStr = engineStatus.toString();
        engineStatusStr = "* " + engineStatusStr;
        engineStatusStr = engineStatusStr.replaceAll("\n", "\n* ");
        engineStatusStr = engineStatusStr.substring(0, engineStatusStr.length() - 3);
        logger.info(engineStatusStr);

        logger.info("Remote correlator status dump (end)");
    }

    protected void sendTemperatureEvent(String sensorId, float temperature) throws EngineException {

        EventType temperatureEventType = new EventType("pers.danisan00.apama.eventdefinitions.Temperature", new Field[] {
                new Field("sensorId", StringFieldType.TYPE), new Field("temperature", FloatFieldType.TYPE) });
        Event event = new Event(temperatureEventType);
        event.setField("sensorId", sensorId);
        // event.setField("temperature", (double) temperature);
        event.setField("temperature", floatToDouble(temperature));

        engineClient.sendEvents(new Event[] { event });
    }

    protected void sendTemperatureEvents(String sensorId, float temperature) throws EngineException, InterruptedException {

        EventType temperatureEventType = new EventType("pers.danisan00.apama.eventdefinitions.Temperature", new Field[] {
                new Field("sensorId", StringFieldType.TYPE), new Field("temperature", FloatFieldType.TYPE) });
        Event event = new Event(temperatureEventType);
        event.setField("sensorId", sensorId);
        // event.setField("temperature", (double) temperature);
        event.setField("temperature", floatToDouble(temperature));

        engineClient.sendEvents(new Event[] { event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event,
                event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event,
                event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event,
                event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event, event,
                event, event, event, event });
        engineClient.flushEvents();
    }

    protected void sendSensorEvent(String sensorId, float meanTemperature) throws EngineException {
        logger.info("Sending Sensor event...");

        EventType sensorEventType = new EventType("pers.danisan00.apama.eventdefinitions.NewSensor", new Field[] { new Field("sensorId", StringFieldType.TYPE),
                new Field("meanTemperature", FloatFieldType.TYPE), new Field("meanPressure", FloatFieldType.TYPE) });
        Event event = new Event(sensorEventType);
        event.setField("sensorId", sensorId);
        // event.setField("temperature", (double) temperature);
        event.setField("meanTemperature", floatToDouble(meanTemperature));
        event.setField("meanPressure", floatToDouble(meanTemperature));

        logger.info("Event sent: " + event.toString() + " of type " + event.getEventType().toString() + ".");

        engineClient.sendEvents(new Event[] { event });
    }

    protected double floatToDouble(float value) {
        return Double.parseDouble(Float.toString(value));
    }

    protected void startListeningForOutputAlertEvents() throws EngineException {
        logger.info("Attaching output event listener...");

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
                logger.debug("Output event received: " + event.toString() + " of type " + event.getEventType().toString() + ".");
            }

        });

        logger.info("Output event listener attached.");
    }

    protected void stopListeningForOutputAlertEvents() throws EngineException {
        logger.info("Detaching output event listener...");

        engineClient.removeConsumer("pers.danisan00.apama.consumers.AlertConsumer");

        logger.info("Output event listener detached.");
    }

    // injectEpl("eventdefinitions/SensorMonitorEvents.mon");
    protected void injectEpl(String eplFilePath) throws IOException, EngineException {
        logger.info("Injecting EPL file " + eplFilePath + "...");

        BufferedReader reader = null;
        StringBuilder eplBuilder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/EPL/" + eplFilePath)));
            String aLine;
            while ((aLine = reader.readLine()) != null) {
                eplBuilder.append(aLine);
                eplBuilder.append("\n");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    // Intentionally ignored
                }
            }
        }

        logger.info("EPL file injected.");

        MonitorScript epl = new MonitorScript(eplBuilder.toString());
        engineClient.injectMonitorScript(epl);
    }

    protected void deleteMonitor(String name) throws EngineException {
        logger.info("Deleting monitor " + name + "...");

        engineClient.deleteName(name, false);

        logger.info("Monitor deleted.");
    }

    protected void deleteEventDefinition(String name) throws EngineException {
        logger.info("Deleting event definition " + name + "...");

        engineClient.deleteName(name, false);

        logger.info("Event definition deleted.");
    }
}
