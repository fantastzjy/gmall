package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        /**
         *  @RequestParam("file") MultipartFile multipartFile
         *  http的form表单提交的file格式和springMVC自定义的MultipartFile格式的不一样，
         *  中间还有一个类型转化的过程，必须指定RequestParam去接收
         */


        // 将图片或者音视频上传到分布式的文件存储系统

        // 将图片的存储路径返回给页面
        String imgUrl = "https://m.360buyimg.com/babel/jfs/t5137/20/1794970752/352145/d56e4e94/591417dcN4fe5ef33.jpg";

        return imgUrl;
    }


    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> getSpuList( String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.getSpuList(catalog3Id);

        return pmsProductInfos;
    }


}
