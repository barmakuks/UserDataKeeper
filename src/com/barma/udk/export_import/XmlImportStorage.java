package com.barma.udk.export_import;

import com.barma.udk.core.Record;
import com.barma.udk.core.interfaces.ICipher;
import com.barma.udk.core.interfaces.IStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Import from XML file storage implementation. Only read data methods are implemented
 * Created by Vitalii Misiura on 12/24/14.
 */
public class XmlImportStorage implements IStorage {

    public XmlImportStorage(String dir, String filename) {
        super();
        m_directory = dir;
        m_filename = filename;
    }

    @Override
    public void saveBegin() {
        // Do nothing
    }

    @Override
    public void saveRecord(Record record) {
        // Do nothing
    }

    @Override
    public void removeRecord(Record record) {
        // Do nothing
    }

    @Override
    public void saveTemplate(Record template) {
        // Do nothing
    }

    @Override
    public void removeTemplate(Record template) {
        // Do nothing
    }

    @Override
    public void saveEnd() {
        // Do nothing
    }

    @Override
    public boolean load() {
        try {
            clear();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(m_directory, m_filename);
            Document doc = builder.parse(file);
            Element root = doc.getDocumentElement();
            NodeList children = root.getChildNodes();

            for (int i = 0; i < children.getLength(); ++i) {
                Node node = children.item(i);
                final String node_name = node.getNodeName();
                boolean result = true;

                if (NodeNames.SECURITY_ROOT.equals(node_name)) {
                    result = parseSecurityNode(node);
                }
                else if (NodeNames.RECORDS_ROOT.equals(node_name)) {
                    result = parseRecordsNode(m_records, NodeNames.RECORD_NODE, node);
                }
                else if (NodeNames.TEMPLATES_ROOT.equals(node_name)) {
                    result = parseRecordsNode(m_templates, NodeNames.TEMPLATE_NODE, node);
                }

                if (!result) {
                    clear();
                    return false;
                }
            }

            return true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean checkIntegrity(ICipher cipher) {

        if (cipher != null) {
            return isPasswordSet() && m_password_hash.equals(cipher.getPasswordHash());
        }
        else {
            return !isPasswordSet();
        }
    }

    @Override
    public boolean isPasswordSet() {
        return m_password_hash != null && !m_password_hash.isEmpty();
    }

    @Override
    public void setupPassword(String passwordHash) {
        // Do nothing
    }

    @Override
    public int getRecordCount() {
        return m_records.size();
    }

    @Override
    public Record getRecord(int index) {

        if (index >= 0 && index < getRecordCount()) {
            return m_records.get(index);
        }

        return null;
    }

    @Override
    public int getTemplatesCount() {
        return m_templates.size();
    }

    @Override
    public Record getTemplate(int index) {

        if (index >= 0 && index < getTemplatesCount()) {
            return m_templates.get(index);
        }

        return null;
    }

    @Override
    public void clear() {
        m_password_hash = null;
        m_records.clear();
        m_templates.clear();
    }

    private static boolean parseRecordsNode(ArrayList<Record> records, final String record_name, Node node) {
        records.clear();
        NodeList children = node.getChildNodes();

        for (int i = 0 ; i < children.getLength(); ++i) {
            final Node child = children.item(i);

            if (record_name.equals(child.getNodeName()) && !parseRecord(records, child)) {
                return false;
            }
        }
        return true;
    }

    private static boolean parseRecord(ArrayList<Record> records_list,  Node node) {

        try {
            Record record = new Record();

            Node name_attr = node.getAttributes().getNamedItem(NodeNames.RECORD_NAME);
            Node id_attr = node.getAttributes().getNamedItem(NodeNames.RECORD_ID);

            String id_value = id_attr.getNodeValue();

            if (!id_value.isEmpty()) {
                record.setId(UUID.fromString(id_value));
            }

            record.setPublicName(name_attr.getNodeValue());

            final NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);

                if (NodeNames.ITEM_NODE.equals(child.getNodeName()) && !parseItem(record, child)) {
                    return false;
                }
            }
            records_list.add(record);

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    private static boolean parseItem(Record record, Node node) {
        Node key = node.getAttributes().getNamedItem(NodeNames.ITEM_KEY);
        Node value = node.getAttributes().getNamedItem(NodeNames.ITEM_VALUE);

        if (key == null) {
            return false;
        }

        String key_str = key.getNodeValue();

        String value_str = "";
        if (value != null) {
            value_str = value.getNodeValue();
        }

        record.addItem(key_str, value_str);

        return true;
    }

    private boolean parseSecurityNode(Node node) {
        Node password_hash = node.getAttributes().getNamedItem(NodeNames.PASSWORD_ATTR_NAME);

        if (password_hash != null) {
            m_password_hash = password_hash.getNodeValue();

            return true;
        }

        return false;
    }


    final private String m_directory;
    final private String m_filename;

    private String m_password_hash;
    private ArrayList<Record> m_records = new ArrayList<Record>();
    private ArrayList<Record> m_templates = new ArrayList<Record>();
}
