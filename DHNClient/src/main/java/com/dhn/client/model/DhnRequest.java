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
@Table(name="DHN_REQUEST")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DhnRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "MSGID", nullable = false, length = 20)
	private String MSGID;  //` VARCHAR(20) NOT NULL,
	
	@Column(name = "AD_FLAG", nullable = true, length = 1)
	private String AD_FLAG;  //` VARCHAR(1) NULL DEFAULT NULL,
	
	@Column(name = "BUTTON1", nullable = true, columnDefinition = "LONGTEXT")
	private String BUTTON1; //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "BUTTON2", nullable = true, columnDefinition = "LONGTEXT")
	private String BUTTON2;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "BUTTON3", nullable = true, columnDefinition = "LONGTEXT")
	private String BUTTON3;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "BUTTON4", nullable = true, columnDefinition = "LONGTEXT")
	private String BUTTON4;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "BUTTON5", nullable = true, columnDefinition = "LONGTEXT")
	private String BUTTON5;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "IMAGE_LINK", nullable = true, columnDefinition = "LONGTEXT")
	private String IMAGE_LINK;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "IMAGE_URL", nullable = true, columnDefinition = "LONGTEXT")
	private String IMAGE_URL;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "MESSAGE_TYPE", nullable = true, length = 2)
	private String MESSAGE_TYPE;  //` VARCHAR(2) NULL DEFAULT NULL,
	
	@Column(name = "MSG", nullable = false, columnDefinition = "LONGTEXT")
	private String MSG;  //` LONGTEXT NOT NULL,
	
	@Column(name = "MSG_SMS", nullable = true, columnDefinition = "LONGTEXT")
	private String MSG_SMS;  //` LONGTEXT NULL DEFAULT NULL,
	
	@Column(name = "ONLY_SMS", nullable = true, length = 1)
	private String ONLY_SMS;  //` VARCHAR(1) NULL DEFAULT NULL,
	
	@Column(name = "P_COM", nullable = true, length = 2)
	private String P_COM;  //` VARCHAR(2) NULL DEFAULT NULL,
	
	@Column(name = "P_INVOICE", nullable = true, length = 100)
	private String P_INVOICE;  //` VARCHAR(100) NULL DEFAULT NULL,
	
	@Column(name = "PHN", nullable = false, length = 15)
	private String PHN;  //` VARCHAR(15) NOT NULL,
	
	@Column(name = "PROFILE", nullable = true, length = 50)
	private String PROFILE;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "REG_DT", nullable = false, length = 20)
	private String REG_DT;  //` DATETIME NOT NULL,
	
	@Column(name = "REMARK1", nullable = true, length = 50)
	private String REMARK1;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "REMARK2", nullable = true, length = 50)
	private String REMARK2;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "REMARK3", nullable = true, length = 50)
	private String REMARK3;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "REMARK4", nullable = true, length = 50)
	private String REMARK4;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "REMARK5", nullable = true, length = 50)
	private String REMARK5;  //` VARCHAR(50) NULL DEFAULT NULL,
	
	@Column(name = "RESERVE_DT", nullable = false, length = 14)
	private String RESERVE_DT;  //` VARCHAR(14) NOT NULL,
	
	@Column(name = "S_CODE", nullable = true, length = 2)
	private String S_CODE;  //` VARCHAR(2) NULL DEFAULT NULL,
	
	@Column(name = "SMS_KIND", nullable = true, length = 1)
	private String SMS_KIND;  //` VARCHAR(1) NULL DEFAULT NULL,
	
	@Column(name = "SMS_LMS_TIT", nullable = true, length = 120)
	private String SMS_LMS_TIT;  //` VARCHAR(120) NULL DEFAULT NULL,
	
	@Column(name = "SMS_SENDER", nullable = true, length = 15)
	private String SMS_SENDER;  //` VARCHAR(15) NULL DEFAULT NULL,
	
	@Column(name = "TMPL_ID", nullable = true, length = 30)
	private String TMPL_ID;  //` VARCHAR(30) NULL DEFAULT NULL,
	
	@Column(name = "WIDE", nullable = true, length = 1)
	private String WIDE;  //` CHAR(1) NULL DEFAULT 'N'

	public String getMSGID() {
		return MSGID;
	}

	public String getAD_FLAG() {
		return AD_FLAG;
	}

	public String getBUTTON1() {
		return BUTTON1;
	}

	public String getBUTTON2() {
		return BUTTON2;
	}

	public String getBUTTON3() {
		return BUTTON3;
	}

	public String getBUTTON4() {
		return BUTTON4;
	}

	public String getBUTTON5() {
		return BUTTON5;
	}

	public String getIMAGE_LINK() {
		return IMAGE_LINK;
	}

	public String getIMAGE_URL() {
		return IMAGE_URL;
	}

	public String getMESSAGE_TYPE() {
		return MESSAGE_TYPE;
	}

	public String getMSG() {
		return MSG;
	}

	public String getMSG_SMS() {
		return MSG_SMS;
	}

	public String getONLY_SMS() {
		return ONLY_SMS;
	}

	public String getP_COM() {
		return P_COM;
	}

	public String getP_INVOICE() {
		return P_INVOICE;
	}

	public String getPHN() {
		return PHN;
	}

	public String getPROFILE() {
		return PROFILE;
	}

	public String getREG_DT() {
		return REG_DT;
	}

	public String getREMARK1() {
		return REMARK1;
	}

	public String getREMARK2() {
		return REMARK2;
	}

	public String getREMARK3() {
		return REMARK3;
	}

	public String getREMARK4() {
		return REMARK4;
	}

	public String getREMARK5() {
		return REMARK5;
	}

	public String getRESERVE_DT() {
		return RESERVE_DT;
	}

	public String getS_CODE() {
		return S_CODE;
	}

	public String getSMS_KIND() {
		return SMS_KIND;
	}

	public String getSMS_LMS_TIT() {
		return SMS_LMS_TIT;
	}

	public String getSMS_SENDER() {
		return SMS_SENDER;
	}

	public String getTMPL_ID() {
		return TMPL_ID;
	}

	public String getWIDE() {
		return WIDE;
	}

}
