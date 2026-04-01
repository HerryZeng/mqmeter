/*
 * Copyright 2019 JoseLuisSR
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package co.signal.mqmeter;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;

import static com.ibm.mq.constants.CMQC.*;

/**
 * This class is to put and get message on WebSphere MQ queue.
 *
 * @author JoseLuisSR
 * @see "https://github.com/JoseLuisSR/mqmeter"
 * @since 01/13/2019
 */
public class MQClientSampler extends AbstractJavaSamplerClient {

    public static final String SETUP_TEST_ERROR_MESSAGE = "setupTest {} {}";

    public static final String CLOSING_QUEUE_MESSAGE = "Closing queue {}";

    public static final String DONE = "Done!";

    private Logger log;

    /**
     * Parameter for setting the MQ Manager.
     */
    private static final String PARAMETER_MQ_MANAGER = "mq_manager";

    /**
     * Parameter for setting MQ QUEUE to put message, could be LOCAL or REMOTE.
     */
    private static final String PARAMETER_MQ_QUEUE_PUT = "mq_queue_put";

    /**
     * Parameter for setting MQ QUEUE for get message, could be LOCAL or REMOTE.
     */
    private static final String PARAMETER_MQ_QUEUE_GET = "mq_queue_get";

    /**
     * Parameter for setting correlate response message with request message.
     */
    private static final String PARAMETER_MQ_CORRELATE_MSG = "mq_correlate_msg";

    /**
     * Constant to correlate response message with messageID.
     */
    private static final String MESSAGE_ID = "messageId";

    /**
     * Constant to correlate response message with correlationID.
     */
    private static final String CORRELATION_ID = "correlationId";

    /**
     * Parameter for setting MQ Hostname where MQ Server is deploying.
     */
    private static final String PARAMETER_MQ_HOSTNAME = "mq_hostname";

    /**
     * Parameter for setting MQ Channel, it should be server connection channel.
     */
    private static final String PARAMETER_MQ_CHANNEL = "mq_channel";

    /**
     * Parameter for setting MQ USER ID.
     */
    private static final String PARAMETER_MQ_USER_ID = "mq_user_id";

    /**
     * Parameter for setting MQ User password.
     */
    private static final String PARAMETER_MQ_USER_PASSWORD = "mq_user_password";

    /**
     * Parameter for setting MQ PORT, is the Listener port.
     */
    private static final String PARAMETER_MQ_PORT = "mq_port";

    /**
     * Parameter for setting MQ Message.
     */
    private static final String PARAMETER_MQ_MESSAGE = "mq_message";

    /**
     * Parameter for setting MQ Encoding Message.
     */
    private static final String PARAMETER_MQ_ENCODING_MESSAGE = "mq_encoding_message";

    /**
     * Parameter for setting MQ Message type like byte, string, object.
     */
    private static final String PARAMETER_MQ_MESSAGE_TYPE = "mq_message_type";

    /**
     * Parameter to set wait interval to get message on queue.
     */
    private static final String PARAMETER_MQ_WAIT_INTERVAL = "mq_wait_interval";

    /**
     * Parameter for setting MQ Message Format.
     */
    private static final String PARAMETER_MQ_MESSAGE_FORMAT = "mq_message_format";

    /**
     * MQ Manager variable
     */
    public static final String MQ_MESSAGE_FORMAT = "${MQ_MESSAGE_FORMAT}";

    /**
     * Parameter for enabling SSL connection.
     */
    private static final String PARAMETER_MQ_SSL_ENABLE = "mq_ssl_enable";

    /**
     * Parameter for SSL keystore path.
     */
    private static final String PARAMETER_MQ_SSL_KEYSTORE_PATH = "mq_ssl_keystore_path";

    /**
     * Parameter for SSL keystore password.
     */
    private static final String PARAMETER_MQ_SSL_KEYSTORE_PASSWORD = "mq_ssl_keystore_password";

    /**
     * Parameter for SSL truststore path.
     */
    private static final String PARAMETER_MQ_SSL_TRUSTSTORE_PATH = "mq_ssl_truststore_path";

    /**
     * Parameter for SSL truststore password.
     */
    private static final String PARAMETER_MQ_SSL_TRUSTSTORE_PASSWORD = "mq_ssl_truststore_password";

    /**
     * Parameter for SSL cipher suite (algorithm).
     */
    private static final String PARAMETER_MQ_SSL_CIPHER_SUITE = "mq_ssl_cipher_suite";

    /**
     * Parameter for encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Default message type constant.
     */
    private static final String DEFAULT_MESSAGE_TYPE = "Byte";

