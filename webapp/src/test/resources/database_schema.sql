Database: "direct"

CREATE TABLE DirectEmail
(
DirectEmailID varchar(255) NOT NULL,
DirectEmail varchar(255) NOT NULL,
PRIMARY KEY (DirectEmailID)
);

CREATE TABLE ContactEmail
(
ContactEmailID varchar(255) NOT NULL,
DirectEmailID varchar(255) NOT NULL,
ContactEmail varchar(255) NOT NULL,
PRIMARY KEY (ContactEmailId),
FOREIGN KEY (DirectEmailID) REFERENCES DirectEmail(DirectEmailID)
);

CREATE TABLE Users (
Username        varchar(255) NOT NULL,
Password         varchar(255) NOT NULL,
PRIMARY KEY (Username)
);

CREATE TABLE UserDirect (
Username varchar(255) NOT NULL,
DirectEmailID varchar(255) NOT NULL,
PRIMARY KEY (Username, DirectEmailID),
FOREIGN KEY (DirectEmailID) REFERENCES DirectEmail(DirectEmailID)
);

CREATE TABLE Log (
LogID varchar(255) NOT NULL,
Incoming tinyint NOT NULL,
Timestamp varchar(255) NOT NULL,
Status tinyint,
Mdn tinyint,
OrigDate varchar(255),
MessageId varchar(255),
OriginalMessageId varchar(255),
MIMEVersion varchar(255),
Subject varchar(255),
ContentType text,
ContentDisposition text,
PRIMARY KEY (LogID),
UNIQUE (MessageId)
);

CREATE INDEX LogMessageId On Log(MessageId(64));

CREATE TABLE FromLine (
FromLineID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
FromLine varchar(255),
PRIMARY KEY (FromLineID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE INDEX FromLine on FromLine(FromLine);

CREATE TABLE ToLine (
ToLineID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
ToLine varchar(255),
PRIMARY KEY (ToLineID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE INDEX ToLine on ToLine(ToLine);

CREATE TABLE Received (
ReceivedID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
Received text,
PRIMARY KEY (ReceivedID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE TABLE ReplyTo (
ReplyToID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
ReplyTo varchar(255),
PRIMARY KEY (ReplyToID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE TABLE Part (
PartID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
RawMessage LONGBLOB,
ContentType text,
ContentTransferEncoding varchar(255),
ContentDisposition varchar(255),
Status tinyint,
PRIMARY KEY (PartID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE TABLE CCDAValidationReport (
CCDAValidationReportID varchar(255) NOT NULL,
LogID varchar(255) NOT NULL,
Filename varchar(255),
ValidationReport LONGBLOB,
PRIMARY KEY (CCDAValidationReportID),
FOREIGN KEY (LogID) REFERENCES Log(LogID)
);

CREATE TABLE PartRelationship (
PartRelationshipID varchar(255) NOT NULL,
ParentID varchar(255),
ChildID varchar(255) NOT NULL,
PRIMARY KEY (PartRelationshipID),
FOREIGN KEY (ParentID) REFERENCES Part(PartID),
FOREIGN KEY (ChildID) REFERENCES Part(PartID)
);

CREATE TABLE Detail (
DetailID varchar(255) NOT NULL,
PartID varchar(255) NOT NULL,
Counter integer,
Name varchar(255),
Status integer,
DTS varchar(255),
Found text,
Expected text,
RFC text,
PRIMARY KEY (DetailID),
FOREIGN KEY (PartID) REFERENCES Part(PartID)
);

CREATE TABLE SmtpEdgeProfile (
SmtpEdgeProfileID varchar(255) NOT NULL,
Username varchar(255) NOT NULL,
ProfileName varchar(255),
SUTSMTPAddress varchar(255),
SUTEmailAddress varchar(255),
SUTUsername varchar(255),
SUTPassword varchar(255),
PRIMARY KEY (SmtpEdgeProfileID),
FOREIGN KEY (Username) REFERENCES Users(Username),
UNIQUE (Username, ProfileName)
);

CREATE TABLE SmtpEdgeLog (
SmtpEdgeLogID varchar(255) NOT NULL,
SmtpEdgeProfileID varchar(255) NOT NULL,
Timestamp varchar(255) NOT NULL,
TransactionID varchar(255),
TestCaseNumber varchar (50),
CriteriaMet tinyint,
TestRequestsResponse text,
PRIMARY KEY (SmtpEdgeLogID),
FOREIGN KEY (SmtpEdgeProfileID) REFERENCES SmtpEdgeProfile(SmtpEdgeProfileID)
);

CREATE INDEX SmtpEdgeLogTransaction On SmtpEdgeLog(TransactionID(64));
CREATE INDEX SmtpEdgeLogTimestamp On SmtpEdgeLog(Timestamp(64));

CREATE TABLE SmtpEdgeContent (
SmtpEdgeContentID varchar(255) NOT NULL,
SmtpEdgeLogID varchar(255) NOT NULL,
Content LONGBLOB,
PRIMARY KEY (SmtpEdgeContentID),
FOREIGN KEY (SmtpEdgeLogID) REFERENCES SmtpEdgeLog(SmtpEdgeLogID)
);

CREATE TABLE XDRRecord (
XDRRecordID varchar(255) NOT NULL,
Username varchar(255),
TestCaseNumber varchar (8),
Timestamp varchar(255) NOT NULL,
CriteriaMet tinyint,
PRIMARY KEY (XDRRecordID),
FOREIGN KEY (Username) REFERENCES Users(Username)
);

CREATE TABLE XDRTestStep (
XDRTestStepID varchar(255) NOT NULL,
XDRRecordID varchar(255) NOT NULL,
Timestamp varchar(255) NOT NULL,
Name varchar(255),
MessageId varchar(255),
Hostname varchar(255),
CriteriaMet tinyint,
PRIMARY KEY (XDRTestStepID),
FOREIGN KEY (XDRRecordID) REFERENCES XDRRecord(XDRRecordID),
UNIQUE (MessageId)
);

CREATE INDEX XDRTestStepMessageId On XDRTestStep(MessageId(64));
CREATE INDEX XDRTestStepHostname On XDRTestStep(Hostname(64));

CREATE TABLE XDRSimulator (
XDRSimulatorID varchar(255) NOT NULL,
XDRTestStepID varchar(255),
SimulatorId varchar(255),
Endpoint varchar(255),
EndpointTLS varchar(255),
PRIMARY KEY (XDRSimulatorID)
);

CREATE TABLE XDRReportItem (
XDRReportItemID varchar(255) NOT NULL,
XDRTestStepID varchar(255) NOT NULL,
Report mediumblob,
ReportType int,
PRIMARY KEY (XDRReportItemID),
FOREIGN KEY (XDRTestStepID) REFERENCES XDRTestStep(XDRTestStepID)
);

