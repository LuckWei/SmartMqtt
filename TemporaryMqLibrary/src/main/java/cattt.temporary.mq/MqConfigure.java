package cattt.temporary.mq;

import android.support.annotation.IntRange;

import cattt.temporary.mq.base.enums.QosClub;
import cattt.temporary.mq.base.enums.QosType;

public class MqConfigure {
    public static String userName = "guest";
    public static String password = "guest";

    public static String clientId = "11";
    public static String serverUri = new StringBuffer().append("tcp").append("://")
            .append("39.106.117.155").append(":").append("1883").toString();

    /**
     * key is topic
     * value is qos
     */
    public static String[] topics = null;
    @QosClub
    public static int qos = QosType.NORMAL;

    public static boolean isTrace = true;

    public static int connectionTimeout = 10;
    public static int keepAliveInterval = 60;
    @IntRange(from = 10, to = 1000)
    public static int maxInflight = 100;
    @IntRange(from = 5000, to = Integer.MAX_VALUE)
    public static int bufferSize = 10000;
}
