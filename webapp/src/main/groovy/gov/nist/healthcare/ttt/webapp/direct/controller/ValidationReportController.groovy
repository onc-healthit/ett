package gov.nist.healthcare.ttt.webapp.direct.controller;

import gov.nist.healthcare.ttt.database.log.PartInterface;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException;
import gov.nist.healthcare.ttt.webapp.direct.model.messageValidator.DirectMessageAttachments;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/api/validationReport")
public class ValidationReportController {

	private static Logger logger = LogManager.getLogger(ValidationReportController.class.getName());
	
	private int attachmentNumber = 0;

	@Autowired
	private DatabaseInstance db;

	@RequestMapping(value = "/{messageId:.+}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
    PartInterface validateDirectMessage(@PathVariable String messageId, HttpServletRequest request) throws Exception {
		
		logger.debug("Getting validation report for message id: " + messageId);
		
		PartInterface partRes = db.getLogFacade().getPartByMessageId(messageId);
		if(partRes == null) {
			throw new TTTCustomException("0x0029", "This validation report does not exist");
		}
		return partRes;
	}
	
	@RequestMapping(value = "/rawContent/{messageId:.+}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<DirectMessageAttachments> getPartsRawContent(@PathVariable String messageId, HttpServletRequest request) throws Exception {
		
		logger.debug("Getting attachments for message id: " + messageId);
		
		Collection<DirectMessageAttachments> res = new ArrayList<DirectMessageAttachments>();
		
		PartInterface partRes = db.getLogFacade().getPartByMessageId(messageId);
		if(partRes == null) {
			throw new TTTCustomException("0x0029", "This validation report does not exist");
		}
		
		res = getPartContentTable(res, partRes);
		
		this.attachmentNumber = 0;
		
		return res;
	}
	
	@RequestMapping(value = "/download/{partId:.+}", method = RequestMethod.GET, produces = "application/json")
public @ResponseBody void downloadContent(@PathVariable String partId,
                                          HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
    logger.info("Starting download for partId: " + partId);
    try {
        PartInterface partRes = db.getLogFacade().getPart(partId);
        if (partRes == null) {
            String msg = "Part not found for partId: " + partId;
            logger.error(msg);
            throw new TTTCustomException("0x0030", msg);
        }
        String rawContent = partRes.getRawMessage();
        String contentType = partRes.getContentType();
        String filename = getFilename(partRes);
        logger.info("Content type: " + contentType);
        logger.info("Filename determined by getFilename: " + filename);
        byte[] originalBytes = rawContent.getBytes(StandardCharsets.UTF_8);
        InputStream finalStream;
        boolean hasBase64Header = rawContent.contains("Content-Transfer-Encoding: base64");
        if (hasBase64Header || contentType.contains("application/octet-stream")) {
            logger.info("Detected base64 or octet-stream, parsing with MimeBodyPart.");
            MimeBodyPart part = new MimeBodyPart(new ByteArrayInputStream(originalBytes));
            Object partContent = part.getContent();
            if (partContent instanceof InputStream) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy((InputStream) partContent, bos);
                finalStream = new ByteArrayInputStream(bos.toByteArray());
            } else if (partContent instanceof String) {
                byte[] textBytes = ((String) partContent).getBytes(StandardCharsets.UTF_8);
                finalStream = new ByteArrayInputStream(textBytes);
            } else {
                logger.info("Unknown content type in MIME part. Using original bytes.");
                finalStream = new ByteArrayInputStream(originalBytes);
            }
        } else if (contentType.contains("application/xml")) {
            logger.info("Detected XML content. Using MimeBodyPart parsing.");
            MimeBodyPart part = new MimeBodyPart(new ByteArrayInputStream(originalBytes));
            finalStream = part.getInputStream();
        } else if (contentType.contains("application/pdf")) {
            logger.info("Detected PDF content. Using direct approach.");
            finalStream = new ByteArrayInputStream(originalBytes);
        } else {
            logger.info("Detected other content type. Using MimeBodyPart parsing.");
            MimeBodyPart part = new MimeBodyPart(new ByteArrayInputStream(originalBytes));
            finalStream = part.getInputStream();
        }
        logger.info("rawContent length: " + originalBytes.length);
        byte[] finalData;
        if (finalStream.markSupported()) {
            finalStream.mark(Integer.MAX_VALUE);
            finalData = IOUtils.toByteArray(finalStream);
            finalStream.reset();
        } else {
            finalData = IOUtils.toByteArray(finalStream);
            finalStream = new ByteArrayInputStream(finalData);
        }
        int fileSize = finalData.length;
        response.setContentLength(fileSize);
        response.setContentType(contentType);
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", filename);
        if (hasFilename(partRes)) {
            headerValue = partRes.getContentDisposition();
        }
        response.setHeader(headerKey, headerValue);
        OutputStream out = response.getOutputStream();
        int bytesCopied = IOUtils.copy(new ByteArrayInputStream(finalData), out);
        logger.info("IOUtils.copy copied " + bytesCopied + " bytes to output stream.");
        finalStream.close();
        out.flush();
        response.flushBuffer();
        logger.info("Download completed successfully for partId: " + partId);
    } catch (Exception ex) {
        logger.error("Error during download for partId: " + partId, ex);
        response.getWriter().print("Error: " + ex.getMessage());
    }
}
	
	public Collection<DirectMessageAttachments> getPartContentTable(Collection<DirectMessageAttachments> res, PartInterface part) {
		if(!part.getRawMessage().equals("")) {
			res.add(new DirectMessageAttachments(getFilename(part), part.getRawMessage(), saveAttachmentAndGetLink(part)));
		}
		for(PartInterface child : part.getChildren()) {
			getPartContentTable(res, child);
		}
		return res;
	}

	public String getFilename(PartInterface part) {
		String contentDisposition = part.getContentDisposition();
		String res = "No Filename " + this.attachmentNumber;
		if(part.getContentType().contains("pkcs7-mime")) {
			return "encrypted-message.txt";
		} else if(part.getContentType().contains("multipart/signed")) {
			return "decrypted-message.txt";
		} else {
			if(contentDisposition != null) {
				if(contentDisposition.contains("filename")) {
					res = contentDisposition.split("filename=")[1];
					if(res.contains(";")) {
						res = res.split(";")[0];
					}
				} else {
					res = "attachment-" + this.attachmentNumber;
					this.attachmentNumber++;
				}
			}
			return res;
		}
	}
	
	public boolean hasFilename(PartInterface part) {
		String contentDisposition = part.getContentDisposition();
		if(contentDisposition != null) {
			if(contentDisposition.contains("filename")) {
				return true;
			}
		}
		return false;
	}
	
	public String saveAttachmentAndGetLink(PartInterface part) {
		return part.getPartID();
	}
	
}