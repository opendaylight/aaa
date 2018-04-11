//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.opendaylight.aaa.api.model;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(
        name = "idmerror"
)
public class IDMError {
    private static final Logger LOG = LoggerFactory.getLogger(IDMError.class);
    private String message;
    private String details;
    private int code;

    public IDMError() {
        this.code = 500;
    }

    public IDMError(int statusCode, String msg, String msgDetails) {
        this.code = 500;
        this.code = statusCode;
        this.message = msg;
        this.details = msgDetails;
    }

    public IDMError(int statusCode, String msg) {
        this(statusCode, msg, "");
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Response response() {
        LOG.error("error: {} details: {} status: {}", new Object[]{this.message, this.details, this.code});
        return Response.status(this.code).entity(this).build();
    }
}
