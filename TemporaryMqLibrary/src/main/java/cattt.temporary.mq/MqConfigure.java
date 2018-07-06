package cattt.temporary.mq;

public class MqConfigure {
    public static String userName = "guest";
    public static String password = "guest";
    public static String clientId = "11";
    public static String serverUri = new StringBuffer().append("tcp").append("://")
            .append("192.168.0.1").append(":").append("1883").toString();

    /**
     * key is topic
     * value is qos
     */
    public static String[] topics = null;
}
