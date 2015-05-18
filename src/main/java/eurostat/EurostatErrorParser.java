package eurostat;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EurostatErrorParser {
	final static HashMap<Integer, String> errorCodeDescription = new HashMap<Integer, String>() {

		private static final long serialVersionUID = -1170550366260970779L;

		{
			put(404, "Not found");
			put(401, "Unauthorized");
			put(413, "Request entity too large");
			put(400, "Bad syntax");
			put(500, "Internal server error");
			put(501, "Not implemented");
			put(503, "Service unavailable");
			put(413, "Request entity too large");
			put(500, "Internal Server error");
		}
	};
	
	enum ParseTag {
		MESSAGE_GENERICDATA("message", "GenericData"),
		FOOTER_FOOTER("footer", "Footer"),
		FOOTER_MESSAGE("footer", "Message"),
		COMMON_TEXT("common", "Text");
		
		private QName qname;
		
		private ParseTag(String namespace, String localName) {
			qname = new QName(namespace, localName);
		}
		
		public boolean matchesTag(QName tag) {
			return tag.getPrefix().equals(qname.getNamespaceURI()) &&
					tag.getLocalPart().equals(qname.getLocalPart());
		}
	}
	
	private final Log logger = LogFactory.getLog(getClass());
	
	int code;
	String severity;
	List<String> textMessages = new ArrayList<String>();
	XMLStreamReader parser;
	Stack<ParseTag> nestingStack = new Stack<ParseTag>();
	
	public EurostatErrorParser(InputStream input) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		parser = factory.createXMLStreamReader(input);
	}

	public void parse() throws NumberFormatException, XMLStreamException {
		Stack<ParseTag> nestingStack = new Stack<ParseTag>();
		
		while (parser.hasNext()) {
			int event = parser.next();
			
			if (event == XMLStreamConstants.START_ELEMENT) {
				QName nodeName = parser.getName();
				
				ParseTag newTag = null;
				for (ParseTag tag : ParseTag.values()) {
					if (tag.matchesTag(nodeName)) {
						newTag = tag;
						break;
					}
				}
				
				if (newTag == ParseTag.FOOTER_MESSAGE) {
					// check current stack
					if (nestingStack.peek() != ParseTag.FOOTER_FOOTER) {
						logger.warn("Found footer message node while not inside a footer.");
					}
					
					code = Integer.parseInt(parser.getAttributeValue(null, "code"));
					severity = parser.getAttributeValue(null, "severity");
				}
				
				nestingStack.push(newTag);
			} else if (event == XMLStreamConstants.END_ELEMENT) {
				nestingStack.pop();
			} else if (event == XMLStreamConstants.CHARACTERS) {
				if (nestingStack.peek() != ParseTag.COMMON_TEXT) {
					continue;
				}
				
				if (!parsingStateMatches(new ParseTag[] { ParseTag.MESSAGE_GENERICDATA, 
														  ParseTag.FOOTER_FOOTER,
														  ParseTag.FOOTER_MESSAGE,
														  ParseTag.FOOTER_MESSAGE})) {
					logger.warn("Unexpected text node nesting level: " + nestingStack);
				}
				textMessages.add(parser.getText());
			}
		}
		
	}
	
	public int getCode() {
		return code;
	}
	
	public String getCodeDescription() {
		return errorCodeDescription.get(code);
	}
	
	public List<String> getText() {
		return textMessages;
	}
	
	public URL getRedirectURL() {
		if (code != 413) {
			return null;
		}
		
		try {
			// The redirect url is the second message.
			URL url = new URL(textMessages.get(1));
			return url;
		} catch (Exception e) {
			return null;
		}
	}
	
	private boolean parsingStateMatches(ParseTag[] state) {
		if (state.length != nestingStack.size()) {
			return false;
		}

		int i = 0;
		for (ParseTag tag : nestingStack) {
			if (tag != state[i++]) {
				return false;
			}
		}
		
		return true;
	}

	public Object getSeverity() {
		return severity;
	}
}
