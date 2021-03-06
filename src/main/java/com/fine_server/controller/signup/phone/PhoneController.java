package com.fine_server.controller.signup.phone;

import com.fine_server.controller.mypage.errors.UserException;
import com.fine_server.controller.signup.dto.PhoneRequestDto;
import com.fine_server.controller.signup.dto.PhoneResponseDto;
import com.fine_server.controller.signup.dto.TokenDto;
import com.fine_server.entity.Member;
import com.fine_server.repository.MemberRepository;
import com.fine_server.service.mypage.PhoneService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

/**
 * written by dahae
 * date: 22.07.20
 */

@Slf4j
@AllArgsConstructor
@Controller
public class PhoneController {
    private PhoneService phoneService;
    private MemberRepository memberRepository;
    private InfraData infraData;
    private final DefaultMessageService messageService;

    public PhoneController() {
        this.messageService = NurigoApp.INSTANCE.initialize("NCS3GI6MWOPFXKTB", "AP3FMR4GFEPHD0DIM1DUXBOTZPGPWV6A", "https://api.coolsms.co.kr");
    }

    @PostMapping("/authMessage/{memberId}")
    public ResponseEntity<PhoneRequestDto> sendOne(HttpServletRequest request, @PathVariable Long memberId, @RequestBody @Valid PhoneRequestDto phoneRequestDto, BindingResult bindingResult) {
        Message message = new Message();

        HttpSession session = request.getSession();
        session.setAttribute("id", memberId);
        session.setAttribute("token", generateCheckToken());

        message.setFrom("01063001337");
        message.setTo(phoneRequestDto.getPhoneNumber());
        message.setText("??????????????? FINE?????????.\n\n????????? ??????????????? ??????????????????!!\n\n" + (String) session.getAttribute("token"));

        this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return new ResponseEntity(phoneRequestDto, HttpStatus.OK);
    }

    public String generateCheckToken() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        uuid = uuid.substring(0, 10);
        return uuid;
    }

    @PostMapping("/phoneVerification/{memberId}")
    public ResponseEntity emailVerification(HttpServletRequest request, @RequestBody @Valid TokenDto tokenDto, @PathVariable Long memberId, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            log.info("errors={}", bindingResult);
            throw new UserException("???????????? ?????? ???????????????.");
        }

        PhoneResponseDto dto = phoneService.phoneVerification(request.getSession(), tokenDto.getToken());

        if(dto == null){
            throw new UserException("??????????????? ?????? ????????????.");
        } else{
            request.getSession().invalidate();

            Optional<Member> fineMember = memberRepository.findById(memberId);
            Member member = fineMember.get();

            member.setUserPhoneNumber(dto.getPhoneNumber());
            member.setLevel((member.getLevel() + 1)); //????????? + 1
            return ResponseEntity.ok().build();
        }
    }
}
