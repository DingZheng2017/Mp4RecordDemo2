package com.feifanuniv.librecord.utils;

import java.io.File;

/**
 * File工具类
 * Created by dingzheng on 2017/12/20.
 */
public class FileUtils {

    private static final String SUFFIX = ".mp4";

    /**
     * 生成文件完整路径+名称，如storage/emulated/0/001/a.mp4
     * @param path
     * @param fileName
     * @return
     */
    public static String generateFileFullPath(String path,String fileName,boolean addExtensition) {
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(path);
        fullPath.append("/");
        if (addExtensition){
            fileName = fileName +"_1";
        }
        fullPath.append(fileName);
        fullPath.append(SUFFIX);

        String pathString = fullPath.toString();
        File file = new File(pathString);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {//目录是否存在
            parentFile.mkdirs();
        }
        return pathString;
    }

    public static boolean isExit(String path) {
        File file = new File(path);
        if (file.exists()){
            return true;
        }
        return false;
    }

}
