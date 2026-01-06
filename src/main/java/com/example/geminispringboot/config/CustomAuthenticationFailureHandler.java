package com.example.geminispringboot.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.geminispringboot.dao.service.UserDaoService;
import com.example.geminispringboot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Import HttpSession
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Autowired
    private UserDaoService userDaoService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        logger.debug("Authentication failed for username: {}", username);

        User user = null;
        if (username != null) {
             user = userDaoService.getOne(new QueryWrapper<User>().eq("username", username));
        }
       
        String specificErrorMessage;
        if (user == null) {
            specificErrorMessage = "用户不存在";
            logger.debug("User {} not found. Setting specific error message to session: {}", username, specificErrorMessage);
        } else {
            specificErrorMessage = "密码错误";
            logger.debug("User {} found, but bad credentials. Setting specific error message to session: {}", username, specificErrorMessage);
        }

        // Store specific error message in session
        HttpSession session = request.getSession(false); // Get existing session, don't create new one
        if (session != null) {
            session.setAttribute("loginErrorSpecificMessage", specificErrorMessage);
            logger.debug("Specific error message stored in session.");
        } else {
            logger.warn("No active session found to store specific error message. Fallback to generic message might occur.");
        }
        
        response.sendRedirect(request.getContextPath() + "/login?error");
    }
}
