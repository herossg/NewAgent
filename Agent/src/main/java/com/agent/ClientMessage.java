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
    private int[] mDeatilMsgid;
    
	public String getUserid() {
		return mUserid;
	}

	public void setUserid(String mUserid) {
		this.mUserid = mUserid;
	}

	public int getMsgid() {
		return mMsgid;
	}

	public void setMsgid(int mMsgid) {
		this.mMsgid = mMsgid;
	}

	public String getAd_flag() {
		return mAd_flag;
	}

	public void setAd_flag(String mAd_flag) {
		this.mAd_flag = mAd_flag;
	}

	public String getBtn1() {
		return mBtn1;
	}

	public void setBtn1(String mBtn1) {
		this.mBtn1 = mBtn1;
	}

	public String getBtn2() {
		return mBtn2;
	}

	public void setBtn2(String mBtn2) {
		this.mBtn2 = mBtn2;
	}

	public String getBtn4() {
		return mBtn4;
	}

	public void setBtn4(String mBtn4) {
		this.mBtn4 = mBtn4;
	}

	public String getBtn3() {
		return mBtn3;
	}

	public void setBtn3(String mBtn3) {
		this.mBtn3 = mBtn3;
	}

	public String getBtn5() {
		return mBtn5;
	}

	public void setBtn5(String mBtn5) {
		this.mBtn5 = mBtn5;
	}

	public String getImg_link() {
		return mImg_link;
	}

	public void setImg_link(String mImg_link) {
		this.mImg_link = mImg_link;
	}

	public String getImg_url() {
		return mImg_url;
	}

	public void setImg_url(String mImg_url) {
		this.mImg_url = mImg_url;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String mMessage) {
		this.mMessage = mMessage;
	}

	public String getProfile() {
		return mProfile;
	}

	public void setProfile(String mProfile) {
		this.mProfile = mProfile;
	}

	public String getTmpl_id() {
		return mTmpl_id;
	}

	public void setTmpl_id(String mTmpl_id) {
		this.mTmpl_id = mTmpl_id;
	}

	public String get1stMessage_type() {
		return m1stMessage_type;
	}

	public void set1stMessage_type(String m1stMessage_type) {
		this.m1stMessage_type = m1stMessage_type;
	}

	public String get2ndMessage_type() {
		return m2ndMessage_type;
	}

	public void set2ndMessage_type(String m2ndMessage_type) {
		this.m2ndMessage_type = m2ndMessage_type;
	}

	public String getsg_title() {
		return mMsg_title;
	}

	public void setMsg_title(String mMsg_title) {
		this.mMsg_title = mMsg_title;
	}

	public String getSender() {
		return mSender;
	}

	public void setSender(String mSender) {
		this.mSender = mSender;
	}

	public String getReserve_date() {
		return mReserve_date;
	}

	public void setReserve_date(String mReserve_date) {
		this.mReserve_date = mReserve_date;
	}

	public String getMMS1() {
		return mMMS1;
	}

	public void setMMS1(String mMMS1) {
		this.mMMS1 = mMMS1;
	}

	public String getMMS2() {
		return mMMS2;
	}

	public void setMMS2(String mMMS2) {
		this.mMMS2 = mMMS2;
	}

	public String getMMS3() {
		return mMMS3;
	}

	public void setMMS3(String mMMS3) {
		this.mMMS3 = mMMS3;
	}

	public String getMMS4() {
		return mMMS4;
	}

	public void setMMS4(String mMMS4) {
		this.mMMS4 = mMMS4;
	}

	public byte[] getImg1() {
		return mImg1;
	}

	public void setImg1(byte[] mImg1) {
		this.mImg1 = mImg1;
	}

	public byte[] getImg2() {
		return mImg2;
	}

	public void setImg2(byte[] mImg2) {
		this.mImg2 = mImg2;
	}

	public byte[] getImg3() {
		return mImg3;
	}

	public void setImg3(byte[] mImg3) {
		this.mImg3 = mImg3;
	}

	public byte[] getImg4() {
		return mImg4;
	}

	public void setImg4(byte[] mImg4) {
		this.mImg4 = mImg4;
	}

	public String[] getPhoneList() {
		return mPhoneList;
	}

	public void setPhoneList(String[] mPhoneList) {
		this.mPhoneList = mPhoneList;
	}

	public int[] getDeatilMsgid() {
		return mDeatilMsgid;
	}

	public void setDeatilMsgid(int[] mDeatilMsgid) {
		this.mDeatilMsgid = mDeatilMsgid;
	}
    
}
