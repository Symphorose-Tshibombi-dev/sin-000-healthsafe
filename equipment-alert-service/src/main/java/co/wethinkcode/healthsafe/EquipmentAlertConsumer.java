package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.ConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentAlertConsumer implements AutoCloseable{

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final EquipmentAlertService service;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> finishedMessageIds = ConcurrentHashMap.newKeySet();

    private Connection connection;
    private Session session;


}
