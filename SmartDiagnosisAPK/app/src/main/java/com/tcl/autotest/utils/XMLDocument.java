package com.tcl.autotest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Environment;
import android.util.Log;

public class XMLDocument {

	/**
     * ����SDcard xml�ļ�
     * @param fileName
     * @return ����xml�ļ���inputStream
     */     
    public InputStream getInputStreamFromSDcard(String fileName){
        try {
            // ·������ʵ����Ŀ�޸�
            String path = Environment.getExternalStorageDirectory().toString() + "/";

            Log.v("", "path : " + path);
 
            File xmlFlie = new File(path+fileName);

            InputStream inputStream = new FileInputStream(xmlFlie);

            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /***
     * ��ӽڵ�
     * @param parentNode
     * @param nodeName
     * @param name
     * @param Value
     * @param fileName
     */
	public void addNode(String parentNode,String nodeName,String name,String Value,String fileName)
	{
		String path = Environment.getExternalStorageDirectory().toString() + "/";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	  File file = new File(path+fileName);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            Element eltName = doc.createElement(nodeName);             
            Attr attr = doc.createAttribute("name");
            attr.setValue(name);
            Attr attr2 = doc.createAttribute("value");
            attr2.setValue(Value==null?"":Value);
             
            eltName.setAttributeNode(attr);
            eltName.setAttributeNode(attr2);
         //   Node eltRootss = doc.getDocumentElement();
            //doc.getDocumentElement().appendChild(eltName);
            Node eltRoot = doc.getDocumentElement()
                    .getElementsByTagName(parentNode).item(0);
            if (eltRoot!=null){
                eltRoot.appendChild(eltName);
            }
            doc2XmlFile(doc, path+fileName);
          //  output(doc);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void addNode(String parentNode,String nodeName,String name,String fileName)
	{
		String path = Environment.getExternalStorageDirectory().toString() + "/";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	  File file = new File(path+fileName);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            Element eltName = doc.createElement(nodeName);             
            Attr attr = doc.createAttribute("name");
            attr.setValue(name);
             
            eltName.setAttributeNode(attr);
         //   Node eltRootss = doc.getDocumentElement();
            //doc.getDocumentElement().appendChild(eltName);
            Node eltRoot = doc.getDocumentElement()
                    .getElementsByTagName(parentNode).item(0);
            if (eltRoot!=null) {
                eltRoot.appendChild(eltName);
            }
            doc2XmlFile(doc, path+fileName);
          //  output(doc);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/*public  void output(Node node) {//��node��XML�ַ������������̨
        TransformerFactory transFactory=TransformerFactory.newInstance();
        try {
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("encoding", "gb2312");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");//��������Ϊ2
            

            DOMSource source=new DOMSource();
            source.setNode(node);
            StreamResult result=new StreamResult();
            result.setOutputStream(System.out);
            
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }*/
	/**
     * ��Document�����޸ĺ�д�뵽xml����
     * @param document Document����
     * @param filename xml�ļ�·��
     * @return
     */
    public boolean doc2XmlFile(Document document, String filename) {
        boolean flag = true;
        try {
            /** ��document�е�����д���ļ��� */
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            
            /** ���� */
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "6");//��������Ϊ2
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filename));
            transformer.transform(source, result);
            
        } catch (Exception ex) {
            flag = false;
           System.out.println("����" + filename + "����" + ex);
            Log.i("qinhao","����" + filename + "����" + ex);
            
          
            ex.printStackTrace();
        }
        return flag;
        
    }
  /* public  void insertCurrentInfoXML(String s,String fileName) throws TransformerException
   {
	   String path = Environment.getExternalStorageDirectory().toString() + "/";

       Log.v("", "path : " + path);

       //File xmlFlie = new File(path+fileName);
	   try {
		   File file = new File(path+fileName);
		   DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		   DocumentBuilder docbuder = dbf.newDocumentBuilder();
		   Document doc = docbuder.parse(file);
		   NodeList nl = doc.getElementsByTagName("currentChild");
		   for(int i=0;i<nl.getLength();i++){
			   
		   String bookName = doc.getElementsByTagName("name").item(i).getFirstChild().getNodeValue();
		   String bookAuthor = doc.getElementsByTagName("author").item(i).getFirstChild().getNodeValue();
		   String bookPageCount = doc.getElementsByTagName("pageCount").item(i).getFirstChild().getNodeValue();
		   String bookPrintDate = doc.getElementsByTagName("printDate").item(i).getFirstChild().getNodeValue();
		   String bookPrice = doc.getElementsByTagName("price").item(i).getFirstChild().getNodeValue();
		   String bookEmail = "";
		   NamedNodeMap nnm =  nl.item(i).getAttributes();
		  
		   bookEmail = nnm.item(1).getNodeValue();
		   nnm.item(1).setNodeValue("deded");
		   for(int j =0;j<nnm.getLength();j++){
		   
			   bookEmail = nnm.item(j).getNodeValue();
			   
		   }
	
		   TransformerFactory factory = TransformerFactory.newInstance();
           Transformer former = factory.newTransformer();
           former.transform(new DOMSource(doc), new StreamResult(file));

		   
		   }
		   } catch (ParserConfigurationException e) {
		   e.printStackTrace();
		   } catch (SAXException e) {
		   e.printStackTrace();
		   } catch (IOException e) {
		   e.printStackTrace();
		   }

     
   }*/
    /***
     * ����XML��ȡ�Զ�����������б�
     * @param inStream
     * @param parser
     * @return
     */
    public static ArrayList<Auto> ParseAutoXml(InputStream inStream, XmlPullParser parser){
    	
    	try {
			parser.setInput(inStream, "UTF-8");
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}// ��������Դ����  

        ArrayList<Auto> AutoArray = new ArrayList<Auto>();
        Auto AutoTemp = null;
        String deivceid;       
        String mName;
        String mIndex;

        try {
            //��ʼ�����¼�
            int eventType = parser.getEventType();

            //�����¼����������ĵ�������һֱ����
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //��Ϊ������һ�Ѿ�̬�������������������switch
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        //����ǰ��ǩ�������
                        String tagName = parser.getName();
                        //  Log.d("", "====XmlPullParser.START_TAG=== tagName: " + tagName);

                        /*if(tagName.equals("DeviceID")){
                           
                           // provinceId = Integer.parseInt(parser.getAttributeValue(0));
                            deivceid = parser.nextText().toString();
                            
                        }*/
                        if(tagName.equals("auto")){
                        	
                        	AutoTemp = new Auto();
                        	mIndex =parser.getAttributeValue(0);
                        	mName = parser.getAttributeValue(1);
                            AutoTemp.setIndex(mIndex);
                            AutoTemp.setAutoName(mName);
                            AutoArray.add(AutoTemp);
                            
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                }

                //��������next����������һ���¼������˵Ľ���ͳ���ѭ��#_#
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return AutoArray;
    }
    /***
     * ����XML��ȡ�ֶ����Զ����б�
     * @param inStream
     * @param parser
     * @return
     */
 public static ArrayList<Manual> ParseManualXml(InputStream inStream, XmlPullParser parser){
    	
    	try {
			parser.setInput(inStream, "UTF-8");
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}// ��������Դ����  

        ArrayList<Manual> ManualArray = new ArrayList<Manual>();
        Manual ManualTemp = null;
        String mName;
        String mIndex;

        try {
            //��ʼ�����¼�
            int eventType = parser.getEventType();

            //�����¼����������ĵ�������һֱ����
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //��Ϊ������һ�Ѿ�̬�������������������switch
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        //����ǰ��ǩ�������
                        String tagName = parser.getName();
                        //  Log.d("", "====XmlPullParser.START_TAG=== tagName: " + tagName);

                        /*if(tagName.equals("DeviceID")){
                           
                           // provinceId = Integer.parseInt(parser.getAttributeValue(0));
                            deivceid = parser.nextText().toString();
                            
                        }*/
                        if(tagName.equals("manual")){
                        	
                        	ManualTemp = new Manual();
                        	mIndex =parser.getAttributeValue(0);
                        	mName = parser.getAttributeValue(1);
                        	ManualTemp.setIndex(mIndex);
                        	ManualTemp.setManualName(mName);
                            ManualArray.add(ManualTemp);
                            
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                }

                //��������next����������һ���¼������˵Ľ���ͳ���ѭ��#_#
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ManualArray;
    }
    
 /***
  * ����XML��ȡ�Զ����ֶ���������б���Ŀ
  * @param inStream
  * @param parser
  * @return
  */
public static String ParseAutoManualListMaxXml(InputStream inStream, XmlPullParser parser,int type){
 	
 	try {
			parser.setInput(inStream, "UTF-8");
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}// ��������Դ����  

     ArrayList<AutoManualListMax> ManualArray = new ArrayList<AutoManualListMax>();
     AutoManualListMax Temp = null;
     String mName;
     String mIndex = null;
     int maxvalue = 0;
     
     try {
         //��ʼ�����¼�
         int eventType = parser.getEventType();

         //�����¼����������ĵ�������һֱ����
         while (eventType != XmlPullParser.END_DOCUMENT) {
             //��Ϊ������һ�Ѿ�̬�������������������switch
             switch (eventType) {
                 case XmlPullParser.START_DOCUMENT:
                     break;
                 case XmlPullParser.START_TAG:
                     //����ǰ��ǩ�������
                     String tagName = parser.getName();
                     //  Log.d("", "====XmlPullParser.START_TAG=== tagName: " + tagName);
                     if(type==0){
                    	 if(tagName.equals("AutoMaxListSet")){
                    		 mIndex =parser.getAttributeValue(0);
                          }
                     }else if(type==1){
                    	 if(tagName.equals("ManualMaxListSet")){
                    		 mIndex =parser.getAttributeValue(0);
                          }
                     }else if(type==2){
                    	 if(tagName.equals("AllMaxListSet")){
                    		 mIndex =parser.getAttributeValue(0);
                    	 }
                     }
                     break;
                 case XmlPullParser.END_TAG:
                     break;
                 case XmlPullParser.END_DOCUMENT:
                     break;
             }

             //��������next����������һ���¼������˵Ľ���ͳ���ѭ��#_#
             eventType = parser.next();
         }
     } catch (XmlPullParserException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }

     return mIndex;
 }
}
