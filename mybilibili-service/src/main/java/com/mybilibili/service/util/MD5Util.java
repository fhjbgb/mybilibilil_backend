package com.mybilibili.service.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * MD5加密
 * 单向加密算法
 * 特点：加密速度快，不需要秘钥，但是安全性不高，需要搭配随机盐值使用
 *
 */
public class MD5Util {

	public static String sign(String content, String salt, String charset) {
		content = content + salt;
		return DigestUtils.md5Hex(getContentBytes(content, charset));
	}

	public static boolean verify(String content, String sign, String salt, String charset) {
		content = content + salt;
		String mysign = DigestUtils.md5Hex(getContentBytes(content, charset));
		return mysign.equals(sign);
	}

	private static byte[] getContentBytes(String content, String charset) {
		if (!"".equals(charset)) {
			try {
				return content.getBytes(charset);
			} catch (UnsupportedEncodingException var3) {
				throw new RuntimeException("MD5签名过程中出现错误,指定的编码集错误");
			}
		} else {
			return content.getBytes();
		}
	}

	//获取文件md5加密后的字符串
    public static String getFileMD5(MultipartFile file) throws Exception {
		InputStream fis = file.getInputStream();
		//ByteArrayOutputStream 比特数组相关的输出流 方便取出数据进行加密
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//接收用字节数组
		byte[] buffer = new byte[1024];
		int byteRead;
		//byteRead的值为数组大小
		//每次从输入流中读取1024大小的内容，byteRead记录本次读取的大小，最后的内容容量可能不足1024.
		//如果为0，则说明读取完毕
		while((byteRead = fis.read(buffer)) > 0){
			//将本次读取的内容写入输出流
			baos.write(buffer, 0, byteRead);
		}
		//关闭
		fis.close();
		//加密后再返回
		return DigestUtils.md5Hex(baos.toByteArray());
    }
}