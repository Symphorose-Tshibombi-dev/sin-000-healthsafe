package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.ConnectionFactory;

public class JmsAlertLevelPublisher implements AlertLevelPublisher{

    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final ObjectMapper mapper = new ObjectMapper();

    public JmsAlertLevelPublisher(ConnectionFactory connectionFactory, String topicName) {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
    }

}