    /**
     * MQ Manager variable
     */
    public static final String MQ_MANAGER = "${MQ_MANAGER}";

    /**
     * MQ Queue for publishing
     */
    public static final String MQ_QUEUE_PUT = "${MQ_QUEUE_PUT}";

    /**
     * Generic Empty value
     */
    public static final String EMPTY = "";

    /**
     * MQ Server Hostname
     */
    public static final String MQ_HOSTNAME = "${MQ_HOSTNAME}";

    /**
     * MQ Server Port
     */
    public static final String MQ_PORT = "${MQ_PORT}";

    /**
     * MQ Channel
     */
    public static final String MQ_CHANNEL = "${MQ_CHANNEL}";

    /**
     * MQ Encoding Message
     */
    public static final String MQ_ENCODING_MESSAGE = "${MQ_ENCODING_MESSAGE}";

    /**
     * Message Payload to send
     */
    public static final String MQ_MESSAGE = "${MQ_MESSAGE}";

    /**
     * Message Headers to send
     */
    private static final String PARAMETER_MQ_HEADER = "mq_headers";

    /**
     * Message Payload to send
     */
    public static final String MQ_HEADER = "${MQ_HEADER}";

    /**
     * MQQueueManager variable.
     */
    private MQQueueManager mqMgr;

    /**
     * MQQueue request variable.
     */
    private MQQueue mqQueuePut;

    /**
     * MQQueue response variable.
     */
    private MQQueue mqQueueGet;

    /**
     * Encoding message variable.
     */
    private String encodingMessage;

    /**
     * Message type variable
     */
    private String messageType;

