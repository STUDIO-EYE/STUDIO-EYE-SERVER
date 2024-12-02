package studio.studioeye.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Setter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id") // 외래 키 필드 추가
    private Long productId;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String encryptedPwd;

    @Column(nullable = false)
    private boolean isApproved;

    @Column(nullable = false)
    @CreatedDate
    private LocalDate createdAt;

    @Builder
    public User(Long product_id, String email, String name, String phoneNumber, String encryptedPwd, boolean isApproved) {
        this.productId = productId;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.encryptedPwd = encryptedPwd;
        this.isApproved = isApproved;
        this.createdAt = LocalDate.now();
    }
}
