package io.grpc.netty;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitializeFeatTest {

    private static Logger logger = LoggerFactory.getLogger(InitializeFeatTest.class);

    public static void featTest() {
        boolean jettyAlpnConfigured = JettyTlsUtil.isJettyAlpnConfigured();
        boolean jettyNpnConfigured = JettyTlsUtil.isJettyNpnConfigured();
        boolean java9AlpnAvailable = JettyTlsUtil.isJava9AlpnAvailable();

        logger.info(
                " ==> jettyAlpnConfigured: {}, jettyNpnConfigured: {}, java9AlpnAvailable: {}",
                jettyAlpnConfigured,
                jettyNpnConfigured,
                java9AlpnAvailable);

        Provider[] providers = Security.getProviders("SSLContext.TLS");
        if (Objects.isNull(providers)) {
            logger.info(" ==> providers is null");
        } else {
            logger.info(" ==> providers length: " + providers.length);
            for (int i = 0; i < providers.length; i++) {
                logger.info(
                        " ==> "
                                + i
                                + ". name： "
                                + providers[i].getName()
                                + " ,version: "
                                + providers[i].getVersion());
            }
        }

        Throwable throwable = checkAlpnAvailability();
        logger.error("checkAlpnAvailability: ", throwable);
    }

    static Throwable checkAlpnAvailability() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init((KeyManager[]) null, (TrustManager[]) null, (SecureRandom) null);
            SSLEngine engine = context.createSSLEngine();
            Method getApplicationProtocol =
                    (Method)
                            AccessController.doPrivileged(
                                    new PrivilegedExceptionAction<Method>() {
                                        public Method run() throws Exception {
                                            return SSLEngine.class.getMethod(
                                                    "getApplicationProtocol");
                                        }
                                    });
            getApplicationProtocol.invoke(engine);
            return null;
        } catch (Throwable var3) {
            return var3;
        }
    }
}
