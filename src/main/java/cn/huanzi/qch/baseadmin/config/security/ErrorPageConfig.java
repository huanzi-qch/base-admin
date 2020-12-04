package cn.huanzi.qch.baseadmin.config.security;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 自定义errorPage
 */
@Component
public class ErrorPageConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {

        /**
         * ErrorPage 有两个参数
         * 参数1 响应状态码
         * 参数2 出现响应状态码的时候的跳转路径  可以自定义跳转路径
         */

        //将ErrorPage 注册到注册器中
        registry.addErrorPages(
                new ErrorPage(HttpStatus.FORBIDDEN, "/error/403"),
                new ErrorPage(HttpStatus.NOT_FOUND, "/error/404"),
                new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500")
                );
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
