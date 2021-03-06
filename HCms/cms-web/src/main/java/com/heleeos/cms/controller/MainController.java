package com.heleeos.cms.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.heleeos.cms.bean.Manager;
import com.heleeos.cms.bean.Result;
import com.heleeos.cms.constant.SessionKey;
import com.heleeos.cms.service.ManagerService;
import com.heleeos.cms.util.CookieUtil;

@RestController
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @Autowired
    public ManagerService managerService;
    
    @Value("#{configProperties.image_host}")
    private String imageHost;
    
    /**
     * 管理端首页. 
     */
    @RequestMapping(value = {"/", "index.html"})
    public ModelAndView toIndex(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("main/index");
        modelAndView.addObject("admin", request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY));
        modelAndView.addObject("imageHost", imageHost);
        return modelAndView;
    }
    
    /**
     * 管理端开发日志界面.
     */
    @RequestMapping(value = "logs.html")
    public ModelAndView toLogs(){
        ModelAndView modelAndView = new ModelAndView("main/logs");
        return modelAndView;
    }
    
    /**
     * 管理端登陆界面.
     */
    @RequestMapping(value = "login.html")
    public ModelAndView toLogin(HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("main/login");
        if(request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY) != null)
            return toIndex(request);
        return modelAndView;
    }
    
    /**
     * 管理员个人设置页面. 
     */
    @RequestMapping(value = "profile.html")
    public ModelAndView toProfile(HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("main/profile");
        modelAndView.addObject("admin", request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY));
        return modelAndView;
    }
    
    /**
     * 检测是否登陆理端.
     */
    @RequestMapping(value = "islogin.json")
    public Result islogin(HttpServletRequest request){
        Result result = new Result();
        Object obj = request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY);
        if(obj != null && obj instanceof Manager){
            result.setCode(200);
            result.putInfo(System.currentTimeMillis());
        }else{
            result.setCode(400);
            result.putInfo("未登录");
        }
        return result;
    }
    
    /**
     * 登陆管理端.
     * 
     * 参数:
     * username 用户名
     * password 密码
     */
    @RequestMapping(value = "login.json")
    public Result login(HttpServletRequest request, HttpServletResponse response){
        Result result = new Result();
        try {
            String cptcha = request.getSession().getAttribute(SessionKey.SESSION_CPTCHA_KEY).toString();
            if(cptcha != null && cptcha.equals(request.getParameter("cptcha"))) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                Manager manager = managerService.get(username, DigestUtils.md5DigestAsHex(password.getBytes()));
                if(manager != null) {
                    managerService.updateLoginTime(manager.getId());
                    request.getSession().setAttribute(SessionKey.SESSION_MANAGER_KEY, manager);
                    CookieUtil.saveManager(response, manager);                    
                    result.setCode(200);
                    result.putInfo("");
                } else{
                    result.setCode(400);
                    result.putInfo("用户名或密码错误!");
                }
            } else {
                result.setCode(400);
                result.putInfo("验证码错误!"); 
            }
        } catch (Exception e) {
            result.setCode(403);
            result.putInfo("请勿非法调用!");
            logger.error("调用登陆接口出现异常!", e);
        }
        return result;
    }
    
    /**
     * 退出管理端.
     */
    @ResponseBody
    @RequestMapping(value = "logout.json")
    public Result logout(HttpServletRequest request, HttpServletResponse response){
        request.getSession().removeAttribute(SessionKey.SESSION_MANAGER_KEY);
        CookieUtil.clearManager(request, response);
        Result result = new Result();
        result.setCode(200);
        result.putInfo("");
        return result;
    }
    
    /**
     * 修改管理员信息. 
     * 
     * 参数:
     * username 用户名
     * realname 姓名
     * password 密码
     * image    头像
     */
    @RequestMapping(value = "update_manager.json")
    public Result update(HttpServletRequest request){
        Result result = new Result();
        
        /* 获取当前登陆的管理员 */
        Manager manager = (Manager) request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY);
        if(manager == null){
            result.setCode(-1);
            result.putInfo("登陆失效,请刷新页面!");
            return result;
        }
        
        String username = request.getParameter("username");
        String realname = request.getParameter("realname");
        String password = request.getParameter("password");
        String picture = request.getParameter("picture");
        
        if(StringUtils.trimToNull(username) == null){
            result.setCode(-1);
            result.putInfo("用户名不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(realname) == null){
            result.setCode(-1);
            result.putInfo("真实姓名不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(password) != null){
            manager.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }
        
        manager.setUsername(username);
        manager.setRealname(realname);
        manager.setPicture(picture);
        
        boolean bol = managerService.save(manager);
        if(bol){
            result.setCode(200);
            result.putInfo("");
        }else{
            result.setCode(400);
            result.putInfo("提交失败");
        }
        return result;
    }
}
