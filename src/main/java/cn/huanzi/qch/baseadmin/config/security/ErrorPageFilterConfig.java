package cn.huanzi.qch.baseadmin.config.security;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 自定义errorPage拦截器
 */
@Component
public class ErrorPageFilterConfig implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (Arrays.asList(403,404, 500).contains(response.getStatus())) {
            response.sendRedirect("/error/" + response.getStatus());
            return;
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }
}

/**
 * 错误页面跳转
 */
@Controller
class ErrorPageController {
    @GetMapping("/error/403")
    public ModelAndView error403(){
        return new ModelAndView("common/error/403");
    }

    @GetMapping("/error/404")
    public ModelAndView error404(){
        return new ModelAndView("common/error/404");
    }

    @GetMapping("/error/500")
    public ModelAndView error500(){
        return new ModelAndView("common/error/500");
    }
}