    /**
     * Initial values for test parameter. They are show in Java Request test sampler.
     *
     * @return Arguments to set as default.
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameter = new Arguments();
        defaultParameter.addArgument(PARAMETER_MQ_MANAGER, MQ_MANAGER);
        defaultParameter.addArgument(PARAMETER_MQ_QUEUE_PUT, MQ_QUEUE_PUT);
        defaultParameter.addArgument(PARAMETER_MQ_QUEUE_GET, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_CORRELATE_MSG, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_WAIT_INTERVAL, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_HOSTNAME, MQ_HOSTNAME);
        defaultParameter.addArgument(PARAMETER_MQ_PORT, MQ_PORT);
        defaultParameter.addArgument(PARAMETER_MQ_CHANNEL, MQ_CHANNEL);
        defaultParameter.addArgument(PARAMETER_MQ_USER_ID, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_USER_PASSWORD, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_MESSAGE_TYPE, DEFAULT_MESSAGE_TYPE);
        defaultParameter.addArgument(PARAMETER_MQ_ENCODING_MESSAGE, MQ_ENCODING_MESSAGE);
        defaultParameter.addArgument(PARAMETER_MQ_MESSAGE, MQ_MESSAGE);
        defaultParameter.addArgument(PARAMETER_MQ_HEADER, MQ_HEADER);
        defaultParameter.addArgument(PARAMETER_MQ_MESSAGE_FORMAT, MQ_MESSAGE_FORMAT);
        defaultParameter.addArgument(PARAMETER_MQ_SSL_ENABLE, "false");
        defaultParameter.addArgument(PARAMETER_MQ_SSL_KEYSTORE_PATH, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_SSL_KEYSTORE_PASSWORD, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_SSL_TRUSTSTORE_PATH, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_SSL_TRUSTSTORE_PASSWORD, EMPTY);
        defaultParameter.addArgument(PARAMETER_MQ_SSL_CIPHER_SUITE, EMPTY);
        return defaultParameter;
    }

    /**
     * Read the test parameter and initialize your test client.
     *
     * @param context to get the arguments values on Java Sampler.
     */
    @Override
    public void setupTest(JavaSamplerContext context) {
        log = getNewLogger();

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(HOST_NAME_PROPERTY, context.getParameter(PARAMETER_MQ_HOSTNAME));
        properties.put(PORT_PROPERTY, Integer.parseInt(context.getParameter(PARAMETER_MQ_PORT)));
        properties.put(CHANNEL_PROPERTY, context.getParameter(PARAMETER_MQ_CHANNEL));

        String userID = context.getParameter(PARAMETER_MQ_USER_ID);
        if (userID != null && !userID.isEmpty()) {
            properties.put(USER_ID_PROPERTY, userID);
        }

        String password = context.getParameter(PARAMETER_MQ_USER_PASSWORD);
        if (password != null && !password.isEmpty()) {
            properties.put(PASSWORD_PROPERTY, password);
        }

        // SSL Configuration
        String sslEnable = context.getParameter(PARAMETER_MQ_SSL_ENABLE);
        if (sslEnable != null && sslEnable.equalsIgnoreCase("true")) {
            String keystorePath = context.getParameter(PARAMETER_MQ_SSL_KEYSTORE_PATH);
            String keystorePassword = context.getParameter(PARAMETER_MQ_SSL_KEYSTORE_PASSWORD);
            String truststorePath = context.getParameter(PARAMETER_MQ_SSL_TRUSTSTORE_PATH);
            String truststorePassword = context.getParameter(PARAMETER_MQ_SSL_TRUSTSTORE_PASSWORD);
            String cipherSuite = context.getParameter(PARAMETER_MQ_SSL_CIPHER_SUITE);


            // Set system properties for SSL
            if (keystorePath != null && !keystorePath.isEmpty()) {
                System.setProperty("javax.net.ssl.keyStore", keystorePath);
                if (keystorePassword != null && !keystorePassword.isEmpty()) {
                    System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
                }
            }
            if (truststorePath != null && !truststorePath.isEmpty()) {
                System.setProperty("javax.net.ssl.trustStore", truststorePath);
                if (truststorePassword != null && !truststorePassword.isEmpty()) {
                    System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
                }
            }

            // Set SSL cipher suite if provided
            if (cipherSuite != null && !cipherSuite.isEmpty()) {
                properties.put(SSL_CIPHER_SUITE_PROPERTY, cipherSuite);
            } else {
                System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
                System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
                System.setProperty("jdk.tls.server.protocols", "TLSv1.2,TLSv1.3");
            }

            log.info("SSL enabled for MQ connection");
            log.info("SSL_KEYSTORE_PATH: {}", keystorePath);
            log.info("KEYSTORE_PASSWORD: {}", keystorePassword);
            log.info("TRUSTSTORE_PATH: {}", truststorePath);
            log.info("TRUSTSTORE_PASSWORD: {}", truststorePassword);
            log.info("CIPHER_SUITE: {}", cipherSuite);

            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String[] cipherSuites = sslContext.getSupportedSSLParameters().getCipherSuites();
            boolean containsCipherSuite = Arrays.asList(cipherSuites).contains(cipherSuite);

            if (containsCipherSuite) {
                log.info("{} is supported.", cipherSuite);
            } else {
                log.info("{} is not supported.", cipherSuite);
                log.info("Supported Cipher Suites as below:");
                for (String suite : cipherSuites) {
                    log.info(suite);
                }
            }
        }

        encodingMessage = context.getParameter(PARAMETER_MQ_ENCODING_MESSAGE);
        messageType = context.getParameter(PARAMETER_MQ_MESSAGE_TYPE);

        log.info("MQ Manager properties are hostname: {}  port: {} channel: {}  user: {}",
                properties.get(HOST_NAME_PROPERTY), properties.get(PORT_PROPERTY), properties.get(CHANNEL_PROPERTY), properties.get(USER_ID_PROPERTY));

        //Connecting to MQ Manager.
        String mqManager = context.getParameter(PARAMETER_MQ_MANAGER);
        log.info("Connecting to queue manager {}", mqManager);
        try {
            mqMgr = new MQQueueManager(mqManager, properties);
        } catch (MQException e) {
            log.info(SETUP_TEST_ERROR_MESSAGE, e.getMessage(), MQConstants.lookupReasonCode(e.getReason()));
        }

        //Open mq queue to put message.
        String queuePutName = context.getParameter(PARAMETER_MQ_QUEUE_PUT);
        if (!MQ_QUEUE_PUT.equalsIgnoreCase(queuePutName) && !StringUtils.isBlank(queuePutName)) {
            log.info("Accessing queue: {} to put message.", queuePutName);

            try {
                mqQueuePut = mqMgr.accessQueue(queuePutName, MQOO_OUTPUT);
                log.info("Successfully accessed queue: {} to put message.", queuePutName);
            } catch (MQException e) {
                log.info(SETUP_TEST_ERROR_MESSAGE, e.getMessage(), MQConstants.lookupReasonCode(e.getReason()));
            }
        }

        //Open mq queue to get message
        String queueGetName = context.getParameter(PARAMETER_MQ_QUEUE_GET);
        if (!StringUtils.isBlank(queueGetName)) {
            log.info("Accessing queue: {} to get message.", queueGetName);
            try {
                mqQueueGet = mqMgr.accessQueue(queueGetName, MQOO_INPUT_AS_Q_DEF);
                log.info("Successfully accessed queue: {} to get message.", queueGetName);
            } catch (MQException e) {
                log.info(SETUP_TEST_ERROR_MESSAGE, e.getMessage(), MQConstants.lookupReasonCode(e.getReason()));
            }
        }

    }

