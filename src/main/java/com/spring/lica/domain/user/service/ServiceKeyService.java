package com.spring.lica.domain.user.service;

import com.spring.lica.common.exception.UserNotFoundException;
import com.spring.lica.common.util.AesEncryptionUtil;
import com.spring.lica.domain.user.dto.ServiceKeyRequest;
import com.spring.lica.domain.user.dto.ServiceKeyResponse;
import com.spring.lica.domain.user.entity.User;
import com.spring.lica.domain.user.entity.UserServiceKey;
import com.spring.lica.domain.user.repository.UserRepository;
import com.spring.lica.domain.user.repository.UserServiceKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceKeyService {

    private final UserServiceKeyRepository serviceKeyRepository;
    private final UserRepository userRepository;
    private final AesEncryptionUtil aesEncryptionUtil;

    @Transactional(readOnly = true)
    public List<ServiceKeyResponse> getKeys(String email) {
        User user = findUser(email);
        return serviceKeyRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(key -> {
                    String decrypted = aesEncryptionUtil.decrypt(key.getServiceKey());
                    return ServiceKeyResponse.from(key, decrypted);
                })
                .toList();
    }

    @Transactional
    public ServiceKeyResponse addKey(String email, ServiceKeyRequest request) {
        User user = findUser(email);
        String encrypted = aesEncryptionUtil.encrypt(request.getServiceKey());
        UserServiceKey key = UserServiceKey.builder()
                .user(user)
                .keyName(request.getKeyName())
                .serviceKey(encrypted)
                .build();
        UserServiceKey saved = serviceKeyRepository.save(key);
        return ServiceKeyResponse.from(saved, request.getServiceKey());
    }

    @Transactional
    public void deleteKey(String email, Long keyId) {
        User user = findUser(email);
        UserServiceKey key = serviceKeyRepository.findByIdAndUserId(keyId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("ServiceKey not found"));
        serviceKeyRepository.delete(key);
    }

    @Transactional(readOnly = true)
    public String decryptKey(String email, Long keyId) {
        User user = findUser(email);
        UserServiceKey key = serviceKeyRepository.findByIdAndUserId(keyId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("ServiceKey not found"));
        return aesEncryptionUtil.decrypt(key.getServiceKey());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
    }
}
