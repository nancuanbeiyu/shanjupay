package com.shanjupay.merchant.config;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class QiniuUtils {

    private static String accessKey ="Ght14dk331tcL2AKjl2O6YoxJkdV5BBMyMW5_UEu";
    private static String secretKey ="LrtFVeISkV2RR6UYJfLZzqCHvjy3hj6YI6FX1Q1A";
    private static String bucket ="yayitietie" ;
     private static String fileAddress= "D:\\workspace\\pay-photograph\\";
    //上传测试
    public static void testUpload(String fileName){
//构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
    //默认不指定key的情况下，以文件内容的hash值作为文件名，这里建议由自己来控制文件名
        String key = UUID.randomUUID()+".png";
        FileInputStream fileInputStream = null;
        try {
    //通常这里得到文件的字节数组
            fileInputStream = new FileInputStream(new File(fileAddress+fileName));
            byte[] uploadBytes = IOUtils.toByteArray(fileInputStream);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(uploadBytes, key, upToken);
            //解析上传成功的结果,
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    System.err.println(ex2);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
