package gov.nist.healthcare.ttt.xdr.domain

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
 * Created by gerardin on 10/28/14.
 */


/*
Jaxb annotations are not used for now.
We parse GPathResult manually


"<TestClientResponse>" +
                "<Test>1666</Test>" +
                "<Status>Success</Status>" +
                "<InHeader>some headers</InHeader>" +
                "<Result>validation report</Result>" +
                "</TestClientResponse>"

 */

@XmlRootElement(name = "TestClientResponse")
@XmlAccessorType(value=XmlAccessType.FIELD)
class TkSendReport {

    @XmlElement(name="Test")
    String test


    @XmlElement(name="Status")
    String status

    @XmlElement(name="InHeader")
    String inHeader

    @XmlElement(name="Result")
    String result

}
