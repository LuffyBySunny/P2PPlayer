package com.itheima.ck.web;


import com.itheima.ck.bean.FileBean;
import com.itheima.ck.bean.FileUploadBean;
import com.itheima.ck.bean.MovieBean;
import com.itheima.ck.bean.MovieDao;
import com.itheima.ck.utils.FileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.invoke.VolatileCallSite;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

// 一个http上传大文件实践
public class UploadController extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(UploadController.class);

    private static String finalDirPath = "/var/www/html/datasource/";
    private static final String IPDATASOURCEADDRESS = "http://192.168.43.68/datasource/";
    private static final String IPTORRENTADDRESS = "http://192.168.43.68/torrent/";
    private String movieName;
    private String movieDetails;
    
    private int chunks;
    
    private HttpServletRequest request;
    
    private String fileName;
    
    
    
    private static final String HOSTIP = "188.131.249.47"; 

    private String tempPath;
    private Map<String, FileBean> fileMap = Collections.synchronizedMap(new HashMap<String, FileBean>());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	
        boolean multipartContent = ServletFileUpload.isMultipartContent(req);
        // 是文件上传请求
        if (multipartContent) {
        	if (request == null) {
    			request = req;
    		}
            // 获取请求长度
            int length = req.getIntHeader("Content-Length");
           // logger.info("用户请求的长度为:{}", length);

            // 创建工厂（这里用的是工厂模式）
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //获取汽车零件清单与组装说明书（从ServletContext中得到上传来的数据）
            ServletContext servletContext = this.getServletConfig().getServletContext();
            // 临时文件目录
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            //工厂把将要组装的汽车的参数录入工厂自己的系统，因为要根据这些参数开设一条生产线（上传来的文件的各种属性）
            factory.setRepository(repository);
            //此时工厂中已经有了汽车的组装工艺、颜色等属性参数（上传来的文件的大小、文件名等）
            //执行下面的这一行代码意味着根据组装工艺等开设了一条组装生产线
            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> fileItems;
            try {
                fileItems = upload.parseRequest(req);
               
                //拿到movie的标题和描述
                for (FileItem item : fileItems) {
                	
					if ("moviename".equals(item.getFieldName())) {
						movieName = item.getString("utf-8");
					
					}
					if ("details".equals(item.getFieldName())) {
						movieDetails = item.getString("utf-8");
				
					}
				}
            } catch (FileUploadException e) {
                logger.error("解析请求出现异常", e);
                return;
            }
            FileUploadBean param = new FileUploadBean(fileItems, logger);

            if (fileName != null) {
				fileName = param.getName();
			}
            chunks = param.getChunks();
            // 然后存储
            String fileName = param.getName();
            String uploadDirPath = finalDirPath + param.getMd5();
            if (tempPath ==null) {
				tempPath = uploadDirPath;
			}
            String tempFileName = fileName + "_" + param.getChunk() + "_tmp";
            Path tmpDir = Paths.get(uploadDirPath);
            //防止多线程并发的问题
            if (!Files.exists(tmpDir)) {
                synchronized (UploadController.class) {
                    if (!Files.exists(tmpDir)) {
                        Files.createDirectory(tmpDir);
                    }
                }
            } else {
                // 文件夹已存在, 先检测是否完成收集
                // 1.检查是否有文件,有进入2, 没有进3
                Path localPath = Paths.get(finalDirPath, fileName);

                // 2.检查md5值是否匹配, 应该建立数据库,存储文件信息才是更快 更好的解决办法.
                // 2.1.若匹配直接返回成功.
                // 2.2 若不成功,删除源文件再次读取
                if (Files.exists(localPath)) {
                    String nowMd5 = DigestUtils.md5Hex(Files.newInputStream(localPath, StandardOpenOption.READ));
                    if (StringUtils.equals(param.getMd5(), nowMd5)) {
                        // 比较相等,那么直接返回成功.
                        logger.info("已检测到重复文件{},并且比较md5相等,已直接返回", fileName);
                        throw new IIOException("文件已存在");
                    } else {
                        // 删除
                        logger.info("已经存在的文件的md5不匹配上传上来的文件的md5,删除后重新下载");
                        Files.delete(localPath);
                    }
                }
                
            }

            // 3. 直接写入到具体目录下.
            //写入该分片数据
            // 0.读取上传文件到数组
            // 1.写到本地
            // 1.记录分片数,检查分片数
            // 2.当对应的md5读取数量达到对应的文件后,合并文件
            // 3.删除临时文件
            Path path = Paths.get(uploadDirPath, tempFileName);
            //文件上传时,获取是否有分片,如果有直接返回.
            if (!Files.exists(path)) {
                // 不存在.
                byte[] fileData = FileUtils.read(param.getFile(), 2048);
                try {
                    Files.write(path, fileData, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    // 删除上传的文件
                    Files.delete(path);
                    throw e;
                }
                FileUtils.authorizationAll(path);
            }

            FileBean fileBean;
            if (fileMap.containsKey(param.getMd5())) {
                fileBean = fileMap.get(param.getMd5());
            } else {
                fileBean = new FileBean(param.getName(), param.getChunks(), param.getMd5());
                fileMap.put(param.getMd5(), fileBean);
            }
            //Stream.iterate(0, (value) -> value).limit(param.getChunk()).forEach(System.out::println);
            fileBean.addIndex(param.getChunk());

           
            File[] files = new File(uploadDirPath).listFiles();
            //如果上传完毕
            if (files.length == chunks) {
                // 合并文件..不用合并到MD5文件夹下
            	   Path  realFile = Paths.get(finalDirPath, fileBean.getName());
            	   
            	 if (!Files.exists(realFile)) {
            		 synchronized (UploadController.class) {
                 		if (!Files.exists(realFile)) {
                 			 realFile = Files.createFile(realFile);
                 			// 设置权限
                             FileUtils.authorizationAll(realFile);
                             for (int i = 0; i < fileBean.getChunks(); i++) {
                                 // 获取每个分片
                                 tempFileName = fileName + "_" + i + "_tmp";
                                 //System.out.println("文件名是"+tempFileName);
                                 Path itemPath = Paths.get(uploadDirPath, tempFileName);
                                 byte[] bytes = Files.readAllBytes(itemPath);
                                 Files.write(realFile, bytes, StandardOpenOption.APPEND);
                                 //写完后删除掉临时文件.
                                 Files.delete(itemPath);
                             }
                             //删除MD5文件夹
                             new File(finalDirPath + param.getMd5()).delete();
                            
                             //制作种子
                             new Thread(new Runnable() {
             					
             					@Override
             					public void run() {
             						String ipString="";
             						try {
             							ipString = getLocalHostLANAddress().getHostAddress();
             						} catch (UnknownHostException e1) {
             							// TODO Auto-generated catch block
             							e1.printStackTrace();
             						}						
             						
             						System.out.println("本机的IP地址是" + ipString);
             						String makeTorrentString = "/usr/local/bin/btmaketorrent.py "+"http://"+"188.131.249.47"+":6969/announce "+finalDirPath + fileBean.getName();	
             						String torrentPath = finalDirPath + fileName + ".torrent";
             						String moveTorrent = "mv " + torrentPath + " /var/www/html/torrent/";
             						String lnFileString = "ln " + finalDirPath + fileBean.getName() + " /var/www/html/torrent/";
             						//制作种子
             						UploadController.this.executeLinuxCmd(makeTorrentString);
             						executeLinuxCmd(moveTorrent);
             						executeLinuxCmd(lnFileString);
             						Path path = Paths.get("/var/www/html/torrent/"+fileName+ ".torrent");
             						FileUtils.authorizationAll(path);
             						
             						
             						//在这插入数据库
             		                try {
             							if (MovieDao.getInstance().getConnection() != null) {
             								//连接成功	
             								if (!MovieDao.getInstance().ifExsts(movieName)) {
             									//插入表
             									
             									MovieBean movie = new MovieBean();
             									movie.name = movieName;
             									movie.details = movieDetails;
             									movie.datasourcePath = "http://" +"188.131.249.47" +"/datasource/" + fileBean.getName();
                             					movie.imagePathString = "";
                             					movie.torrentpathString = "http://" +"188.131.249.47" +"/torrent/" + fileName + ".torrent";
             									MovieDao.getInstance().addMovie(movie);
             								}else {
             									//更新表
             					
             									String sqlString = "update movie set details=?,datasourcePath=?,torrentpathString=? where name=?";
             									PreparedStatement preparedStatement = MovieDao.getInstance().getConnection().prepareStatement(sqlString);
             									preparedStatement.setString(1, movieDetails);
             									preparedStatement.setString(2,"http://" +"188.131.249.47" +"/datasource/" + fileBean.getName());
             									preparedStatement.setString(3,"http://" +"188.131.249.47" +"/torrent/" + fileName + ".torrent");
             									preparedStatement.setString(4, movieName);
             									preparedStatement.executeUpdate();
             									preparedStatement.close();
             								}
             							}
             							
             						}catch (SQLException e) {
             							// TODO: handle exception
             							e.printStackTrace();
             						}
             									  }
             				}).start();
                             
                            // logger.info("合并文件{}成功", fileName);
                         
     					}
     				}
				}
            }
         }
    }
    
    public void Merge() throws IOException{
    	 Path realFile = Paths.get(finalDirPath, fileName);
    	 realFile = Files.createFile(realFile);
         
         // 设置权限
         FileUtils.authorizationAll(realFile);
         for (int i = 0; i < chunks; i++) {
             // 获取每个分片
             String tempFileName = fileName + "_" + i + "_tmp";
             Path itemPath = Paths.get(tempPath, tempFileName);
             byte[] bytes = Files.readAllBytes(itemPath);
             Files.write(realFile, bytes, StandardOpenOption.APPEND);
            
         }
         //删除MD5文件夹
         new File(tempPath).delete();
    	 
    }
    
    public String executeLinuxCmd(String cmd) {
       
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmd);
            InputStream in = process.getInputStream();
          
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[8192];
            for (int n; (n = in.read(b)) != -1;) {
            	out.append(new String(b, 0, n));
            }
            in.close();
            // process.waitFor();
            process.destroy();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.doPost(req, resp);
    }
}
