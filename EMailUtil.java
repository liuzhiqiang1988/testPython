package com.leedarson.email.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import kdweibo4j.http.BASE64Encoder;

/**
 * 邮箱 LZQ 2018年5月17日
 */
@Service
public class EMailUtil {

	
	private  List<String> imgList =new ArrayList<>();
	private  String bodyText ="";
	
	static Logger log = LogManager.getLogger();

	@Value("${eMail.host}")
	private String host;
	@Value("${eMail.imap.port}")
	private Integer imapPort;
	@Value("${eMail.smtp.prot}")
	private Integer smtpPort;

	/**
	 * 发送文本邮件 LZQ 2018年5月18日
	 * 
	 * @param userName
	 * @param password
	 * @param title
	 * @param content
	 * @return
	 */
	public JSONObject sendEmailTextByUser(String userName, String password, String title, String content) {
		JSONObject back = new JSONObject();
		back.put("state", true);

		JavaMailSenderImpl jms = new JavaMailSenderImpl();
		jms.setUsername(userName);
		jms.setPassword(password);
		jms.setHost(host);
		jms.setPort(smtpPort);
		// 建立邮件消息
		SimpleMailMessage mainMessage = new SimpleMailMessage();
		// 发送者
		mainMessage.setFrom(userName);

		// 接收者
		mainMessage.setTo(userName);
		// 发送的标题
		mainMessage.setSubject(title);
		// 发送的内容
		mainMessage.setText(content);

		try {
			jms.send(mainMessage);
		} catch (Exception e) {
			log.error("邮件发送异常");
			log.error(e.getStackTrace());
			back.put("massage", "邮件发送异常");
			back.put("state", false);
			return back;
		}

		return back;
	}

	/**
	 * 获取邮件列表 LZQ 2018年5月18日
	 * 
	 * @param username
	 * @param password
	 * @return 返回邮件状态()
	 */
	public JSONObject getEmailList(String userName, String password) {
		JSONObject backData = new JSONObject();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		Session session = creadSession();

		IMAPFolder folder = null;
		IMAPStore store = null;
		try {
			// 使用imap会话机制，连接服务器
			store = (IMAPStore) session.getStore("imap");

			store.connect(host, imapPort, userName, password);
			Folder defaultFolder = store.getDefaultFolder();
			Folder[] allFolder = defaultFolder.list();
			for (int i = 0; i < allFolder.length; i++) {
				// 文件夹名称
				String folderName = allFolder[i].getFullName();

				folder = (IMAPFolder) store.getFolder(folderName);

				if (folder.getType() != 2) {
					JSONArray list = new JSONArray();
					backData.put(folderName, list);
					int size = folder.getMessageCount();
					if (!folder.isOpen()) {
						folder.open(Folder.READ_ONLY);
					}
					if (size > 0) {
						// folder.open(Folder.READ_ONLY);

						Message[] mass = folder.getMessages();

						// 遍历每个目录里文件
						for (int n = 0; n < mass.length; n++) {
							JSONObject item = new JSONObject();
							Message mas = mass[n];
							String state = mas.getFlags().toString();
							item.put("Seen", true); // Seen) 和是否标记（Flagged）
							item.put("Flagged", false);
							if (StringUtils.indexOf(state, "Seen") == -1) {
								item.put("Seen", false);
							}
							if (StringUtils.indexOf(state, "Flagged") > -1) {
								item.put("Flagged", true);
							}

							// item.put("title", mas.getSubject() );
							// item.put("date", sdf.format(mas.getSentDate()) );
							// item.put("from", mas.getFrom()[0].toString() );

							// item.getContentType()获得类型类型 是否有文件
							// item.getSize() 数据大小
							// item.getFlags() 是否看过(Seen) 和是否标记（Flagged）
							// item.getSubject() 标题
							// folder.getNewMessageCount() 获得新消息数
							// folder.getUnreadMessageCount() 获得未读消息数

							// item.getFrom()[0].toString 发送者
							// item.getFlags().contains(Flag.FLAGGED); 是否标记

							list.add(item);
						}
						backData.put(folderName, list);
					}
					if (folder != null) {
						folder.close(true);
					}
				}

			}

		} catch (MessagingException e) {
			e.printStackTrace();
			backData.put("error", "获取邮件列表信息异常");
			log.error("获取邮件列表异常");
			log.error(e.getStackTrace());
			return backData;
		} finally {
			try {

				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				log.debug("关闭邮箱链接异常");
			}
		}
		return backData;
	}

