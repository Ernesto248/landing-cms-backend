package com.jenislashes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Refresh refresh = new Refresh();
    private Admin admin = new Admin();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    public void setRefresh(Refresh refresh) {
        this.refresh = refresh;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public static class Jwt {
        private String issuer = "jeni-backend";
        private String secret = "change-me";
        private long accessTokenMinutes = 15;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenMinutes() {
            return accessTokenMinutes;
        }

        public void setAccessTokenMinutes(long accessTokenMinutes) {
            this.accessTokenMinutes = accessTokenMinutes;
        }
    }

    public static class Refresh {
        private long tokenDays = 14;
        private String cookieName = "jeni_refresh_token";
        private String cookieSameSite = "auto";
        private String cookieSecure = "auto";

        public long getTokenDays() {
            return tokenDays;
        }

        public void setTokenDays(long tokenDays) {
            this.tokenDays = tokenDays;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public String getCookieSameSite() {
            return cookieSameSite;
        }

        public void setCookieSameSite(String cookieSameSite) {
            this.cookieSameSite = cookieSameSite;
        }

        public String getCookieSecure() {
            return cookieSecure;
        }

        public void setCookieSecure(String cookieSecure) {
            this.cookieSecure = cookieSecure;
        }
    }

    public static class Admin {
        private String email = "";
        private String password = "";
        private String fullName = "Jeni Admin";

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
