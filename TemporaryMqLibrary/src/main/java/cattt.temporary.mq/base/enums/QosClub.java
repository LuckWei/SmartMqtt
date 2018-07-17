package cattt.temporary.mq.base.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({QosType.LOW, QosType.GOOD, QosType.NORMAL})
@Retention(RetentionPolicy.SOURCE)
public @interface QosClub {}