    /**
     * Close and disconnect MQ variables.
     * @param context to get the arguments values on Java Sampler.
     */
    @Override
    public void teardownTest(JavaSamplerContext context) {
        try {
            if (mqQueuePut != null && mqQueuePut.isOpen()) {
                log.info(CLOSING_QUEUE_MESSAGE, mqQueuePut.getName());
                mqQueuePut.close();
                log.info(DONE);
            }
            if (mqQueueGet != null && mqQueueGet.isOpen()) {
                log.info(CLOSING_QUEUE_MESSAGE, mqQueueGet.getName());
                mqQueueGet.close();
                log.info(DONE);
            }
            if (mqMgr != null && mqMgr.isConnected()) {
                log.info("Disconnecting from the Queue Manager");
                mqMgr.disconnect();
                log.info(DONE);
            }
        } catch (MQException e) {
            log.info("teardownTest {}", e.getCause());
        }
    }

    /**
     * Main method to execute the test on single thread.
     * @param context to get the arguments values on Java Sampler.
     * @return SampleResult, captures data such as whether the test was successful,
     * the response code and message, any request or response data, and the test start/end times
     */
    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult result = newSampleResult();
        String message = context.getParameter(PARAMETER_MQ_MESSAGE);
        String response = "";
        byte[] messageId = {};

