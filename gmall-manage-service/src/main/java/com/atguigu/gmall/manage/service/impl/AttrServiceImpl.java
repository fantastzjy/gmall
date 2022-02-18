package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;


    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        //foreach循环中对pmsBaseAttrInfo的操作就是对pmsBaseAttrInfos中每一个进行操作，保存值进去也是
        for (PmsBaseAttrInfo pmsBaseAttrInfo1 : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo1.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            pmsBaseAttrInfo1.setAttrValueList(pmsBaseAttrValues);
        }


        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttroInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();

        if (StringUtils.isBlank(id)) {
            // id为空，保存
            // 保存属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            // 保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {

                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);

            }
        } else {
            // id不空，修改
            // 属性修改  用Example 最好
            Example example = new Example(pmsBaseAttrInfo.getClass());
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());

            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);
            // 属性值修改
            // 按照属性id删除所有属性值   这里虽然设置的是一个PmsBaseAttrValue的id 但是会把所有的id相同的删掉
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);

            // 删除后，将新的属性值插入
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();

            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                //进入到修改页面时会查询出所有属性值的id到页面上，要修改的船传进来时会有id 但是新增加的就没有id
                //这里对新增加的在进行id的设置，原先就有的不用设置，删除的上一步已经删除过了
                if (StringUtils.isBlank(pmsBaseAttrValue.getAttrId())) {
                    pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                }
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }

        }
        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        //查询和删除都是创建一个bean对象，然后设置一些属性再执行mapper
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();

        pmsBaseAttrValue.setAttrId(attrId);

        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);


        return pmsBaseAttrValues;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(HashSet<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);

        return pmsBaseAttrInfos;
    }


}
