package com.heleeos.cms.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.heleeos.cms.constant.SessionKey;
import com.heleeos.cms.util.CookieUtil;
/**
 * 登陆过滤器.
 * 
 * @author liyu
 */
public class AuthorInterceptor extends HandlerInterceptorAdapter {
    
    /* 检测是否登陆,没有登录的跳转到登陆页面 */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        Object obj = req.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY);
        if(obj == null) {
            obj = CookieUtil.getManager(req);
        }
        
        if (obj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return false;
        }
        return true;
    }
}
