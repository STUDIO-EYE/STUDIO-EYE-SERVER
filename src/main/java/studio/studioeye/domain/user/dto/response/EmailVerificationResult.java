package studio.studioeye.domain.user.dto.response;

public class EmailVerificationResult {

    private boolean verificationStatus;
    private String message;

    private EmailVerificationResult(boolean verificationStatus, String message) {
        this.verificationStatus = verificationStatus;
        this.message = message;
    }

    public static EmailVerificationResult of(boolean verificationStatus) {
        String message = verificationStatus ? "Email verification successful" : "Email verification failed";
        return new EmailVerificationResult(verificationStatus, message);
    }

    public boolean isVerificationStatus() {
        return verificationStatus;
    }

    public String getMessage() {
        return message;
    }
}
