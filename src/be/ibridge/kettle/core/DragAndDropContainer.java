/**
 * 
 */
package be.ibridge.kettle.core;

import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.exception.KettleXMLException;

/**
 * This class contains code to help you drag data from one 
 * part of a GUI to another by using XML as an intermediate step.
 *  
 * @author matt
 * @since 2006-04-16
 *
 */
public class DragAndDropContainer implements XMLInterface
{
	public static final int TYPE_STEP                = 1;
	public static final int TYPE_BASE_STEP_TYPE      = 2;
	public static final int TYPE_DATABASE_CONNECTION = 3;
	public static final int TYPE_TRANS_HOP           = 4;
	public static final int TYPE_TEXT                = 5;
	
	private static final String typeCodes[] = { "", "Step", "BaseStep", "DatabaseConnection", "TransHop", "Text" };
	
	private int type;
	private String data;
	
	/** 
	 * Create a new DragAndDropContainer
	 * 
	 * @param type The type of drag&drop to perform
	 * @param data The data in the form of a String
	 */
	public DragAndDropContainer(int type, String data)
	{
		this.type = type;
		this.data = data;
	}

	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public String getData()
	{
		return data;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
	public String getTypeCode()
	{
		if (type<=0 || type>=typeCodes.length) return null;
		
		return typeCodes[type];
	}
	
	public static final int getType(String typeCode)
	{
		for (int i=1;i<typeCodes.length;i++)
		{
			if (typeCodes[i].equals(typeCode)) return i;
		}
		return 0;
	}
	
	public String getXML()
	{
		try
		{
			StringBuffer xml = new StringBuffer();
			
			xml.append(XMLHandler.getXMLHeader()); // UFT-8 XML header
			
			xml.append("<DragAndDrop>"+Const.CR);
			xml.append("  "+XMLHandler.addTagValue("DragType", getTypeCode()));
			xml.append("  "+XMLHandler.addTagValue("Data", Base64.encodeBytes(data.getBytes(Const.XML_ENCODING))));
			xml.append("</DragAndDrop>"+Const.CR);
			
			return xml.toString();
		}
		catch(UnsupportedEncodingException e)
		{
			throw new RuntimeException("Unable to encode String in encoding ["+Const.XML_ENCODING+"]", e);
		}
	} 

	/**
	 * Construct a Drag and drop container from an XML String
	 * @param xml The XML string to convert from
	 */
	public DragAndDropContainer(String xml) throws KettleXMLException
	{
		try
		{
			Document doc = XMLHandler.loadXMLString(xml);
			Node dnd = XMLHandler.getSubNode(doc, "DragAndDrop"); 
			
			type = getType( XMLHandler.getTagValue(dnd, "DragType") );
			data = new String( Base64.decode( XMLHandler.getTagValue(dnd, "Data") ), Const.XML_ENCODING );
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unexpected error parsing Drag & Drop XML fragment: "+xml, e);
		}
	}
}