package com.atguigu.gmall.manage;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

    //@Autowired
    //RedisUtil redisUtil;
    //
    //@Test
    //public void contextLoads() {
    //    Jedis jedis = redisUtil.getJedis();
    //    System.out.println(jedis);
    //}


    @Reference
    SkuService skuService;// 查询mysql


    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

        // 查询mysql数据

        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();

        // 转化为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);

            //将查询出来的转化为long类型，要存储在elasticsearch进行排序，
            // 因为从mysql中查询出来的是string类型的，不能尽心排序
            String skuId = pmsSkuInfo.getId();
            pmsSearchSkuInfo.setId(Long.parseLong(skuId));
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);

        }

        // 导入es   导入时需要将集合中循环单条导入
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            //pmsSearchSkuInfo会转化为json格式的
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
            jestClient.execute(put);
        }

    }

    public void put() throws IOException {

        // 查询mysql数据

        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();

        // 转化为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);

            //将查询出来的转化为long类型，要存储在elasticsearch进行排序，
            // 因为从mysql中查询出来的是string类型的，不能尽心排序
            String skuId = pmsSkuInfo.getId();
            pmsSearchSkuInfo.setId(Long.parseLong(skuId));
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);

        }

        // 导入es   导入时需要将集合中循环单条导入
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            //pmsSearchSkuInfo会转化为json格式的
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
            jestClient.execute(put);
        }

    }



}
