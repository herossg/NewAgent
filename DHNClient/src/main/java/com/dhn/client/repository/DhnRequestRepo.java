package com.dhn.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dhn.client.model.DhnRequest;

@Repository
public interface DhnRequestRepo extends JpaRepository<DhnRequest, String>, JpaSpecificationExecutor<DhnRequest> {
	final static String DEL_MSGID = "delete from dhn_request where msgid = :msgid";
	
	@Modifying
	@Transactional
	@Query(value = DEL_MSGID, nativeQuery = true)
	public void deleteByMsgidQuery(@Param("msgid") String msgid);
}