	/**
	 * 
	 * LZQ 创建连接 Session 2018年5月18日
	 * 
	 * @return
	 */
	public Session creadSession() {
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties props = System.getProperties();
		props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.imap.socketFactory.port", imapPort.toString());
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.imap.host", host);
		props.setProperty("mail.imap.port", imapPort.toString());
		props.setProperty("mail.imap.auth.login.disable", "true");
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(false);
		return session;
	}

	/**
	 *
	 * 打开某一个邮箱文件夹(分页)
	 * 
	 * @auhor yewenjie
	 * @Create_Date: 2018年5月22日上午10:16:23
	 * @last_modify by yewenjie at 2018年5月22日上午10:16:23 <br>
	 * @Why_and_What_is_modified:
	 * @param userName
	 * @param password
	 * @param folderName
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public JSONArray openFolder(String userName, String password, String folderName, int page, int pageSize) {
		JSONArray backData = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Session session = creadSession();
		int start = ((page - 1) * pageSize) + 1;
		int end = page * pageSize;
		IMAPFolder folder = null;
		IMAPStore store = null;
		try {
			// 使用imap会话机制，连接服务器
			store = (IMAPStore) session.getStore("imap");
			store.connect(host, imapPort, userName, password);

			// 文件夹名称
			folder = (IMAPFolder) store.getFolder(folderName);

			int size = folder.getMessageCount();
			folder.open(Folder.READ_ONLY);

			if (size > 0) {
				if (start > size) {
					return backData;
				}
				if (end > size) {
					end = size;
				}
				Message[] mass = folder.getMessages(start, end);
				for (int i = 0; i < mass.length; i++) {
					JSONObject item = new JSONObject();
					Message mas = mass[i];
					String state = mas.getFlags().toString();

					// TODO 测试点

					// 判断邮件类型
					if (mas.isMimeType("multipart/*")) {
						try {
							Multipart multipart = (Multipart) mas.getContent();
							int cont = multipart.getCount();

							// 多附件处理
							for (int ii = 0; ii < cont; ii++) {
								BodyPart body = multipart.getBodyPart(ii);

								// 获得附件文件名称
								String fileName = body.getFileName();
								
								System.out.println("-------");
								System.out.println(folder.getUID(mas));
								System.out.println(fileName);
								
								if (fileName != null) {

									if (body.isMimeType("image/*")) {
										System.out.println("是图片");
										InputStream is = body.getInputStream();
										int a = body.getSize();

										byte[] b = new byte[a];
										is.read(b);
										is.close();

									}

								}

							}

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}

					// TODO 测试点结束

					item.put("Seen", true); // Seen) 和是否标记（Flagged）
					item.put("Flagged", false);
					if (StringUtils.indexOf(state, "Seen") == -1) {
						item.put("Seen", false);
					}
					if (StringUtils.indexOf(state, "Flagged") > -1) {
						item.put("Flagged", true);
					}
					Address[] arr = mas.getRecipients(Message.RecipientType.TO);
					Address[] from = mas.getFrom();
					if (from != null) {
						item.put("from", mas.getFrom()[0].toString());// 发件人
					} else {
						item.put("from", "");// 发件人
					}

					InternetAddress address = (InternetAddress) arr[0];
					String personal = address.getPersonal();
					if (personal != null) {
						String address2 = address.getAddress();
						item.put("to", address2);// 只拿第一个收件人
					} else {
						item.put("to", "");// 只拿第一个收件人
					}
					item.put("title", mas.getSubject());
					item.put("date", sdf.format(mas.getSentDate()));
					item.put("id", folder.getUID(mas));
					//
					// try {
					// String allMultipart = getAllMultipart(mas);
					// } catch (Exception e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					backData.add(item);
				}
			}

		} catch (MessagingException e) {

			JSONObject error = new JSONObject();
			error.put("error", "获取邮件信息异常");
			backData.add(error);
			log.error("获取邮件列表异常");
			return backData;
		} finally {
			try {
				if (folder != null) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				log.debug("关闭邮箱链接异常");
			}
		}

		return backData;
	}

	/**
	 *
	 * 综合解析邮箱内容
	 * 
	 * @auhor yewenjie
	 * @Create_Date: 2018年5月22日上午10:03:54
	 * @last_modify by yewenjie at 2018年5月22日上午10:03:54 <br>
	 * @Why_and_What_is_modified:
	 * @param part
	 *            //Messages and BodyParts.
	 * @return
	 * @throws Exception
	 */
	public String getAllMultipart(Part part) throws Exception {
		String contentType = part.getContentType();
		int index = contentType.indexOf("name");
		boolean conName = false;
		if (index != -1) {
			conName = true;
		}
		// 判断part类型
		if (part.isMimeType("text/plain") && !conName) {
			System.out.println("b"+(String) part.getContent());
		} else if (part.isMimeType("text/html") && !conName) {
			bodyText=part.getContent().toString();
			System.out.println("a"+(String) part.getContent());
		} else if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int counts = multipart.getCount();
			for (int i = 0; i < counts; i++) {
				// 递归获取数据
				getAllMultipart(multipart.getBodyPart(i));
				// 附件可能是截图或上传的(图片或其他数据)
				if (multipart.getBodyPart(i).getDisposition() != null) {
					// 附件为截图
					if (multipart.getBodyPart(i).isMimeType("image/*")) {
						
					
						InputStream is = multipart.getBodyPart(i).getInputStream();
						 byte[] data =  readInputStream(is);
		                 BASE64Encoder encode = new BASE64Encoder();  
		                 String s = encode.encode(data);  
		                 imgList.add(s);
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			getAllMultipart((Part) part.getContent());
		}
		return "";
	}


	// 获取某一文件详细信息
	public JSONObject getEmailInfoById(String userName, String password, String folderName, Long id,HttpServletResponse response) {
		JSONObject backData = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Session session = creadSession();
		imgList=new ArrayList<>();
		bodyText="";
		IMAPFolder folder = null;
		IMAPStore store = null;
		try {
			// 使用imap会话机制，连接服务器
			store = (IMAPStore) session.getStore("imap");
			store.connect(host, imapPort, userName, password);

			// 文件夹名称
			folder = (IMAPFolder) store.getFolder(folderName);
			int size = folder.getMessageCount();
			folder.open(Folder.READ_ONLY);

			Message mas = folder.getMessageByUID(id);

		
			backData.put("title", mas.getSubject());
			backData.put("date", sdf.format(mas.getSentDate()));
			backData.put("from", decodeText(mas.getFrom()[0].toString()));
			backData.put("id", folder.getUID(mas));
			// TODO 获取详细 收件人
			JSONArray mails = getMailpartName(userName,password,folderName,id);
			backData.put("mails", mails);
	
			
		
		    Multipart multipart = (Multipart) mas.getContent();
	         int count = multipart.getCount();    // 部件个数
	         for(int i=0; i<count; i++) {
	             // 单个部件     注意：单个部件有可能又为一个Multipart，层层嵌套
	             BodyPart part = multipart.getBodyPart(i);
	             // 单个部件类型
	             String type = part.getContentType().split(";")[0];
	             /**
	              * 类型众多，逐一判断，其中TEXT、HTML类型可以直接用字符串接收，其余接收为内存地址
	              * 可能不全，如有没判断住的，请自己打印查看类型，在新增判断
	              */
	             if(type.equals("multipart/alternative")) {        // HTML （文本和超文本组合）
	                 System.out.println("超文本:" + part.getContent().toString());
	                 getAllMultipart(part);
	                 //backData.put("content",part.getContent());
	             }else if(type.equals("text/plain")) {    // 纯文本
	                 System.out.println("纯文本:" + part.getContent().toString());
	                 backData.put("content",part.getContent());
	             }else if(type.equals("text/html")){    // HTML标签元素
	                 System.out.println("HTML元素:" + part.getContent().toString());
	                 backData.put("content",part.getContent());
	             }else if(type.equals("multipart/related")){    // 内嵌资源 (包涵文本和超文本组合)
	                 System.out.println("内嵌资源:" + part.getContent().toString());
	                 getAllMultipart(part);
	             //	backData.put("aa", part.getContent());
	             }else if(type.contains("application/")) {        // 应用附件 （zip、xls、docx等）
	                 System.out.println("应用文件：" + part.getContent().toString());
	       
	                 
	             }else if(type.contains("image/")) {            // 图片附件 （jpg、gpeg、gif等）
	                 System.out.println("图片文件：" + part.getContent().toString());

	                 byte[] data =  readInputStream(part.getInputStream());
	                 BASE64Encoder encode = new BASE64Encoder();  
	                 String s = encode.encode(data);  
	                 imgList.add(s);
	             }
	            
	             
	         }
	         if(imgList.size()>0){
	        	 backData.put("imgList",imgList);
             }
	         if(!"".equals(bodyText)){
	        	 backData.put("content",bodyText);
	         }
		} catch (MessagingException e) {
			backData.put("error", "获取邮件信息异常");
			log.error("获取邮件列表异常");
			return backData;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (folder != null) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				log.debug("关闭邮箱链接异常");
			}
		}

		return backData;
	}
	
	  /**
	 * 将图片流转为二进制
	 *
	 * @auhor wangzhenhong
	 * @Create_Date: 2018年5月25日上午10:19:16
	 * @last_modify by wangzhenhong at 2018年5月25日上午10:19:16 <br>
	 * @Why_and_What_is_modified:
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	private static byte[] readInputStream(InputStream inStream) throws Exception{  
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
	        //创建一个Buffer字符串  
	        byte[] buffer = new byte[1024];  
	        //每次读取的字符串长度，如果为-1，代表全部读取完毕  
	        int len = 0;  
	        //使用一个输入流从buffer里把数据读取出来  
	        while( (len=inStream.read(buffer)) != -1 ){  
	            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度  
	            outStream.write(buffer, 0, len);  
	        }  
	        //关闭输入流  
	        inStream.close();  
	        //把outStream里的数据写入内存  
	        return outStream.toByteArray();  
	    }  

	
	/**
	 * 获得邮件附件内容
	 *
	 * @auhor wangzhenhong
	 * @Create_Date: 2018年5月25日下午3:08:09
	 * @last_modify by wangzhenhong at 2018年5月25日下午3:08:09 <br>
	 * @Why_and_What_is_modified:
	 * @param userName
	 * @param password
	 * @param folderName
	 * @param id
	 * @return
	 */
	public JSONArray getMailpartName(
			String userName, 
			String password,
			String folderName, 
			Long id) {
		
		JSONArray MailList=new JSONArray();
		
		Session session = creadSession();
		IMAPFolder folder = null;
		IMAPStore store = null;
		
		try {
			store = (IMAPStore) session.getStore("imap");
			store.connect(host, imapPort, userName, password);
			folder = (IMAPFolder) store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message mas = folder.getMessageByUID(id);
			// 判断邮件类型
			if (mas.isMimeType("multipart/*")) {
				try {
					
					Multipart multipart = (Multipart) mas.getContent();
					int cont = multipart.getCount();
					// 多附件处理
					for (int ii = 0; ii < cont; ii++) {
						BodyPart body = multipart.getBodyPart(ii);
						JSONObject MailOne=new JSONObject();
						 byte[] data;
						
			                
						
						// 获得附件文件名称
						String fileName = body.getFileName();
						if(fileName != null){
							try {
								data = readInputStream(body.getInputStream());
								BASE64Encoder encode = new BASE64Encoder();  
				                String s = encode.encode(data);
				                float bb =(float) ((s.length()/1024)*0.75);
				                System.out.println(bb);
				                MailOne.put("name", decodeText(fileName));
								MailOne.put("size", bb+"K");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							MailList.add(MailOne);
						}
					}
				} catch (IOException e1) {
					log.error("获取邮件附件异！");
				}
			}
		}catch (MessagingException e) {
			log.error("获取邮件附件异常");
			
		}finally {
			
			try {
				if(folder.isOpen()){
					folder.close(true);
				}
				
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				log.debug("关闭邮箱链接异常");
			}
		}
		
		return MailList;
		
	}
	
    /** 
     * 文本解码   wzh
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本 
     * @return 解码后的文本 
     * @throws UnsupportedEncodingException 
     */  
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {  
        if (encodeText == null || "".equals(encodeText)) {  
            return "";  
        } else {  
            return MimeUtility.decodeText(encodeText);  
        }  
    } 
	
	/**
	 * 获取邮件附件 LZQ 2018年5月22日
	 * 
	 * @param userName
	 * @param password
	 * @param folderName
	 *            文件夹名称
	 * @param id
	 *            邮件id
	 * @param partName
	 *            附件名称
	 * @return 文件流
	 */
	public void getMailAttachments(String userName, String password, String folderName, 
			Long id,String partName,HttpServletResponse response) {
		
		Session session = creadSession();
		IMAPFolder folder = null;
		IMAPStore store = null;
		try {
			store = (IMAPStore) session.getStore("imap");
			store.connect(host, imapPort, userName, password);
			folder = (IMAPFolder) store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			Message mas = folder.getMessageByUID(id);
			// 判断邮件类型
			if (mas.isMimeType("multipart/*")) {
				try {
					Multipart multipart = (Multipart) mas.getContent();
					int cont = multipart.getCount();
					// 多附件处理
					for (int ii = 0; ii < cont; ii++) {
						BodyPart body = multipart.getBodyPart(ii);
						// 获得附件文件名称
						String fileName = body.getFileName();
						if(fileName != null){
							if (fileName.equals(partName)) {
								response.setCharacterEncoding("utf-8");
								response.setContentType(body.getContentType() + ";charset=utf-8");
								response.setHeader("Content-Disposition", "attachment; filename="+ fileName);
								
								
			
								//TODO    未解决中文乱码问题
//								response.setHeader("Accept-Languagen", "zh-CN,zh;");
//								JSONObject oo = new JSONObject();
//								oo.put("err", "是我12naq");
//								org.apache.catalina.connector.ResponseFacade
//								String aa = "显示中文错误名称 哪么name";
//								String aa1 =  new String("显示中文错误名称 哪么name".getBytes(),"UTF-8");
//								String aa2 =   new String("显示中文错误名称 哪么name".getBytes(),"UTF-16") ;
//								String aa3 =   new String("显示中文错误名称 哪么name".getBytes(),"ASCII");
//								String aa4 =  new String("显示中文错误名称 哪么name".getBytes(),"GB2312") ;
//								String aa5 =  new String("显示中文错误名称 哪么name".getBytes(),"GBK")  ;
//								String aa6 =  new String("显示中文错误名称 哪么name".getBytes(),"ISO-8859-1")  ;
//								
//								response.setHeader("errerMessage1",aa);
//								response.setHeader("errerMessage2", aa1  );
//								response.setHeader("errerMessage3",  aa2);
//								response.setHeader("errerMessage4",  aa3 );
//								response.setHeader("errerMessage5",   aa4);
//								response.setHeader("errerMessage6",  aa5);
//								response.setHeader("errerMessage7", aa6 );
//
//							System.out.println(response.getCharacterEncoding());
//							System.out.println(aa);
//							System.out.println(aa1);
//							System.out.println(aa2);
//							System.out.println(aa3);
//							System.out.println(aa4);
//							System.out.println(aa5);
//							System.out.println(aa6);
						
							
								BufferedInputStream bis = new BufferedInputStream(body.getInputStream()) ;
								BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());

								int size;
								while((size=bis.read())>-1){
									bos.write(size);
								}
								bis.close();
								bos.close();
							}
						}
					}
				} catch (IOException e1) {
					log.error("获取邮件附件异！");
				}
			}
			
			
			if (folder != null) {
				folder.close(true);
			}
			
		} catch (MessagingException e) {
			log.error("获取邮件附件异常");
			
		}finally {
			
			try {
				if(folder.isOpen()){
					folder.close(true);
				}
				
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				log.debug("关闭邮箱链接异常");
			}
		}

	}
	
	
	
	



}
