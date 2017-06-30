package cn.zhou.path.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @ClassName WebUtils
 * @Description 与网络相关的工具列
 * @author wzf
 * @Date 2017年2月20日 下午4:44:11
 * @version 1.0.0
 */
public class WebUtils {
	public static void send(HttpServletResponse response, String content) {
		try {
			PrintWriter out = response.getWriter();
			out.write(content);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 如果是ajax请求，则需要前端配合，判断自定义响应头是否包含X-Redirect-Page
	 * */
	public static void sendRedirect(HttpServletRequest request,HttpServletResponse resp,String url) throws IOException{
	    if(isAjax(request)){
	        resp.addHeader("X-Redirect-Url", url);
	    }else{
	        resp.sendRedirect(url);
	    }
	}
	
	
	
	public static boolean isAjax(HttpServletRequest request){
		return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("x-requested-with"));
	}
	
	
	
	public static  String getRemoteAddress(HttpServletRequest request){
		String ip = request.getHeader("x-forwarded-for");  
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
	}
}
