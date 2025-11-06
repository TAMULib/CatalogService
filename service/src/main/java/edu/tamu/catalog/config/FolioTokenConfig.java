package edu.tamu.catalog.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "okapi.auth")
public class FolioTokenConfig {

    /**
     * FOLIO Access Token Cookie header name.
     */
    @Value("${okapi.auth.accessCookieName:folioAccessToken}")
    private String accessCookieName;

    /**
     * An offset in milliseconds to apply when calculating token expiration.
     *
     * A value of 15 would mean that a token that expires on or before 15 seconds from "now" is considered expired.
     */
    @Value("${okapi.auth.expireOffset:15}")
    private Long expireOffset;

    /**
     * The URL path to use when attempting to log into FOLIO.
     */
    @Value("${okapi.auth.loginPath:/authn/login-with-expiry}")
    private String loginPath;

    /**
     * FOLIO Refresh Token Cookie header name.
     */
    @Value("${okapi.auth.refreshCookieName:folioRefreshToken}")
    private String refreshCookieName;

}
