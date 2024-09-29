package studio.studioeye.domain.user.dto.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private LocalDate createdAt;
    private boolean isApproved;
    private String message;

    public UserResponse(Long id, String email, String name, String phoneNumber,
                        LocalDate createdAt, boolean isApproved) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.isApproved = isApproved;
    }

    public UserResponse(String message) {
        this.message = message;
    }
}