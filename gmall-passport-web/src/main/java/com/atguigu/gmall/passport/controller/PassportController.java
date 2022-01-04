package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    //因为已经继承过了web-Util 里面有该类 这里不用注入
    //@Autowired
    //JwtUtil jwtUtil;

    @RequestMapping("verify")
    @ResponseBody
    // 不用这个request，因为他是拦截器发起的请求，是拦截器的ip地址，所以要想获取原始的ip地址，就要把原始请求的IP地址当做参数传进来
    //public String verify(String token,HttpServletRequest request) {
    public String verify(String token, String currentIp) {
        //通过jwt验证token真假
        Map<String, String> map = new HashMap<>();

        //下面通过HttpServletRequest获取的IP地址不正确
        //String ip = request.getHeader("x-forwarded-for");
        //if (StringUtils.isBlank(ip)) {
        //    ip = request.getRemoteAddr();// 从request中获取ip
        //    if (StringUtils.isBlank(ip)) {
        //        ip = "127.0.0.1";
        //    }
        //}

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall0105", currentIp);
        if (decode != null) {
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
            map.put("status", "success");
        } else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }


    @RequestMapping("vlogin")
    @ResponseBody
    //public String vlogin(String code, HttpServletRequest request) {
    public ModelAndView vlogin(String code, HttpServletRequest request) {

        // 授权码换取access_token
        // 换取access_token
        // client_secret=f043fe09dcab7e9b90cdd7491e282a8f
        // client_id=2173054083

        String s3 = "https://api.weibo.com/oauth2/access_token?";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "3584121244");
        paramMap.put("client_secret", "6e845ff6ef7bd72caaf8f1256ab91ec7");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com/vlogin");
        paramMap.put("code", code);// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String, Object> access_map = JSON.parseObject(access_token_json, Map.class);

        // access_token换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");
        String show_user_url = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String, Object> user_map = JSON.parseObject(user_json, Map.class);

        // 将用户信息保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String) user_map.get("screen_name"));
        String g = "0";
        String gender = (String) user_map.get("gender");
        if (gender.equals("m")) {
            g = "1";
        }
        umsMember.setGender(g);

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        // 检查该用户(社交用户)以前是否登陆过系统
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);

        if (umsMemberCheck == null) {
            umsMember = userService.addOauthUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
        }

        // 生成jwt的token，并且重定向到首页，携带该token
        String token = null;
        String memberId = umsMember.getId();// rpc的主键返回策略失效
        String nickname = umsMember.getNickname();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", memberId);// 是保存数据库后主键返回策略生成的id
        userMap.put("nickname", nickname);


        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();// 从request中获取ip
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }
        }

        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        // 将token存入redis一份
        userService.addUserToken(token, memberId);

        return  new ModelAndView(new RedirectView("http://search.gmall.com/index?token=" + token));
        //return "redirect:http://search.gmall.com:8077/index?token=" + token;
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {

        String token = "";

        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        //登陆成功
        if (umsMemberLogin != null) {

            // 用jwt制作token
            //私有部分
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);
            //盐值
            // 通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();// 从request中获取ip
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            // 按照设计的算法对参数进行加密后，生成token
            token = JwtUtil.encode("2019gmall0105", userMap, ip);

            // 将token存入redis一份
            userService.addUserToken(token, memberId);


        } else {
            // 登录失败
            token = "fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map) {

        map.put("ReturnUrl", ReturnUrl);
        //这里打开了登录界面
        return "index";
    }
}
