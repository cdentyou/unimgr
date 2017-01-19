package org.opendaylight.unimgr.mef.notification.model.types;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

/**
 * Container for SchemaPath.
 */
public class NotificationType {

    private final SchemaPath schemaPath;

    public NotificationType(String schema){
        schemaPath = createSchemaPath(schema);
    }

    private SchemaPath createSchemaPath(String schema){
        //TODO: figure it out how to set data - new Data() does not takes seconds whats not fit for us
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2015, 4, 8, 0, 0, 0);
        Date revisionDate = cal.getTime();
        URI uri;
        try {
            uri = new URI(schema);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI for notification", e);
        }
        QName qn = QName.create(uri,revisionDate,schema+"-message");

        return SchemaPath.create(true, qn);
    }

    public SchemaPath getSchemaPath(){
        return schemaPath;
    }
}