        String headers = context.getParameter(PARAMETER_MQ_HEADER);
        String messageFormat = context.getParameter(PARAMETER_MQ_MESSAGE_FORMAT);
        try {
            MQMessage mqMessage = writeMessage(message);
            if (!"${MQ_MESSAGE_FORMAT}".equals(messageFormat) && !"null".equals(messageFormat)) {
                log.info("Setting message format to {}", messageFormat);
                mqMessage.format = messageFormat;
            }
            if (!"${MQ_HEADER}".equals(headers) && !"null".equals(headers)) {
                if (null != headers && !headers.isEmpty()) {
                    log.info("Setting message header to {}", headers);
                    result.setRequestHeaders(headers);
                    for (String header : headers.split(",")) {
                        String[] keyValue = header.split(":");
                        mqMessage.setStringProperty(keyValue[0], keyValue[1]);
                    }
                }
            }
            result.setSamplerData(message);
            sampleResultStart(result, mqMessage);
            String queuePut = context.getParameter(PARAMETER_MQ_QUEUE_PUT);
            if (!StringUtils.isBlank(queuePut) ) {
                if (Objects.nonNull(mqQueuePut)) {
                    messageId = putMQMessage(mqMessage);
                    if (null != messageId && messageId.length > 0) {
                        log.info("Successfully put message into MQ queue: {}.", mqQueuePut.getName());
                        log.info("messageId: {}", Hex.encodeHexString(messageId));
                        log.info("messageBody: [{}]", message);
                    } else {
                        sampleResultFail(result, "500", new Exception(String.format("Failed to put message into MQ queue: %s", mqQueuePut.getName())));
                        return result;
                    }
                }else {
                    sampleResultFail(result, "500", new Exception(String.format("Failed to access MQ queue: %s with put", queuePut)));
                    return result;
                }
            }

            //Get message on queue.
            String queueGet = context.getParameter(PARAMETER_MQ_QUEUE_GET);
            if (!StringUtils.isBlank(queueGet) ) {
                if (Objects.nonNull(mqQueueGet)) {
                    do {
                        response = getMQMessage(context, messageId);
                    } while (null == response);
                }else {
                    sampleResultFail(result, "500", new Exception(String.format("Failed to access MQ queue: %s with get", queueGet)));
                    return result;
                }
            }

            sampleResultSuccess(result, response);
        } catch (MQException e) {
            sampleResultFail(result, "500", e);
            log.info("MQ Exception： runTest {} {}", e.getMessage(), MQConstants.lookupReasonCode(e.getReason()));
        } catch (Exception e) {
            sampleResultFail(result, "500", e);
            log.info("Exception runTest {}", e.getMessage());
        }
        return result;
    }

    /**
     * Method to put message on IBM mq queue.
     * @param message to put on mq queue.
     * @return messageId generate by MQ Manager.
     * @throws MQException
     */
    private byte[] putMQMessage(MQMessage message) throws MQException {

        mqQueuePut.put(message, new MQPutMessageOptions());
        return message.messageId;
    }

    /**
     * Create
     * @param message
     * @return MQMessage
     * @throws IOException
     */
    private MQMessage writeMessage(String message) throws IOException {

        MQMessage mqMessage = new MQMessage();

        log.info("generate a message...");
        switch (messageType) {
            case "Object":
                mqMessage.writeObject(message);
                break;
            case "String":
                mqMessage.writeString(message);
                break;
            default:
                mqMessage.write(message.getBytes(encodingMessage));
                break;
        }

        return mqMessage;
    }

    /**
     * Method to get message from IBM mq queue.
     * @param context to get the arguments values on Java Sampler.
     * @param messageId to correlate response message with request message.
     * @return String, message on mq queue.
     * @throws MQException
     * @throws UnsupportedEncodingException
     */
    private String getMQMessage(JavaSamplerContext context, byte[] messageId) throws MQException, UnsupportedEncodingException {
        MQGetMessageOptions mqGMO = new MQGetMessageOptions();
        String response = null;

        if (mqQueueGet != null && mqQueueGet.isOpen()) {
            String correlateMsg = context.getParameter(PARAMETER_MQ_CORRELATE_MSG);
            MQMsg2 mqMsg2 = new MQMsg2();

            if (Objects.nonNull(messageId) &&
                    (StringUtils.isEmpty(correlateMsg) ||
                            correlateMsg.equals(MESSAGE_ID) ||
                            correlateMsg.equals(CORRELATION_ID))) {
                mqMsg2.setCorrelationId(messageId);
            }
            //Set wait Interval to get message on queue.
            String waitInterval = context.getParameter(PARAMETER_MQ_WAIT_INTERVAL);
            if (waitInterval != null && !waitInterval.isEmpty() && StringUtils.isNumeric(waitInterval)) {
                mqGMO.options = MQGMO_WAIT;
                mqGMO.waitInterval = Integer.parseInt(waitInterval);
            }

            log.info("Getting a message...");
            try {
                mqQueueGet.getMsg2(mqMsg2, mqGMO);
                response = new String(mqMsg2.getMessageData(), encodingMessage);
            } catch (MQException e) {
                if (2033 != e.getReason()) {
                    throw e;
                }
            }
        }

        return response;
    }

    /**
     *
     * @return SampleResult, captures data such as whether the test was successful,
     * the response code and message, any request or response data, and the test start/end times
     */
    private SampleResult newSampleResult() {
        SampleResult result = new SampleResult();
        result.setDataEncoding(ENCODING);
        result.setDataType(SampleResult.TEXT);
        return result;
    }

    /**
     * Start the sample request and set the <code>samplerData</code> to the
     * requestData.
     *
     * @param result the sample result to update
     * @param data   the request to set as <code>samplerData</code>
     */
    private void sampleResultStart(SampleResult result, MQMessage data) {
//        result.setSamplerData(data.toString());
        result.sampleStart();
    }

    /**
     * Set the sample result as <code>sampleEnd()</code>,
     * <code>setSuccessful(true)</code>, <code>setResponseCode("OK")</code> and if
     * the response is not <code>null</code> then
     * <code>setResponseData(response.toString(), ENCODING)</code> otherwise it is
     * marked as not requiring a response.
     *
     * @param result   sample result to change
     * @param response the successful result message, may be null.
     */
    private void sampleResultSuccess(SampleResult result, String response) {
        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseCodeOK();
        if (response != null) {
            result.setResponseData(response, ENCODING);
        } else {
            result.setResponseData("No response required", ENCODING);
        }
    }

    /**
     * Mark the sample result as <code>sampleEnd</code>,
     * <code>setSuccessful(false)</code> and the <code>setResponseCode</code> to
     * reason.
     *
     * @param result the sample result to change
     * @param reason the failure reason
     */
    private void sampleResultFail(SampleResult result, String reason, Exception exception) {
        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseCode(reason);
        String responseMessage;

        responseMessage = "Exception: " + exception.getMessage();
        responseMessage += exception.getClass().equals(MQException.class) ? " MQ Reason Code: " + MQConstants.lookupReasonCode(((MQException) exception).getReason()) : EMPTY;
        responseMessage += exception.getCause() != null ? " Cause: " + exception.getCause() : EMPTY;
        result.setResponseMessage(responseMessage);

        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        result.setResponseData(stringWriter.toString(), ENCODING);
    }

}