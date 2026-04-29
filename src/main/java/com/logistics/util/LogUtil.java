
package com.logistics.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fetch:
 * 
 * @author NiUYD
 * create time: 2022/11/4 13:52
 */
public class LogUtil {
    public static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    private LogUtil() {
        throw new IllegalStateException("this is a util class");
    }
}
