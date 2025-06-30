package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class IFileUtils {
    public static String readFileFromResources(String path) throws IOException {
        ClassLoader classLoader = IFileUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
