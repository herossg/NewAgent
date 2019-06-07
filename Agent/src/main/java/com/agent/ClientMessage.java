package com.agent;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class ClientMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mUserid;
    private int mMsgid;
    private String mAd_flag;
    private String mBtn1;
    private String mBtn2;
    private String mBtn3;
    private String mBtn4;
    private String mBtn5;
    private String mImg_link;
    private String mImg_url;
    private String mMessage;
    private String mProfile;
    private String mTmpl_id;
    private String m1stMessage_type;
    private String m2ndMessage_type;
    private String mMsg_title;
    private String mSender;
    private String mReserve_date;
    private String mMMS1;
    private String mMMS2;
    private String mMMS3;
    private String mMMS4;
    private byte[] mImg1;
    private byte[] mImg2;
    private byte[] mImg3;
    private byte[] mImg4;
    
    private String[] mPhoneList;

	public String getmUserid() {
		return mUserid;
	}

	public void setmUserid(String mUserid) {
		this.mUserid = mUserid;
	}

	public int getmMsgid() {
		return mMsgid;
	}

	public void setmMsgid(int mMsgid) {
		this.mMsgid = mMsgid;
	}

	public String getmAd_flag() {
		return mAd_flag;
	}

	public void setmAd_flag(String mAd_flag) {
		this.mAd_flag = mAd_flag;
	}

	public String getmBtn1() {
		return mBtn1;
	}

	public void setmBtn1(String mBtn1) {
		this.mBtn1 = mBtn1;
	}

	public String getmBtn2() {
		return mBtn2;
	}

	public void setmBtn2(String mBtn2) {
		this.mBtn2 = mBtn2;
	}

	public String getmBtn4() {
		return mBtn4;
	}

	public void setmBtn4(String mBtn4) {
		this.mBtn4 = mBtn4;
	}

	public String getmBtn3() {
		return mBtn3;
	}

	public void setmBtn3(String mBtn3) {
		this.mBtn3 = mBtn3;
	}

	public String getmBtn5() {
		return mBtn5;
	}

	public void setmBtn5(String mBtn5) {
		this.mBtn5 = mBtn5;
	}

	public String getmImg_link() {
		return mImg_link;
	}

	public void setmImg_link(String mImg_link) {
		this.mImg_link = mImg_link;
	}

	public String getmImg_url() {
		return mImg_url;
	}

	public void setmImg_url(String mImg_url) {
		this.mImg_url = mImg_url;
	}

	public String getmMessage() {
		return mMessage;
	}

	public void setmMessage(String mMessage) {
		this.mMessage = mMessage;
	}

	public String getmProfile() {
		return mProfile;
	}

	public void setmProfile(String mProfile) {
		this.mProfile = mProfile;
	}

	public String getmTmpl_id() {
		return mTmpl_id;
	}

	public void setmTmpl_id(String mTmpl_id) {
		this.mTmpl_id = mTmpl_id;
	}

	public String getM1stMessage_type() {
		return m1stMessage_type;
	}

	public void setM1stMessage_type(String m1stMessage_type) {
		this.m1stMessage_type = m1stMessage_type;
	}

	public String getM2ndMessage_type() {
		return m2ndMessage_type;
	}

	public void setM2ndMessage_type(String m2ndMessage_type) {
		this.m2ndMessage_type = m2ndMessage_type;
	}

	public String getmMsg_title() {
		return mMsg_title;
	}

	public void setmMsg_title(String mMsg_title) {
		this.mMsg_title = mMsg_title;
	}

	public String getmSender() {
		return mSender;
	}

	public void setmSender(String mSender) {
		this.mSender = mSender;
	}

	public String getmReserve_date() {
		return mReserve_date;
	}

	public void setmReserve_date(String mReserve_date) {
		this.mReserve_date = mReserve_date;
	}

	public String getmMMS1() {
		return mMMS1;
	}

	public void setmMMS1(String mMMS1) {
		this.mMMS1 = mMMS1;
	}

	public String getmMMS2() {
		return mMMS2;
	}

	public void setmMMS2(String mMMS2) {
		this.mMMS2 = mMMS2;
	}

	public String getmMMS3() {
		return mMMS3;
	}

	public void setmMMS3(String mMMS3) {
		this.mMMS3 = mMMS3;
	}

	public String getmMMS4() {
		return mMMS4;
	}

	public void setmMMS4(String mMMS4) {
		this.mMMS4 = mMMS4;
	}

	public byte[] getmImg1() {
		return mImg1;
	}

	public void setmImg1(byte[] mImg1) {
		this.mImg1 = mImg1;
	}

	public byte[] getmImg2() {
		return mImg2;
	}

	public void setmImg2(byte[] mImg2) {
		this.mImg2 = mImg2;
	}

	public byte[] getmImg3() {
		return mImg3;
	}

	public void setmImg3(byte[] mImg3) {
		this.mImg3 = mImg3;
	}

	public byte[] getmImg4() {
		return mImg4;
	}

	public void setmImg4(byte[] mImg4) {
		this.mImg4 = mImg4;
	}

	public String[] getmPhoneList() {
		return mPhoneList;
	}

	public void setmPhoneList(String[] mPhoneList) {
		this.mPhoneList = mPhoneList;
	}
    
    
}
