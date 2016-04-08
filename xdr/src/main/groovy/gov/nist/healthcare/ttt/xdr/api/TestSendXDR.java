//package gov.nist.healthcare.ttt.xdr.api;
//
//
//import gov.nist.toolkit.configDatatypes.SimulatorActorType;
//import gov.nist.toolkit.configDatatypes.SimulatorProperties;
//import gov.nist.toolkit.toolkitApi.BasicSimParameters;
//import gov.nist.toolkit.toolkitApi.DocumentRecipient;
//import gov.nist.toolkit.toolkitApi.DocumentSource;
//import gov.nist.toolkit.toolkitApi.SimulatorBuilder;
//import gov.nist.toolkit.toolkitApi.ToolkitServiceException;
//import gov.nist.toolkit.toolkitServicesCommon.RawSendRequest;
//import gov.nist.toolkit.toolkitServicesCommon.RawSendResponse;
//import gov.nist.toolkit.toolkitServicesCommon.SimConfig;
//import gov.nist.toolkit.toolkitServicesCommon.resource.DocumentResource;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.util.Map;
//
//
//public class TestSendXDR implements XdrSender {
//
//	public static void main(String[] args) throws Exception {
//		String urlRoot = String.format("http://localhost:8080/xdstools2");
//
//		SimulatorBuilder spi = new SimulatorBuilder(urlRoot);
//		BasicSimParameters srcParams = new BasicSimParameters();
//		BasicSimParameters recParams = new BasicSimParameters();
//
//		srcParams.setId("source");
//		srcParams.setUser("bill");
//		srcParams.setActorType(SimulatorActorType.DOCUMENT_SOURCE);
//		srcParams.setEnvironmentName("test");
//
//		recParams.setId("recipient");
//		recParams.setUser("bill");
//		recParams.setActorType(SimulatorActorType.DOCUMENT_RECIPIENT);
//		recParams.setEnvironmentName("test");
//		
//		System.out.println("STEP - DELETE DOCREC SIM");
//        spi.delete(recParams.getId(), recParams.getUser());
//
//		System.out.println("STEP - CREATE DOCREC SIM");
//		DocumentRecipient documentRecipient = spi.createDocumentRecipient(
//				recParams.getId(),
//				recParams.getUser(),
//				recParams.getEnvironmentName()
//				);
//		
//		System.out.println(documentRecipient.getFullId());
//
//		System.out.println("This is un-verifiable since notifications are handled through the servlet filter chain which is not configured here");
//		System.out.println("STEP - UPDATE - REGISTER NOTIFICATION");
//		documentRecipient.setProperty(SimulatorProperties.TRANSACTION_NOTIFICATION_URI, urlRoot + "/rest/toolkitcallback");
//		documentRecipient.setProperty(SimulatorProperties.TRANSACTION_NOTIFICATION_CLASS, "main.java.xdr.TestSendXDR");
//		SimConfig withRegistration = documentRecipient.update(documentRecipient.getConfig());
//		System.out.println("Updated Src Sim config is" + withRegistration.describe());
//
//		System.out.println("verify sim built");
//		System.out.println(documentRecipient.getId() == recParams.getId());
//
//		System.out.println("STEP - DELETE DOCSRC SIM");
//		spi.delete(srcParams.getId(), srcParams.getUser());
//
//		System.out.println("STEP - CREATE DOCSRC SIM");
//		DocumentSource documentSource = spi.createDocumentSource(
//				srcParams.getId(),
//				srcParams.getUser(),
//				srcParams.getEnvironmentName()
//				);
//
//
//		System.out.println("verify sim built");
//		System.out.println(documentSource.getId() == srcParams.getId());
//
//		System.out.println("STEP - UPDATE - SET DOC REC ENDPOINTS INTO DOC SRC");
////		documentSource.setProperty(SimulatorProperties.pnrEndpoint, documentRecipient.asString(SimulatorProperties.pnrEndpoint));
//		documentSource.setProperty(SimulatorProperties.pnrEndpoint, "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/10/docrec/prb");
////		documentSource.setProperty(SimulatorProperties.pnrTlsEndpoint, "https://transport-testing.nist.gov:12081/ttt/sim/b55b2b31-3e67-4d61-ba19-c01b02ee4b8e/rec/xdrpr");
//		SimConfig updatedVersion = documentSource.update(documentSource.getConfig());
//		System.out.println("Updated Src Sim config is " + updatedVersion.describe());
//
//		System.out.println(updatedVersion);
//
//		System.out.println("STEP - SEND XDR");
//		RawSendRequest req = documentSource.newRawSendRequest();
//
//		FileInputStream in = new FileInputStream(new File("Xdr_full_metadata_only.xml"));
////		req.setMetadata(IOUtils.toString(in));
//		DocumentResource document = new DocumentResource();
//		document.setContents("Hello World!".getBytes());
//		document.setMimeType("text/plain");
//		req.addDocument("Document01", document);
//
//		RawSendResponse response = documentSource.sendProvideAndRegister(req);
//		System.out.println(response.getRequestSoapHeader());
//		System.out.println(response.getRequestSoapBody());
//		System.out.println(response.getResponseSoapHeader());
//		System.out.println(response.getResponseSoapBody());
//
////		String responseSoapBody = response.getResponseSoapBody();
////		OMElement responseEle = Util.parse_xml(responseSoapBody);
////		RegistryErrorListParser rel = new RegistryErrorListParser(responseEle);
////		List<RegistryError> errors = rel.getRegistryErrorList();
////		for(RegistryError err : errors) {
////			System.out.println(err.codeContext);
////		}
//
////		System.out.println(errors.size() == 0);
//	}
//
//	@Override
//	public Object sendXdr(Map config) {
//		String urlRoot = String.format("http://localhost:8080/xdstools2");
//
//		SimulatorBuilder spi = new SimulatorBuilder(urlRoot);
//		BasicSimParameters srcParams = new BasicSimParameters();
//
//		srcParams.setId("source");
//		srcParams.setUser("bill");
//		srcParams.setActorType(SimulatorActorType.DOCUMENT_SOURCE);
//		srcParams.setEnvironmentName("test");
//		
//		System.out.println("STEP - CREATE DOCSRC SIM");
//		DocumentSource documentSource = null;
//		try {
//			documentSource = spi.createDocumentSource(
//					srcParams.getId(),
//					srcParams.getUser(),
//					srcParams.getEnvironmentName()
//					);
//		} catch (ToolkitServiceException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println("STEP - UPDATE - SET DOC REC ENDPOINTS INTO DOC SRC");
////		documentSource.setProperty(SimulatorProperties.pnrEndpoint, documentRecipient.asString(SimulatorProperties.pnrEndpoint));
//		documentSource.setProperty(SimulatorProperties.pnrEndpoint, "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/10/docrec/prb");
////		documentSource.setProperty(SimulatorProperties.pnrTlsEndpoint, "https://transport-testing.nist.gov:12081/ttt/sim/b55b2b31-3e67-4d61-ba19-c01b02ee4b8e/rec/xdrpr");
//		SimConfig updatedVersion = null;
//		try {
//			updatedVersion = documentSource.update(documentSource.getConfig());
//		} catch (ToolkitServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Updated Src Sim config is " + updatedVersion.describe());
//		
//		System.out.println("STEP - SEND XDR");
//		RawSendRequest req = documentSource.newRawSendRequest();
//
//		try {
//			FileInputStream in = new FileInputStream(new File("Xdr_full_metadata_only.xml"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////		req.setMetadata(IOUtils.toString(in));
//		DocumentResource document = new DocumentResource();
//		document.setContents("Hello World!".getBytes());
//		document.setMimeType("text/plain");
//		req.addDocument("Document01", document);
//
//		RawSendResponse response = null;
//		try {
//			response = documentSource.sendProvideAndRegister(req);
//		} catch (ToolkitServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(response.getRequestSoapHeader());
//		System.out.println(response.getRequestSoapBody());
//		System.out.println(response.getResponseSoapHeader());
//		System.out.println(response.getResponseSoapBody());
//
//		return null;
//	}
//}
