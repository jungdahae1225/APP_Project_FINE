package com.fine_server.controller.signup;

import com.fine_server.controller.mypage.errors.UserException;
import com.fine_server.entity.Member;
import com.fine_server.entity.mypage.MemberRequestDto;
import com.fine_server.service.mypage.MemberService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;

@Slf4j
@Controller
@AllArgsConstructor
public class SignUpController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Member> signUp(@RequestBody @Valid MemberRequestDto memberDto, BindingResult bindingResult, Errors errors) {
        if(bindingResult.hasErrors()){
            log.info("errors={}", bindingResult);
            throw new UserException("입력값이 잘못 되었습니다.");
        }
        Member member = memberService.saveNewAccount(memberDto);
        return new ResponseEntity(member, HttpStatus.OK);
    }

}
