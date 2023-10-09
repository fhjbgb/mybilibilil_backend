package com.mybilibili.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.mybilibili.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Component
public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    //用于端点续传
    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

   @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String PATH_KEY = "path-key:";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";

    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";

    private static final String DEFAULT_GROUP = "group1";

    private static final int SLICE_SIZE = 1024 * 1024 * 2;

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    //获取文件类型，根据二进制流获取
    public String getFileType(MultipartFile file){
        if(file == null){
            throw new ConditionException("非法文件！");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }

    //上传
    public String uploadCommonFile(MultipartFile file) throws Exception {
        //metaData :元数据
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        //StorePath：文件上传完成后所有的路径信息    填充好数据后上传文件
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();
    }

    public String uploadCommonFile(File file, String fileType) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        StorePath storePath = fastFileStorageClient.uploadFile(new FileInputStream(file),
                                    file.length(), fileType, metaDataSet);
        return storePath.getPath();
    }

    //上传可以断点续传的文件  解决大文件上传存在的问题。将大文件分片，按顺序依次传输。
    public String uploadAppenderFile(MultipartFile file) throws Exception{
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //offset:偏移量：说明在哪个位置添加对应分片内容
    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception{
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    //通过分片上传文件
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if(file == null || sliceNo == null || totalSliceNo == null){
            throw new ConditionException("参数异常！");
        }
        //上传路径
        String pathKey = PATH_KEY + fileMd5;
        //当前上传文件的总大小
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        //记录上传了多少分片了，主要用于与总分片数比对
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        //看一下是否为空
        if(!StringUtil.isNullOrEmpty(uploadedSizeStr)){
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }
        //判断分片序号
        if(sliceNo == 1){ //上传的是第一个分片
            //直接上传  获得第一个分片
            String path = this.uploadAppenderFile(file);
            //进一步判断
            if(StringUtil.isNullOrEmpty(path)){
                throw new ConditionException("上传失败！");
            }
            //上传成功后把信息存在redis中
            //第一片信息上传，之后的上传基于第一次来上传
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        }else{
            //获取路径
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if(StringUtil.isNullOrEmpty(filePath)){
                throw new ConditionException("上传失败！");
            }
            //文件、文件路径、偏移量
            this.modifyAppenderFile(file, filePath, uploadedSize);
            //更新redis的数据
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        // 修改历史上传分片文件大小
        uploadedSize  += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        //如果所有分片全部上传完毕，则清空redis里面相关的key和value
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if(uploadedNo.equals(totalSliceNo)){
            //只有上传完成才会返回正确的地址，否则返回空值
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    //文件转换成分片
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception{
        String fileName = multipartFile.getOriginalFilename();
        String fileType = this.getFileType(multipartFile);
        //生成临时文件，将MultipartFile转为File
        File file = this.multipartFileToFile(multipartFile);
        //文件长度
        long fileLength = file.length();
        //计数器，方便文件分片生成
        int count = 1;
        for(int i = 0; i < fileLength; i += SLICE_SIZE){
            //RandomAccessFile支持随机访问，适合端点续传的功能,读权限
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            //搜寻位置：起始位置
            randomAccessFile.seek(i);
            //读取数据
            byte[] bytes = new byte[SLICE_SIZE];
            //获取数组大小的长度，预防最后一片长度不够的情况
            int len = randomAccessFile.read(bytes);
            //路径
            String path = "D:\\myBilibiliResource\\tmpfile\\" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            //信息流关闭
            randomAccessFile.close();
            count++;
        }
        //删除临时文件
        file.delete();
    }

    //返回文件，底层是文件流的读写
    public File multipartFileToFile(MultipartFile multipartFile) throws Exception{
        //文件名
        String originalFileName = multipartFile.getOriginalFilename();
        //获取原始的文件名称，用.区分，只取名称不取类型
        String[] fileName = originalFileName.split("\\.");
        //生成临时文件
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
//        传入java的文件
        multipartFile.transferTo(file);
        return file;
    }


//    删除
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }


        // 对请求进行包装，防止使用者直接获取某些信息
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String path) throws Exception{
        //获取文件信息，传入组名和路径就能获取
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        //文件总大小
        long totalFileSize = fileInfo.getFileSize();
        //url拼接，固定开头和文件的位置
        String url = httpFdfsStorageAddr + path;
        //枚举类型,获取请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        //判断枚举类中是否还有数据
        while(headerNames.hasMoreElements()){
            //获取下一个header
            String header = headerNames.nextElement();
            //向map中添加header和header值
            headers.put(header, request.getHeader(header));
        }
        //获取range的header对象，range是请求头中标记视频分段的字段
        String rangeStr = request.getHeader("Range");
        String[] range;
        //判断是否为空
        if(StringUtil.isNullOrEmpty(rangeStr)){
            //为空  进行初始赋值
            rangeStr = "bytes=0-" + (totalFileSize-1);
        }
        //对内容进行拆除分离 ：bytes=xxxxxxx-xxxxxxx  起始位置-终止位置
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if(range.length >= 2){
            //如果有起始位置就给起始位置赋值
            begin = Long.parseLong(range[1]);
        }
        //终止位置赋初值
        long end = totalFileSize-1;
        if(range.length >= 3){
            //存在结束位置，赋值
            end = Long.parseLong(range[2]);
        }
        //长度计算
        long len = (end - begin) + 1;
        //依次设置响应头中的属性、响应参数设置
        //contentrange存储range的总数据
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int)len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url, headers, response);
    }

    public void downLoadFile(String url, String localPath) {
        fastFileStorageClient.downloadFile(DEFAULT_GROUP, url,
                new DownloadCallback<String>() {
                    @Override
                    public String recv(InputStream ins) throws IOException {
                        File file = new File(localPath);
                        OutputStream os = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = ins.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        ins.close();
                        return "success";
                    }
                });
    }
}
