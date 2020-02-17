package com.dhn.client.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="DHN_RESULT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DhnResult implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "msgid", nullable = false, length = 20)
	private String msgid; //` varchar(20) not null,

	@Column(name = "ad_flag", length = 1)
	private String ad_flag; //` varchar(1) null default null,

	@Column(name = "button1", columnDefinition = "LONGTEXT")
	private String button1; //` longtext null default null,

	@Column(name = "button2", columnDefinition = "LONGTEXT")
	private String button2; //` longtext null default null,

	@Column(name = "button3", columnDefinition = "LONGTEXT")
	private String button3; //` longtext null default null,

	@Column(name = "button4", columnDefinition = "LONGTEXT")
	private String button4; //` longtext null default null,

	@Column(name = "button5", columnDefinition = "LONGTEXT")
	private String button5; //` longtext null default null,

	@Column(name = "code", length = 4)
	private String code; //` varchar(4) null default null,

	@Column(name = "image_link", columnDefinition = "LONGTEXT")
	private String image_link; //` longtext null default null,

	@Column(name = "image_url", columnDefinition = "LONGTEXT")
	private String image_url; //` longtext null default null,

	@Column(name = "kind", length = 1)
	private String kind; //` varchar(1) null default null,

	@Column(name = "message", columnDefinition = "LONGTEXT")
	private String message; //` longtext null default null,

	@Column(name = "message_type", length = 2)
	private String message_type; //` varchar(2) null default null,

	@Column(name = "msg", nullable = false, columnDefinition = "LONGTEXT")
	private String msg; //` longtext not null,

	@Column(name = "msg_sms", columnDefinition = "LONGTEXT")
	private String msg_sms; //` longtext null default null,

	@Column(name = "only_sms", length = 1)
	private String only_sms; //` varchar(1) null default null,

	@Column(name = "p_com", length = 2)
	private String p_com; //` varchar(2) null default null,

	@Column(name = "p_invoice", length = 100)
	private String p_invoice; //` varchar(100) null default null,

	@Column(name = "phn", nullable = false, length = 15)
	private String phn; //` varchar(15) not null,

	@Column(name = "profile", length = 50)
	private String profile; //` varchar(50) null default null,

	@Column(name = "reg_dt", nullable = false, length = 20)
	private String reg_dt; //` datetime not null,

	@Column(name = "remark1", length = 50)
	private String remark1; //` varchar(50) null default null,

	@Column(name = "remark2", length = 50)
	private String remark2; //` varchar(50) null default null,

	@Column(name = "remark3", length = 50)
	private String remark3; //` varchar(50) null default null,

	@Column(name = "remark4", length = 50)
	private String remark4; //` varchar(50) null default null,

	@Column(name = "remark5", length = 50)
	private String remark5; //` varchar(50) null default null,

	@Column(name = "res_dt", length = 20)
	private String res_dt; //` datetime null default null,

	@Column(name = "reserve_dt", nullable = false, length = 14)
	private String reserve_dt; //` varchar(14) not null,

	@Column(name = "result", length = 1)
	private String result; //` varchar(1) null default null,

	@Column(name = "s_code", length = 2)
	private String s_code; //` varchar(2) null default null,

	@Column(name = "sms_kind", length = 1)
	private String sms_kind; //` varchar(1) null default null,

	@Column(name = "sms_lms_tit", length = 120)
	private String sms_lms_tit; //` varchar(120) null default null,

	@Column(name = "sms_sender", length = 15)
	private String sms_sender; //` varchar(15) null default null,

	@Column(name = "sync", nullable = false, length = 1)
	private String sync; //` varchar(1) not null,

	@Column(name = "tmpl_id", length = 30)
	private String tmpl_id; //` varchar(30) null default null,

	@Column(name = "wide", length = 1)
	private String wide; //` char(1) null default 'n' 

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getAd_flag() {
		return ad_flag;
	}

	public void setAd_flag(String ad_flag) {
		this.ad_flag = ad_flag;
	}

	public String getButton1() {
		return button1;
	}

	public void setButton1(String button1) {
		this.button1 = button1;
	}

	public String getButton2() {
		return button2;
	}

	public void setButton2(String button2) {
		this.button2 = button2;
	}

	public String getButton3() {
		return button3;
	}

	public void setButton3(String button3) {
		this.button3 = button3;
	}

	public String getButton4() {
		return button4;
	}

	public void setButton4(String button4) {
		this.button4 = button4;
	}

	public String getButton5() {
		return button5;
	}

	public void setButton5(String button5) {
		this.button5 = button5;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getImage_link() {
		return image_link;
	}

	public void setImage_link(String image_link) {
		this.image_link = image_link;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage_type() {
		return message_type;
	}

	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg_sms() {
		return msg_sms;
	}

	public void setMsg_sms(String msg_sms) {
		this.msg_sms = msg_sms;
	}

	public String getOnly_sms() {
		return only_sms;
	}

	public void setOnly_sms(String only_sms) {
		this.only_sms = only_sms;
	}

	public String getP_com() {
		return p_com;
	}

	public void setP_com(String p_com) {
		this.p_com = p_com;
	}

	public String getP_invoice() {
		return p_invoice;
	}

	public void setP_invoice(String p_invoice) {
		this.p_invoice = p_invoice;
	}

	public String getPhn() {
		return phn;
	}

	public void setPhn(String phn) {
		this.phn = phn;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getReg_dt() {
		return reg_dt;
	}

	public void setReg_dt(String reg_dt) {
		this.reg_dt = reg_dt;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getRemark4() {
		return remark4;
	}

	public void setRemark4(String remark4) {
		this.remark4 = remark4;
	}

	public String getRemark5() {
		return remark5;
	}

	public void setRemark5(String remark5) {
		this.remark5 = remark5;
	}

	public String getRes_dt() {
		return res_dt;
	}

	public void setRes_dt(String res_dt) {
		this.res_dt = res_dt;
	}

	public String getReserve_dt() {
		return reserve_dt;
	}

	public void setReserve_dt(String reserve_dt) {
		this.reserve_dt = reserve_dt;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getS_code() {
		return s_code;
	}

	public void setS_code(String s_code) {
		this.s_code = s_code;
	}

	public String getSms_kind() {
		return sms_kind;
	}

	public void setSms_kind(String sms_kind) {
		this.sms_kind = sms_kind;
	}

	public String getSms_lms_tit() {
		return sms_lms_tit;
	}

	public void setSms_lms_tit(String sms_lms_tit) {
		this.sms_lms_tit = sms_lms_tit;
	}

	public String getSms_sender() {
		return sms_sender;
	}

	public void setSms_sender(String sms_sender) {
		this.sms_sender = sms_sender;
	}

	public String getSync() {
		return sync;
	}

	public void setSync(String sync) {
		this.sync = sync;
	}

	public String getTmpl_id() {
		return tmpl_id;
	}

	public void setTmpl_id(String tmpl_id) {
		this.tmpl_id = tmpl_id;
	}

	public String getWide() {
		return wide;
	}

	public void setWide(String wide) {
		this.wide = wide;
	}



}
