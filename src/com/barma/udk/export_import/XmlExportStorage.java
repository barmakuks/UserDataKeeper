package com.barma.udk.export_import;

import android.os.Environment;
import android.util.Log;
import com.barma.udk.core.Record;
import com.barma.udk.core.interfaces.ICipher;
import com.barma.udk.core.interfaces.IStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Properties;

/**
 * Export data into XML file. Only save data methods are implemented
 * Created by Vitalii Misiura on 11/13/14.
 */
public class XmlExportStorage implements IStorage {

    public XmlExportStorage(String directory, String filename) {
        m_filename = filename;
        m_directory = directory;
    }

    @Override
    public void saveBegin() {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            m_document = builder.newDocument();

            m_root = m_document.createElement(NodeNames.ROOT_NAME);
            m_document.appendChild(m_root);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupPassword(String passwordHash) {
        Element securityNode = m_document.createElement(NodeNames.SECURITY_ROOT);
        securityNode.setAttribute(NodeNames.PASSWORD_ATTR_NAME, passwordHash);
        m_root.appendChild(securityNode);
    }

    @Override
    public void saveRecord(Record record) {

        if (m_recordsRoot == null)
        {
            m_recordsRoot = m_document.createElement(NodeNames.RECORDS_ROOT);
            m_root.appendChild(m_recordsRoot);
        }

        Element recordNode = m_document.createElement(NodeNames.RECORD_NODE);
        recordNode.setAttribute(NodeNames.RECORD_NAME, record.getPublicName());
        recordNode.setAttribute(NodeNames.RECORD_ID, record.getId().toString());

        for (int i = 0; i < record.getItemsCount(); ++i)
        {
            Record.Item item = record.getItem(i);
            Element itemNode = m_document.createElement(NodeNames.ITEM_NODE);
            itemNode.setAttribute(NodeNames.ITEM_KEY, item.key);
            itemNode.setAttribute(NodeNames.ITEM_VALUE, item.value);
            recordNode.appendChild(itemNode);
        }

        m_recordsRoot.appendChild(recordNode);
    }

    @Override
    public void removeRecord(Record record) {
        // Do nothing
    }

    @Override
    public void saveTemplate(Record template) {

        if (m_templatesRoot == null) {
            m_templatesRoot = m_document.createElement(NodeNames.TEMPLATES_ROOT);
            m_root.appendChild(m_templatesRoot);
        }

        Element recordNode = m_document.createElement(NodeNames.TEMPLATE_NODE);
        recordNode.setAttribute(NodeNames.RECORD_NAME, template.getPublicName());
        recordNode.setAttribute(NodeNames.RECORD_ID, template.getId().toString());

        for (int i = 0; i < template.getItemsCount(); ++i) {
            Record.Item item = template.getItem(i);
            Element itemNode = m_document.createElement(NodeNames.ITEM_NODE);
            itemNode.setAttribute(NodeNames.ITEM_KEY, item.key);
            recordNode.appendChild(itemNode);
        }

        m_templatesRoot.appendChild(recordNode);
    }

    @Override
    public void removeTemplate(Record template) {
        // Do nothing
    }

    @Override
    public void saveEnd() {

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Properties outFormat = new Properties();
            outFormat.setProperty(OutputKeys.INDENT, "yes");
            outFormat.setProperty(OutputKeys.METHOD, "xml");
            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            outFormat.setProperty(OutputKeys.VERSION, "1.0");
            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperties(outFormat);

            DOMSource domSource = new DOMSource(m_document.getDocumentElement());
            OutputStream output = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
            String xmlString = output.toString();

            if (isExternalStorageWritable()) {
                File file = getDocumentsStorageDir(m_directory, m_filename);
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(xmlString.getBytes());
                outputStream.close();
            }

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean load() {
        // Do nothing
        return false;
    }

    @Override
    public boolean checkIntegrity(ICipher cipher) {
        // Do nothing
        return false;
    }

    @Override
    public boolean isPasswordSet() {
        // Do nothing
        return false;
    }

    @Override
    public int getRecordCount() {
        // Do nothing
        return 0;
    }

    @Override
    public Record getRecord(int i) {
        // Do nothing
        return null;
    }

    @Override
    public int getTemplatesCount() {
        // Do nothing
        return 0;
    }

    @Override
    public Record getTemplate(int i) {
        // Do nothing
        return null;
    }

    @Override
    public void clear() {
        // Do nothing
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static private File getDocumentsStorageDir(String directory, String docName) throws IOException {
        final File dir = new File(directory);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("Data export", "Directory not created");
            }
        }

        File file = new File(dir, docName);

        if (!file.createNewFile()) {
            Log.e("Data export", "file not created");
        }
        return file;
    }



    private final String  m_filename;
    private final String  m_directory;
    private Document      m_document;
    private Element       m_root;
    private Element       m_recordsRoot;
    private Element       m_templatesRoot;
}
