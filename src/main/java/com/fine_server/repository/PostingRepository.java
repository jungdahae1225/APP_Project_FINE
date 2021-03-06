package com.fine_server.repository;

import com.fine_server.entity.Member;
import com.fine_server.entity.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * written by eunhye
 * date: 22.06.16
 */

/**
 * edit by.dahae
 * date: 22.06.23
 * JPA 리포지토리 사용 필요로 상속 추가
 */
public interface PostingRepository  extends JpaRepository<Posting, Long>, PostingCustomRepository {

    List<Posting> findByMemberId(Long memberId);
}
