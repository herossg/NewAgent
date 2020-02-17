package com.dhn.client.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dhn.client.model.DhnResult;

@Repository
public interface DhnResultRepo extends JpaRepository<DhnResult, String> {
	
	final static String RESULT_UPDATE = "update dhn_result set code = :code , message = :message, result='Y', sync='Y' where msgid = :msgid";
	
	List<DhnResult> findByResult(String result);
	
	@Modifying
	@Transactional
	@Query(value = RESULT_UPDATE, nativeQuery = true)
	public void updateByMsgidQuery(@Param("msgid") String msgid, @Param("code") String code, @Param("message") String message);
	
	
}
