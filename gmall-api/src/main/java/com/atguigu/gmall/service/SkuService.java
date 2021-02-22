package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {


    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    PmsSkuInfo getSkuInfoById(String skuId, String remoteAddr);

    List<PmsSkuInfo> getAllSku();

    boolean checkPrice(String productSkuId, BigDecimal price);
}
