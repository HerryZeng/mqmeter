# mqmeter

MQ JMeter Extension.

A [JMeter](http://jmeter.apache.org/) Plugin to put and get message on [IBM MQ](https://www.ibm.com/products/mq) Queue, also publish message on Topic. It connect to MQ Server through server channel using ip address, port number, userID and password (if the channel has CHLAUTH rules).

## Install

Build the extension:

    mvn package

Install the extension `mqmeter-x.y.z.jar` into 

    `$JMETER_HOME/lib/ext`.

Also you can install it through [JMeter Plugins](http://jmeter.apache.org/), search "IBM MQ Support".

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/jmete-plugins-mqmeter.png?raw=true)

## Usage

After installing `mqmeter`, you can choose two kind of Java Sampler, these are:

### MQClientSampler

Use it to put and get message (optional) on MQ queue. On JMeter add a Java Request Sampler and select the `MQClientSampler` class name. The following parameter are necessary.

* **mq_manager**: MQ Manager name. You can find it through IBM WebSphere MQ Explore or console.
* **mq_queue_put**: MQ Queue name to put message. Could be Local or Remote queue.
* **mq_queue_get**: MQ Queue name to get message. Could be Local or Remote queue. Leave it empty if you don't want get response message.
* **mq_correlate_msg**: Correlate the response message with request message to get the right message from the queue. Put 'messageId' or 'correlationId' values. Leave it empty if you don't want get response message.
* **mq_wait_interval**: Set wait interval that the get message call waits for a suitable message to arrive. Similar to time-out to get response message on queue.
* **mq_hostname**: Host name or ip address where MQ Server is running.
* **mq_port**: Port number of the MQ Server listener.
* **mq_channel**: The Server channel name on MQ Server.
* **mq_user_id**: The userID to connect to MQ server channel. Leave it empty if you don't need user id to connect to MQ.
* **mq_user_password**: The user password to connect to MQ server channel. Leave it empty if you don't need user id and password to connect to MQ.
* **mq_use_mqcsp_authentication**: The connection authentication used. Set false for Compatibility mode, or true for MQCSP authentication.
* **mq_encoding_message**: Character encoding standard for your message: For EBCDIC put Cp1047. ASCII just put ASCII.
* **mq_message**: The content of the message that you want.
* **mq_headers**: The format is `key1=value1,key2=value2`.
* **mq_message_format**: MQMD Message [Format](https://www.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.ref.dev.doc/q097520_.htm).
   You can set one of the values in constants [CMQC.MQFMT_*](https://www.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.javadoc.doc/WMQJavaClasses/constant-values.html#com.ibm.mq.constants.CMQC.MQFMT_STRING).
   For example, the value of constant `CMQC.MQFMT_STRING` - `MQSTR `.
* **mq_ssl_enable**: Set to `true` to enable SSL/TLS connection to MQ Server. Default is `false`.
* **mq_ssl_keystore_path**: Path to the SSL keystore file (JKS or PKCS12 format). Required for mutual SSL authentication (client certificate). Leave empty if not needed.
* **mq_ssl_keystore_password**: Password for the SSL keystore. Leave empty if keystore path is not set.
* **mq_ssl_truststore_path**: Path to the SSL truststore file (JKS or PKCS12 format). Required to verify the MQ server certificate. For one-way SSL, only this parameter is needed.
* **mq_ssl_truststore_password**: Password for the SSL truststore. Leave empty if truststore path is not set.
* **mq_ssl_cipher_suite**: SSL/TLS cipher suite (algorithm) to use for the connection. Must match the CipherSpec configured on the MQ server channel. Examples: `TLS_RSA_WITH_AES_128_CBC_SHA256`, `TLS_RSA_WITH_AES_256_CBC_SHA256`, `ECDHE_RSA_AES_128_CBC_SHA256`.

#### Put & Get Message on Queue

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/PutGetMessageOnQueue.png?raw=true)

#### Put Message on Queue

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/PutMessageOnQueue.png?raw=true)

### MQPublishSampler

MQ can manage topics also and you can publish and subscribe to it, use this class to publish message on MQ Topic.
On JMeter add a Java Request Sampler and select the `MQPublishSampler` class name. The following parameters are necessary.

* **mq_manager**: MQ Manager name. You can find it through IBM WebSphere MQ Explore or console.
* **mq_topic**: MQ topic name to publish message.
* **mq_hostname**: Host name or ip address where MQ Server is running.
* **mq_port**: Port number of the MQ Server listener.
* **mq_channel**: The Server channel name on MQ Server.
* **mq_user_id**: The userID to connect to MQ server channel. Leave it empty if you don't need user id to connect to MQ.
* **mq_user_password**: The user password to connect to MQ server channel. Leave it empty if you don't need user id and password to connect to MQ.
* **mq_encoding_message**: Character encoding standard for your message: For EBCDIC put Cp1047. ASCII just put ASCII.
* **mq_message**: The content of the message that you want.
* **mq_ssl_enable**: Set to `true` to enable SSL/TLS connection to MQ Server. Default is `false`.
* **mq_ssl_keystore_path**: Path to the SSL keystore file (JKS or PKCS12 format). Required for mutual SSL authentication (client certificate). Leave empty if not needed.
* **mq_ssl_keystore_password**: Password for the SSL keystore. Leave empty if keystore path is not set.
* **mq_ssl_truststore_path**: Path to the SSL truststore file (JKS or PKCS12 format). Required to verify the MQ server certificate. For one-way SSL, only this parameter is needed.
* **mq_ssl_truststore_password**: Password for the SSL truststore. Leave empty if truststore path is not set.
* **mq_ssl_cipher_suite**: SSL/TLS cipher suite (algorithm) to use for the connection. Must match the CipherSpec configured on the MQ server channel. Examples: `TLS_RSA_WITH_AES_128_CBC_SHA256`, `TLS_RSA_WITH_AES_256_CBC_SHA256`, `ECDHE_RSA_AES_128_CBC_SHA256`.

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/PublishMessageOnTopic.png?raw=true)

