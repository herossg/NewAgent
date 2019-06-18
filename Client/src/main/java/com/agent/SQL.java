package com.agent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SQL {
	static public String CreateMaster;
	static public String CreateDetail;
	static public String SelectMaster;
	static public String SelectDetail;
	
	public SQL(String file) 
	{
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			
			Node node = doc.getElementsByTagName("SQL").item(0);
			Element element = (Element)node;
			SQL.CreateMaster = getTagValue("MASTER_CREATE", element);
			SQL.CreateDetail = getTagValue("DETAIL_CREATE", element);
			SQL.SelectMaster = getTagValue("MASTER_SELECT", element);
			SQL.SelectDetail = getTagValue("DETAIL_SELECT", element);
			
			System.out.println("SQL : " + SQL.CreateDetail);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String getTagValue(String tag, Element element) {
		NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
			Node nValue = (Node) list.item(0);
		return nValue.getNodeValue();
	}
	
}
