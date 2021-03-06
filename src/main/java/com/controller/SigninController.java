package com.controller;

import com.pojo.Group;
import com.pojo.Signin;
import com.pojo.User;
import com.service.GroupService;
import com.service.SigninService;
import com.service.UserService;
import com.util.GetIPUtils;
import com.util.JudgeIPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhang
 */
@Controller
@RequestMapping("")
public class SigninController {
    @Autowired
    SigninService signinService;
    @Autowired
    UserService userService;
    @Autowired
    GroupService groupService;
    //*
    // 展示全部签到情况
    // */

    @RequestMapping("listAllSignin")
    public ModelAndView listAllSignin(){
        ModelAndView mav = new ModelAndView();
        List<Signin> signins = signinService.list();
        List<User> users = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        for (int i = 0;i<signins.size();i++){
            users.add(userService.get(signins.get(i).getSigninUserId()));
            groups.add(groupService.get(users.get(i).getGroupId()));
        }
        mav.addObject("signins",signins);
        mav.addObject("users",users);
        mav.addObject("groups",groups);
        mav.setViewName("signindata");
        return mav;
    }


    @RequestMapping("listSigninByGroup")
    public ModelAndView listSigninByGroup(HttpSession session){
        ModelAndView mav = new ModelAndView();
        int groupID = ((User)session.getAttribute("user")).getGroupId();
        List<User> users = userService.list(groupID);
        Group group = groupService.get(groupID);
        mav.addObject("users",users);
        mav.addObject("group",group.getGroupName());
        mav.setViewName("groupsignin");
        return mav;
    }

    //*
    // 签到
    // */

    @RequestMapping("signin")
    public ModelAndView signin(HttpSession session, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        User user = (User)session.getAttribute("user");
        int isSigninToday = user.getIsSigninToday();
        int isInCSTI = JudgeIPUtils.isincsti(request);
        if (isSigninToday == 0 && isInCSTI == 1){
            Signin signin = new Signin();
            signin.setSigninIp(GetIPUtils.getIpAddr(request));
            signin.setSigninTime(new Timestamp(System.currentTimeMillis()));
            signin.setSigninUserId(user.getId());
            signin.setSigninUa(request.getHeader("user-agent"));
            signinService.add(signin);
            user.setIsSigninToday(1);
            session.setAttribute("user",user);
            user.setGmtModified(null);
            userService.update(user);
        }
        mav.setViewName("redirect:index");
        return mav;
    }
}
