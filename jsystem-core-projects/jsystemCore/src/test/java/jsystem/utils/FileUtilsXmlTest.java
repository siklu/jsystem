package jsystem.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileUtilsXmlTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void savesXmlReportUsingRuntimeTransformer() throws Exception {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element root = document.createElement("report");
		root.setTextContent("Test result");
		document.appendChild(root);
		File output = folder.newFile("report.xml");

		FileUtils.saveDocumentToFile(document, output);

		Document saved = FileUtils.readDocumentFromFile(output);
		assertEquals("report", saved.getDocumentElement().getTagName());
		assertEquals("Test result", saved.getDocumentElement().getTextContent());
	}
}
