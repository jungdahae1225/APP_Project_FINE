package com.fine_server.service.chat;

import com.fine_server.entity.ChatMember;
import com.fine_server.entity.ChatMessage;
import com.fine_server.entity.ChatRoom;
import com.fine_server.entity.Member;
import com.fine_server.entity.chat.ChatMessageDto;
import com.fine_server.entity.chat.MessageType;
import com.fine_server.entity.chat.ReturnChatMessageDto;
import com.fine_server.repository.ChatMessageRepository;
import com.fine_server.repository.ChatRoomRepository;
import com.fine_server.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final MemberRepository memberRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatRoomRepository chatRoomRepository;

    public ReturnChatMessageDto makeChatMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = chatMessageDto.toEntity();

        Member member = findMember(chatMessageDto.getMemberId());
        chatMessage.setMember(member);

        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId());
        ChatRoom chatRoom = optionalChatRoom.get();
        chatMessage.setChatRoom(chatRoom);
        List<ChatMember> chatMemberList = chatRoom.getChatMemberList();

        for(ChatMember chatMember : chatMemberList) {
            if(chatMember.getId().equals(chatMessageDto.getMemberId())) {
                int lastReadPoint = chatRoom.getChatMessageList().size();
                chatMember.setLastReadPoint(lastReadPoint);
            }
        }

        int unreadCount = this.getUnreadCount(chatMemberList);

        chatMessage.setUnreadCount(unreadCount);

        chatMessageRepository.save(chatMessage);

        LocalDateTime createdTime = chatMessage.getCreatedDate();

        ReturnChatMessageDto message = new ReturnChatMessageDto(
                chatMessageDto.getType(), chatMessageDto.getRoomId(),
                member.getMemberIdAndNicknameAndUserImageNumInfo(),
                chatMessageDto.getMessage(), unreadCount, createdTime);

        return message;
    }

    public ReturnChatMessageDto enterRoom(Long roomId, Long memberId) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(roomId);
        ChatRoom chatRoom = optionalChatRoom.get();

        for(ChatMember chatMember : chatRoom.getChatMemberList()) {
            if(chatMember.getMember().getId().equals(memberId)) {
                ChatMember myChatMember = chatMember;
                chatMember.setPresentPosition(true);
                this.readMessage(chatRoom, myChatMember);

                int lastReadPoint = chatRoom.getChatMessageList().size();
                chatMember.setLastReadPoint(lastReadPoint);

                ReturnChatMessageDto message = new ReturnChatMessageDto(
                        MessageType.ENTER, roomId,
                        myChatMember.getMember().getMemberIdAndNicknameAndUserImageNumInfo(),
                        "", 0, LocalDateTime.now());

                return message;
            }
        }
        return null;
    }

    public void quitRoom(Long roomId, Long memberId) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(roomId);
        ChatRoom chatRoom = optionalChatRoom.get();


        for(ChatMember chatMember : chatRoom.getChatMemberList()) {
            if(chatMember.getMember().getId().equals(memberId)) {
                chatMember.setPresentPosition(false);

                int lastReadPoint = chatRoom.getChatMessageList().size();
                chatMember.setLastReadPoint(lastReadPoint);

                break;
            }
        }
    }

    public int getUnreadCount(List<ChatMember> chatMemberList) {
        int totalCount = chatMemberList.size();

        int readCount = 0;
        for(ChatMember chatMember : chatMemberList) {
            if(chatMember.getPresentPosition().equals(true)) {
                readCount++;
            }
        }

        int unreadCount = totalCount - readCount;
        return unreadCount;
    }

    public Member findMember(Long memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.get();
        return member;
    }

    public void readMessage(ChatRoom chatRoom, ChatMember myChatMember) {
        List<ChatMessage> messageList = chatRoom.getChatMessageList();

        Timestamp recentOutTime = myChatMember.getRecentOutTime();
        for(ChatMessage chatMessage : messageList) {
            LocalDateTime createdDate = chatMessage.getCreatedDate();
            Timestamp createTime = Timestamp.valueOf(createdDate);
            if(createTime.after(recentOutTime)) { // A가 B보다 늦은 경우 즉, 생성된 시간이 방에서 나간 시간보다 늦은 경우 즉, 읽지 않은 경우
                int unreadCount = chatMessage.getUnreadCount();
                if(unreadCount > 0) {
                    chatMessage.setUnreadCount(chatMessage.getUnreadCount()-1);
                }
            }
        }
    }
}
