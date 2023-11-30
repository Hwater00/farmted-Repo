package com.farmted.passservice.service;

import com.farmted.passservice.domain.Pass;
import com.farmted.passservice.dto.request.RequestCreatePassDto;
import com.farmted.passservice.dto.request.RequestLoginDto;
import com.farmted.passservice.enums.TokenType;
import com.farmted.passservice.repository.PassRepository;
import com.farmted.passservice.util.redis.RedisRepository;
import com.farmted.passservice.util.redis.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassServiceImpl implements PassService {

    private final PassRepository passRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RedisRepository redisRepository;

    @Override
    @Transactional
    public void createPass(RequestCreatePassDto dto) {
        duplicateUserCheck(dto);
        String password = dto.getPassword();
        dto.setPassword(passwordEncoder.encode(password));
        Pass pass = dto.toEntity();
        passRepository.save(pass);
    }

    @Override
    @Transactional
    public String login(RequestLoginDto dto) {
        Pass pass = passRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getPassword(), pass.getPassword()))
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");

        String accessTokenInfo = tokenService.createToken(pass.getUuid(), pass.getRole(), TokenType.ACCESS);
        String refreshTokenInfo = tokenService.createToken(pass.getUuid(), pass.getRole(), TokenType.REFRESH);

        tokenService.saveRefreshToken(pass.getUuid(), refreshTokenInfo);

        log.info("로그인 성공");

        return accessTokenInfo;
    }

    @Override
    @Transactional
    public void logout(String uuid) {
        try {
            Optional<RefreshToken> token = redisRepository.findById(uuid);
            if (token.isPresent()) {
                redisRepository.deleteById(uuid);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    // email 중복 검사
    private void duplicateUserCheck(RequestCreatePassDto dto) {
        Optional<Pass> pass = passRepository.findByEmail(dto.getEmail());

        if(pass.isPresent()) throw new RuntimeException("이미 존재하는 회원입니다.");
    }

}
