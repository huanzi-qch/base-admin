package cn.huanzi.qch.baseadmin.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.*;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 自定义rememberMeServices
 */
public class MyPersistentTokenBasedRememberMeServices extends AbstractRememberMeServices {
    private PersistentTokenRepository tokenRepository = new InMemoryTokenRepositoryImpl();
    private SecureRandom random = new SecureRandom();
    public static final int DEFAULT_SERIES_LENGTH = 16;
    public static final int DEFAULT_TOKEN_LENGTH = 16;
    private int seriesLength = 16;
    private int tokenLength = 16;

    public MyPersistentTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService);
        this.tokenRepository = tokenRepository;
    }

    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
        if (cookieTokens.length != 2) {
            throw new InvalidCookieException("Cookie token did not contain 2 tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
        } else {
            String presentedSeries = cookieTokens[0];
            String presentedToken = cookieTokens[1];
            PersistentRememberMeToken token = this.tokenRepository.getTokenForSeries(presentedSeries);
            if (token == null) {
                throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
            } else if (!presentedToken.equals(token.getTokenValue())) {
                //这里不再数据操作，交由LogoutHandlerConfig处理

//                this.tokenRepository.removeUserTokens(token.getUsername());
                throw new CookieTheftException(this.messages.getMessage("PersistentTokenBasedRememberMeServices.cookieStolen", "Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack."));
            } else if (token.getDate().getTime() + (long)this.getTokenValiditySeconds() * 1000L < System.currentTimeMillis()) {
                throw new RememberMeAuthenticationException("Remember-me login has expired");
            } else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Refreshing persistent login token for user '" + token.getUsername() + "', series '" + token.getSeries() + "'");
                }
                //这里不再数据操作，交由LogoutHandlerConfig处理

//                PersistentRememberMeToken newToken = new PersistentRememberMeToken(token.getUsername(), token.getSeries(), this.generateTokenData(), new Date());
//
//                try {
//                    this.tokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(), newToken.getDate());
//                    this.addCookie(newToken, request, response);
//                } catch (Exception var9) {
//                    this.logger.error("Failed to update token: ", var9);
//                    throw new RememberMeAuthenticationException("Autologin failed due to data access problem");
//                }

                return this.getUserDetailsService().loadUserByUsername(token.getUsername());
            }
        }
    }

    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        //这里不再数据操作，交由LogoutHandlerConfig处理

//        String username = successfulAuthentication.getName();
//        this.logger.debug("Creating new persistent login for user " + username);
//        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(username, this.generateSeriesData(), this.generateTokenData(), new Date());
//
//        try {
//            this.tokenRepository.createNewToken(persistentToken);
//            this.addCookie(persistentToken, request, response);
//        } catch (Exception var7) {
//            this.logger.error("Failed to save persistent token ", var7);
//        }

    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //这里不再做数据操作，交由LogoutHandlerConfig处理

//        super.logout(request, response, authentication);
//        if (authentication != null) {
//            this.tokenRepository.removeUserTokens(authentication.getName());
//        }

    }

    public String generateSeriesData() {
        byte[] newSeries = new byte[this.seriesLength];
        this.random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    public String generateTokenData() {
        byte[] newToken = new byte[this.tokenLength];
        this.random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }

    public void removeCookie(HttpServletRequest request, HttpServletResponse response){
        super.cancelCookie(request, response);
    }

    public void addCookie(PersistentRememberMeToken token, HttpServletRequest request, HttpServletResponse response) {
        this.setCookie(new String[]{token.getSeries(), token.getTokenValue()}, this.getTokenValiditySeconds(), request, response);
    }

    public void setSeriesLength(int seriesLength) {
        this.seriesLength = seriesLength;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        Assert.isTrue(tokenValiditySeconds > 0, "tokenValiditySeconds must be positive for this implementation");
        super.setTokenValiditySeconds(tokenValiditySeconds);
    }
}