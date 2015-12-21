package gov.nist.healthcare.ttt.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.Status

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
/**
 * TODO
 * This class use to be automatically instantiated by jaxb.
 * Now it is done manually with groovy slurper, until a
 * stable format is defined.
 *
 * Created by gerardin on 10/15/14.
 */

@XmlRootElement(name = "transactionLog")
@XmlAccessorType(value=XmlAccessType.FIELD)
class TkValidationReport {

    @XmlElement
    String request

    @XmlElement
    String response

    Status status

    String messageId

    String simId

    String directFrom


}
