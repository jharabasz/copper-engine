/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.wfrepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class WebAppClassloaderClasspathProvider implements CompilerOptionsProvider,ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(WebAppClassloaderClasspathProvider.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    @Override
    public Collection<String> getOptions() {

        List<File> webInfJarFilesJarFiles =  webInfJars();


        List<File> jarFiles = classLoaderJars();

        Set<File> allFiles = new HashSet<File>();

        allFiles.addAll(webInfJarFilesJarFiles);
        allFiles.addAll(jarFiles);

        StringBuilder buf = new StringBuilder();
        for(File f : allFiles){
            StringBuilder buf2 = new StringBuilder();
            buf2.append(f.getAbsolutePath()).append(File.pathSeparator);
            buf.insert(0, buf2.toString());
        }


        List<String> options = new ArrayList<String>();
        options.add("-classpath");
        options.add(buf.toString());

        logger.error("options: "+options);
        return options;
    }

    private  List<File> webInfJars() {
        if(applicationContext instanceof WebApplicationContext){
            final WebApplicationContext webappContext = (WebApplicationContext) applicationContext;
            String realDir = webappContext.getServletContext().getRealPath("WEB-INF/lib");
            logger.info("resolving lib directory to: "+realDir);

            File dir = new File(realDir);

            File[] webInfJars = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });

            return Arrays.asList(webInfJars);
        }
        return new ArrayList<File>(0);
    }

    private List<File> classLoaderJars() {
        List<File> jarFiles = new ArrayList<File>();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {

                for (URL url : ((URLClassLoader)loader).getURLs() ) {
                    try {
                        //convert the URL to a URI to remove the HTML encoding, if it exists.
                        File f = new File(url.toURI().getPath());
                        jarFiles.add(f);
                    }
                    catch (URISyntaxException e) {
                        throw new RuntimeException("failed to convert the classpath URL '"+url+"' to a URI",e);
                    }

                }

            }else {
                logger.error("Classloader instance of: "+loader.getClass());
            }
            loader = loader.getParent();
        }
        return jarFiles;
    }

}
