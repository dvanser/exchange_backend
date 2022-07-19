package com.exchange.security.TwoFA;

import com.exchange.security.TwoFA.api.Base32;
import com.exchange.security.TwoFA.api.Clock;
import com.exchange.security.TwoFA.api.Digits;
import com.exchange.security.TwoFA.api.Hash;
import com.exchange.security.TwoFA.api.Hmac;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Implements Time-based One-time Password algorithm
 */

public class Totp {

    // Is using google charts to draw QR code
    private static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    private final String secret;
    private final Clock clock;
    private static final int DELAY_WINDOW = 1;

    /**
     * Initialize an OTP instance with the shared secret generated on Registration process
     *
     * @param secret Shared secret
     */
    public Totp(String secret, int inverval) {
        this.secret = secret;
        clock = new Clock(inverval);
    }

    /**
     * Initialize an OTP instance with the shared secret generated on Registration process
     *
     * @param secret Shared secret
     * @param clock  Clock responsible for retrieve the current interval
     */
    public Totp(String secret, Clock clock) {
        this.secret = secret;
        this.clock = clock;
    }

    /**
     * Verifier - To be used only on the server side
     *
     * Taken from Google Authenticator with small modifications from
     * <a href="http://code.google.com/p/google-authenticator/source/browse/src/com/google/android/apps/authenticator/PasscodeGenerator.java?repo=android#212">PasscodeGenerator.java</a>
     *
     * Verify a timeout code. The timeout code will be valid for a time
     * determined by the interval period and the number of adjacent intervals
     * checked.
     *
     * @param otp Timeout code
     * @return True if the timeout code is valid
     *
     *         Author: sweis@google.com (Steve Weis)
     */
    public boolean verify(String otp) {
        long code = Long.parseLong(otp);
        long currentInterval = clock.getCurrentInterval();
        int pastResponse = Math.max(DELAY_WINDOW, 0);

        for (int i = pastResponse; i >= 0; --i) {
            int candidate = generate(this.secret, currentInterval - i);
            if (candidate == code) {
                return true;
            }
        }
        return false;
    }

    private int generate(String secret, long interval) {
        return hash(secret, interval);
    }

    private int hash(String secret, long interval) {
        byte[] hash = new byte[0];
        try {
            //Base32 encoding is just a requirement for google authenticator. We can remove it on the next releases.
            hash = new Hmac(Hash.SHA1, Base32.decode(secret), interval).digest();
        } catch (NoSuchAlgorithmException | Base32.DecodingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return bytesToInt(hash);
    }

    private int bytesToInt(byte[] hash) {
        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        return binary % Digits.SIX.getValue();
    }

    public String generateQRUrl(String appName, String userEmail) throws UnsupportedEncodingException {
        return QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", appName, userEmail, this.secret, appName),"UTF-8");
    }

    public int generateSmsCode() {
        return generate(this.secret, clock.getCurrentInterval());
    }
}
