package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {// 三级分类id、关键字、

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
        if (delValueIds != null) {
            for (String delValueId : delValueIds) {

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                String urlParam = getUrlParamForCrumb(pmsSearchParam, delValueId);
                pmsSearchCrumb.setUrlParam(urlParam);

                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
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
         *
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


    //这里可以和上面的方法合并写成可变形参
    //private String getUrlParam(PmsSearchParam pmsSearchParam,String...delValueId) {
    private String getUrlParam(PmsSearchParam pmsSearchParam) {

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

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}