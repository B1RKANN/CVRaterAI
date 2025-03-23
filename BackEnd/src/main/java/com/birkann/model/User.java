package com.birkann.model;

import java.util.Collection;
import java.util.List;
import java.time.Instant;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails{
	
	@Column
	private String name;
	
	@Column
	private String email;
	
	@Column
	private String password;
	
	@Column
	@Enumerated(EnumType.STRING)
	private Role role = Role.USER; // Varsayılan olarak USER rolü atanacak
	
	// OAuth2 işlemleri için ek alanlar
	@Column(length = 5000)
	private String oAuth2AccessToken;
	
	@Column
	private Instant oAuth2TokenExpiresAt;
	
	@Column(length = 2000)
	private String oAuth2RefreshToken;
	
	@Column
	private String oAuth2RegistrationId; // Örn: "google", "github"

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return email;
	}
	
	@OneToOne
	private Credit credit;
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	/**
	 * OAuth2 token'ın süresi dolup dolmadığını kontrol eder
	 * @return Token süresi dolduysa true, dolmadıysa false
	 */
	public boolean isOAuth2TokenExpired() {
		if (oAuth2TokenExpiresAt == null) {
			return true;
		}
		return Instant.now().isAfter(oAuth2TokenExpiresAt);
	}
}
