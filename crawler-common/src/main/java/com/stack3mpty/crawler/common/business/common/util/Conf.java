package com.stack3mpty.crawler.common.business.common.util;

import com.stack3mpty.crawler.common.business.business.builder.TaskBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class Conf {

    public static List<String> getAllBuilderClassNames() {
        List<String> classNames = new ArrayList<>();
        try {
            String packageName = "com.stack3mpty.examples.builder";
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(TaskBuilder.class));

            Set<BeanDefinition> candidates = scanner.findCandidateComponents(packageName);
            for (BeanDefinition candidate : candidates) {
                String className = candidate.getBeanClassName();
                if (className != null) {
                    classNames.add(className);
                }
            }
        } catch (Exception e) {
            log.error("Error scanning for TaskBuilder classes", e);
        }
        return classNames;
    }

    public static Map<String, List<String>> getClassMapByJar(String packageSuffix) {
        Map<String, List<String>> classMap = new HashMap<>();
        List<String> classes = new ArrayList<>();
        classMap.put("example", classes);
        String packageName = "com.stack3mpty.examples." + packageSuffix;
        String packagePath = packageName.replace('.', '/');
        URL url = Conf.class.getResource("/" + packagePath);
        if (url == null) {
            return Map.of();
        }
        if (url.getProtocol().equals("jar")) {
            String path;
            try (JarFile jar = new JarFile(new File(url.toURI()).getPath())) {
                Enumeration<JarEntry> e = jar.entries();
                while (e.hasMoreElements()) {
                    path = e.nextElement().getName();
                    if (path.startsWith(packagePath)
                            && path.endsWith(".class")) {
                        classes.add(path.substring(0, path.length() - 6)
                                .replace('/', '.'));
                    }
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                String root = new File(url.toURI()).getPath();
                root = root.substring(0,
                        root.length() - packagePath.length());
                search(classes, root, packagePath);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return classMap;
    }

    private static void search(
            List<String> classes, String root,
            String path) {
        File folder = new File(root + path);
        String[] list = folder.list();
        if (list == null) {
            return;
        }
        for (String file : folder.list()) {
            if (new File(root + path + "/" + file).isDirectory()) {
                search(classes, root, path + "/" + file);
            } else if (file.endsWith(".class")) {
                classes.add(path.replace('/', '.') + "." + file.substring(0, file.length() - 6));
            }
        }
    }
}
