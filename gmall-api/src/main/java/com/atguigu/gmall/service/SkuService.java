package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

public interface SkuService {


    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfo(String skuId);
}
