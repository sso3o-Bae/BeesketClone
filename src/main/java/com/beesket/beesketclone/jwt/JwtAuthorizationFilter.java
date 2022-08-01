package com.beesket.beesketclone.jwt;

//시큐리티가 filter를 가지고 있는데 그 필터 중에 BasicAuthenticationFilter라는 것이 있음
//권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
//만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안 탄다.

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.beesket.beesketclone.model.User;
import com.beesket.beesketclone.repository.UserRepository;
import com.beesket.beesketclone.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;

    }

    //인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        System.out.println("인증이나 권한이 필요한 주소 요청이 됨");

        String jwtHeader = request.getHeader("Authorization");
        System.out.println("jwtHeader : " + jwtHeader);

        //header가 있는지 확인
        if(jwtHeader == null){
            chain.doFilter(request, response);
            return;
        }


        //JWT토큰을 검증을 해서 정상적인 사용자인지 확인
        //String jwtToken = request.getHeader("Authorization").replace("Bearer","");
        String email =
                JWT.require(Algorithm.HMAC512("cos")).build().verify(jwtHeader).getClaim("email").asString();


        //서명이 정상적으로 됨
        if(email != null){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new IllegalArgumentException("email이 없습니다.")
        );

            UserDetailsImpl userDetails = new UserDetailsImpl(user);


        //JWT 토큰 서명을 통해 서명이 정상이면 Authentication 객체를 만들어준다.
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        //강제로 시큐리티의 세션에 접근하여 Authentication 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request,response);
        }

    }
}
