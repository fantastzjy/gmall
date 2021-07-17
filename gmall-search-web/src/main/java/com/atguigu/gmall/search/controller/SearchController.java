package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        // 调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        // 抽取检索结果锁包含的平台属性集合
        HashSet<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        // 根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);
        //在modelMap.put("attrList", pmsBaseAttrInfos);后对pmsBaseAttrInfos在进行修改原来的attrList的值也会改变？？？？


        //合并后的制作面包屑和删除属性

        // 对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        //将所在组的所有属性值全去除掉
        //制作面包屑
        ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        String[] delValueIds = pmsSearchParam.getValueId();
        //只有在判断平台属性列表中有值的情况下才会生成面包屑和平台属性列表
        if (delValueIds != null) {
            //将这个循环放在外面是因为正好这个循环的次数就是面包屑的个数
            for (String delValueId : delValueIds) {
                //注意这个细节不能在循环外面
                // 因为每次循环都要将pmsBaseAttrInfos集合重新扫描一遍
                // 如果放在循环外面扫描一遍之后迭代器里面就没有东西了
                //迭代器原理 ：即使每次迭代向后移动   到最后不会再回到前面，所以将迭代器放在循环里面
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                String urlParam = getUrlParamForCrumb(pmsSearchParam, delValueId);
                pmsSearchCrumb.setUrlParam(urlParam);

                //循环删除集合（本质数组）中的东西一定不能用remove删除要用iterator迭代器进行删除
                //因为 用remove删除后集合（数组）的索引值会重新改变，最后会出现下标越界
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String attrValueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(attrValueId)) {

                            // 查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }


        /**
         *
         *分开的删除平台属性和面包屑功能
         *
         // 对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
         //将所在组的所有属性值全去除掉
         String[] delValueIds = pmsSearchParam.getValueId();
         if (delValueIds != null) {
         Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
         while (iterator.hasNext()) {
         PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
         List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
         for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
         String attrValueId = pmsBaseAttrValue.getId();
         for (String delValueId : delValueIds) {
         if (delValueId.equals(attrValueId)) {
         //删除该属性值所在的属性组
         iterator.remove();
         }
         }

         }
         }
         }

         ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
         //制作面包屑

         if (delValueIds != null) {
         for (String delValueId : delValueIds) {

         PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
         pmsSearchCrumb.setValueId(delValueId);

         pmsSearchCrumb.setValueName(delValueId);

         String urlParam = getUrlParamForCrumb(pmsSearchParam,delValueId);
         pmsSearchCrumb.setUrlParam(urlParam);
         pmsSearchCrumbs.add(pmsSearchCrumb);
         }

         }

         modelMap.put("attrValueSelectedList", pmsSearchCrumbs);

         *
         *
         */


        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);

        //将搜索用的关键词回显到搜索框
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }


        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {

        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        //数组判断是否为空用  !=null
        if (skuAttrValueList != null) {
            //不用进行下面的判断，因为前面一定有keyword或者catalog3Id，只有从哪两个才能进入list界面
            //if(StringUtils.isNotBlank(urlParam)){
            //    urlParam += "&";
            //}
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)) {
                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }
            }
        }

        return urlParam;
    }


    //这里可以和上面的方法getUrlParamForCrumb合并写成可变形参
    //private String getUrlParam(PmsSearchParam pmsSearchParam,String...delValueId) {
    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        //catalog3Id 和 Keyword 是必有一项的  因为只能通过这两个入口才能进来
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        //数组判断是否为空用  !=null
        if (skuAttrValueList != null) {
            //不用进行下面的判断，因为前面一定有keyword或者catalog3Id，只有从哪两个才能进入list界面
            //if(StringUtils.isNotBlank(urlParam)){
            //    urlParam += "&";
            //}
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }

    //进入首页
    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index() {
        return "index";
    }


}