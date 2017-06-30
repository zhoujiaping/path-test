package cn.zhou.path.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import cn.zhou.path.util.WebUtils;

public class ExceptionResolver implements HandlerExceptionResolver {
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object obj, Exception ex) {
		ex.printStackTrace();
		Map<String, Object> model = new HashMap<String, Object>();  
        model.put("ex", ex);  
		if (! WebUtils.isAjax(request)) {
	        return new ModelAndView("error", model);  
		}
		PrintWriter out = null;
		try {
			out = response.getWriter();
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/plain;charset=utf-8");
			response.setStatus(500);
			String msg = ex.getMessage();
			if(msg.length()>100){
			    out.print("服务器内部错误");
			}else{
			    out.print(msg);
			}
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			out.close();
		}
		return null;
		
	}
}