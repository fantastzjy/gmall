package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

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

    public void get() throws IOException {
        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "51");
        boolQueryBuilder.filter(termQueryBuilder);
        // must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "黑鲨");
        boolQueryBuilder.must(matchQueryBuilder);

        // query
        searchSourceBuilder.query(boolQueryBuilder);
        // from
        searchSourceBuilder.from(0);
        // size
        searchSourceBuilder.size(20);
        // highlight
        searchSourceBuilder.highlight();

        String dslStr = searchSourceBuilder.toString();
        System.err.println(dslStr);

        // 用api执行复杂查询
        Search search = new Search.Builder(dslStr).addIndex("gmall").addType("PmsSkuInfo").build();

        SearchResult execute = jestClient.execute(search);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<PmsSearchSkuInfo>();

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);

        }
        System.out.println(pmsSearchSkuInfos.size());
    }

}
