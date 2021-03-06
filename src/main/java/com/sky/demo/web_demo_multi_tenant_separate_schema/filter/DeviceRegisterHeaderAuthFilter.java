package com.sky.demo.web_demo_multi_tenant_separate_schema.filter;

import com.sky.demo.web_demo_multi_tenant_separate_schema.auth.token.RegisterHeaderAuthToken;
import com.sky.demo.web_demo_multi_tenant_separate_schema.util.CodecUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Created by user on 16/9/27.
 */
public class DeviceRegisterHeaderAuthFilter extends HeaderAuthFilter {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegisterHeaderAuthFilter.class);

    @Override
    protected AuthenticationToken createToken(String[] authorizationList) {
        if (authorizationList == null) {
            throw new AuthenticationException("authentication header is null");
        }
        return new RegisterHeaderAuthToken(authorizationList[0], authorizationList[1]);
    }

    @Override
    protected String[] getPrincipalsAndCredentials(String authorizationHeader, ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.getHttpRequest(request);
        if (StringUtils.isBlank(authorizationHeader)) {
            logger.error("authorization list is null ,request url is {}", httpRequest.getRequestURI());
            return null;
        }

        try {
            String authorization = CodecUtil.decode(authorizationHeader);
            String[] authorizationList = authorization.split(":");
            if (authorizationList.length != 2) {
                logger.error("authorization list length != 2,current header is {},request url is {}",
                        authorizationHeader, httpRequest.getRequestURI());
                return null;
            }
            return authorizationList;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
