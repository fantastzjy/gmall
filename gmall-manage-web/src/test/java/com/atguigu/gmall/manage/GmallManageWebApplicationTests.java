package com.atguigu.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.*;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {


    @Test
    public void contextLoads() throws IOException, MyException {

        // 配置fdfs的全局链接地址
        String tracker = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();// 获得配置文件的路径

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();

        // 获得一个trackerServer的实例
        TrackerServer trackerServer = trackerClient.getConnection();

        // 通过tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String[] uploadInfos = storageClient.upload_file("d:/Desktop/分类列表模板 .xls", "xls", null);
        //http://47.99.111.209//group1/M00/00/00/rBktxF-2kgCALu3sAACwyLmgHuw274_big.jpg
        //http://47.99.111.209/group1/M00/00/00/rBktxF-3MeWAc3bEBTdNhiVjisA115.mp4
        //http://47.99.111.209/group1/M00/00/00/rBktxF-3NV6AK_FKUkZVLuBX1Xs30.rmvb
        String url = "http://47.99.111.209";

        for (String uploadInfo : uploadInfos) {
            url += "/" + uploadInfo;

            //url = url + uploadInfo;
        }

        System.out.println(url);

    }

}
