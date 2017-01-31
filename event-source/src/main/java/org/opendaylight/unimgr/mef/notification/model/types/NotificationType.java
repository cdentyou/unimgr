package org.opendaylight.unimgr.mef.notification.model.types;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

/**
 * Wrapper for SchemaPath.
 */
public class NotificationType {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationType.class);
    private final SchemaPath schemaPath;

    public NotificationType(String schema){
        schemaPath = createSchemaPath(schema);
    }

    /**
     * Method creates SchemaPath from String.
     *
     * @param schema String value used in SchemaPath
     * @return SchemaPatch
     */
    private SchemaPath createSchemaPath(String schema){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        //It is set on specific day, becouse laster we compare SchemaPaths between each other, and date have to be identical.
        cal.set(2017, 1, 1, 0, 0, 0);
        Date revisionDate = cal.getTime();
        URI uri;
        try {
            uri = new URI(schema);
        } catch (URISyntaxException e) {
            LOG.error("Bad URI: {}",e.toString());
            throw new RuntimeException("Bad URI for notification", e);
        }
        QName qn = QName.create(uri,revisionDate,schema+"-message");

        return SchemaPath.create(true, qn);
    }

    public SchemaPath getSchemaPath(){
        return schemaPath;
    }
}
