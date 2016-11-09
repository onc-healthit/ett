# Frequently Asked Questions #

[TOC]

## Direct ##

* Q: Why am I not receiving a validation report?
    * A: Please verify that you have registered your direct address with a corresponding contact address which is where the validation report will be delivered.  If you have registered, please check the spam folder to see if the validation report has been held there.  Also, the local mailbox (the part before the '@' symbol) portion is case-sensitive, so verify that the case in what was registered matches what is being sent over the wire.


* Q: Why can't the ETT find my certificate via LDAP?
    * A: Please make sure that the LDAP server and port are accessible and not blocked by a firewall.  We have been told of issues with older library packages (such as ApacheDS) that were resolved when the library packages were upgraded.

## Direct Edge -- SMTP/POP/IMAP ##

* Q:
    * A:

## Direct Edge -- XDR ##

* Q: I have sent my XDR message to the endpoint and receive a Registry Response.  However, the ETT never moves past the Pending Refresh button no matter how many times I press it.  How do I move forward?
    * A: Verify that your XDR message contains the Direct Address Block and that Block is completely filled out and matches what was entered into the Test Case UI.  The ETT uses the Direct Address block to route the messages, so if the values do not match, they will not be forwarded to the correct module within ETT.


* Q: For XDR Test Case 7 why do we not enter a full endpoint?
    * A: Ordinarily, ETT tracks the incoming messages via the Direct Address block. However, in this case, the SUT is supposed to stop the communication before a message is sent because the certificate is bad. Since the message should not come through, we will not have access to the Direct Address.  Therefore the ETT asks for the IP  address and tracks through that.   Since this is happening at the socket level, the full path for an endpoint is not applicable.

## XDM ##

* Q:
    * A:

## Other ##

* Q: Is it safe to send Protected Health Information (PHI) to NIST tooling?
    * A: No.  Never ever send Protected Health Information (PHI) to a NIST server.  If necessary, local copies of our tooling can be downloaded and installed.  DO NOT send live data to the tool on our website, there is no protection for PHI/PII.