## SSL Configuration

The following table explains the relationship between `mq_ssl_keystore_path` and `mq_ssl_truststore_path` parameters:

| Scenario | Configuration Required |
|----------|----------------------|
| **One-way SSL** (Server authentication only) | Only `mq_ssl_truststore_path` is required. This truststore should contain the MQ server's certificate or the CA certificate that signed it. |
| **Two-way SSL** (Mutual authentication) | Both `mq_ssl_keystore_path` and `mq_ssl_truststore_path` are required. The keystore contains the client certificate for authentication, and the truststore contains the server's certificate for verification. |

### SSL Configuration Examples

**One-way SSL (most common):**
```
mq_ssl_enable=true
mq_ssl_truststore_path=/path/to/truststore.jks
mq_ssl_truststore_password=changeit
mq_ssl_cipher_suite=TLS_RSA_WITH_AES_128_CBC_SHA256
```

**Two-way SSL (mutual authentication):**
```
mq_ssl_enable=true
mq_ssl_keystore_path=/path/to/keystore.jks
mq_ssl_keystore_password=changeit
mq_ssl_truststore_path=/path/to/truststore.jks
mq_ssl_truststore_password=changeit
mq_ssl_cipher_suite=TLS_RSA_WITH_AES_128_CBC_SHA256
```

**Note:** The `mq_ssl_cipher_suite` value must match the CipherSpec configured on the MQ server channel. Common values include:
- `TLS_RSA_WITH_AES_128_CBC_SHA256`
- `TLS_RSA_WITH_AES_256_CBC_SHA256`
- `ECDHE_RSA_AES_128_CBC_SHA256`

## IBM WebSphere MQ

The below images show where find the values for some of the above properties

* **MQ Manager**

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/MQManager.png?raw=true)

* **MQ Server channel**

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/MQServerChanel.png?raw=true)

* **MQ Server listener**

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/MQServerListener.png?raw=true)

* **MQ Chlauth**

You can find the steps to add users access to MQ Manager through Channel Authentication (CHLAUTH) with this tutorial
[IBM CHLAUTH](http://www-01.ibm.com/support/docview.wss?uid=swg27041997&aid=1)

![Screenshot](https://github.com/JoseLuisSR/mqmeter/blob/develop/doc/img/MQChlauth.png?raw=true)

## Troubleshooting

When use the MQPublishSampler Java Sampler is possible that you get the below exception:

java.lang.NoSuchMethodError: com.ibm.mq.MQQueueManager.accessTopic(Ljava/lang/String;Ljava/lang/String;II)Lcom/ibm/mq/MQTopic;
	at co.signal.mqmeter.MQPublishSampler.setupTest(MQPublishSampler.java:123) ~[mqmeter-1.4.jar:?]
	at org.apache.jmeter.protocol.java.sampler.JavaSampler.sample(JavaSampler.java:194) ~[ApacheJMeter_java.jar:3.3 r1808647]
	at org.apache.jmeter.threads.JMeterThread.executeSamplePackage(JMeterThread.java:498) ~[ApacheJMeter_core.jar:3.3 r1808647]
	at org.apache.jmeter.threads.JMeterThread.processSampler(JMeterThread.java:424) ~[ApacheJMeter_core.jar:3.3 r1808647]
	at org.apache.jmeter.threads.JMeterThread.run(JMeterThread.java:255) ~[ApacheJMeter_core.jar:3.3 r1808647]
	at java.lang.Thread.run(Unknown Source) [?:?]

It is throw because there is com.ibm.mq* jar file  that is upload by JMeter and it does not have the methods related with topic or other MQ objects. It jar file isn't related with mqmeter plugin but JMeter calls it first.

Checking JMeter lib and ext folders to remove com.ibm.mq* jar files that do not have methods or mq topic objects.

