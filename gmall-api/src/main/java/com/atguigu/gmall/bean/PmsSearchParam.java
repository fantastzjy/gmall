package com.atguigu.gmall.bean;

import java.io.Serializable;
import java.util.List;


public class PmsSearchParam implements Serializable {

    private String catalog3Id;

    private String keyword;

    //才开始用的是 private List<PmsSkuAttrValue> skuAttrValueList;
    //这样传进来的属性值是传不进来的，因为传进来的是一个字符串，这里不对应所以封装不上
    //解决方案有两个，这里改成字符串数组接收，或者将创近来的参数加一个集合
    private String[] valueId;

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }
}
