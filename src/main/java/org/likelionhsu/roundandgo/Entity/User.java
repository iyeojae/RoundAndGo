package org.likelionhsu.roundandgo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.likelionhsu.roundandgo.Common.LoginType;
import org.likelionhsu.roundandgo.Common.ProfileColor;
import org.likelionhsu.roundandgo.Common.Role;
import org.likelionhsu.roundandgo.Common.Timestamped;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @Column(unique = true)
    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProfileColor profileColor = ProfileColor.PINK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean isActived;
}