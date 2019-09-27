package com.agent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

public class ClientMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mUserid;
    private int mMsgid;
    private String mBtn1;
    private String mBtn2;
    private String mBtn3;
    private String mBtn4;
    private String mBtn5;
    private String m1stMessage_type;
    private String m2ndMessage_type;
    private String mMessage;
    private String mProfile;
    private String mTmpl_id;
    private String mSender;
    private String mReserve_date;
    private String mReg_dt;
    private String mMMS1;
    private String mMMS2;
    private String mMMS3;
    private String mMMS4;
    private byte[] mImg1;
    private byte[] mImg2;
    private byte[] mImg3;
    private byte[] mImg4;
    private String mAdd1;
    private String mAdd2;
    private String mAdd3;
    private String mAdd4;
    private String mAdd5;
    private String mServerXML;
    
    private StringBuffer mPhnList;
    
    /* 수신결과 관련 */
    private String mPhn;
    private String mResultCode;
    private String mResultDate;
	private String mResultMsg;
    
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
		if(this.mMMS1 != null && !this.mMMS1.isEmpty()) {
			File imgfile = new File(mMMS1);
			this.mImg1 = new byte[(int) imgfile.length()];
			try {
				DataInputStream imgis = new DataInputStream(new FileInputStream(imgfile));
				imgis.readFully(this.mImg1);
				imgis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getMMS2() {
		return mMMS2;
	}

	public void setMMS2(String mMMS2) {
		this.mMMS2 = mMMS2;
		if(this.mMMS2 != null && !this.mMMS2.isEmpty()) {
			File imgfile = new File(mMMS2);
			this.mImg2 = new byte[(int) imgfile.length()];
			try {
				DataInputStream imgis = new DataInputStream(new FileInputStream(imgfile));
				imgis.readFully(this.mImg2);
				imgis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getMMS3() {
		return mMMS3;
	}

	public void setMMS3(String mMMS3) {
		this.mMMS3 = mMMS3;
		if(this.mMMS3 != null && !this.mMMS3.isEmpty()) {
			File imgfile = new File(mMMS3);
			this.mImg3 = new byte[(int) imgfile.length()];
			try {
				DataInputStream imgis = new DataInputStream(new FileInputStream(imgfile));
				imgis.readFully(this.mImg3);
				imgis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getMMS4() {
		return mMMS4;
	}

	public void setMMS4(String mMMS4) {
		this.mMMS4 = mMMS4;
		if(this.mMMS4 != null && !this.mMMS4.isEmpty()) {
			File imgfile = new File(mMMS4);
			this.mImg4 = new byte[(int) imgfile.length()];
			try {
				DataInputStream imgis = new DataInputStream(new FileInputStream(imgfile));
				imgis.readFully(this.mImg4);
				imgis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	public String getPhn() {
		return mPhn;
	}

	public void setPhn(String mPhn) {
		this.mPhn = mPhn;
	}

	public String getResultCode() {
		return mResultCode;
	}

	public void setResultCode(String mResultCode) {
		this.mResultCode = mResultCode;
	}

	public String getResultDate() {
		return mResultDate;
	}

	public void setResultDate(String mResultDate) {
		this.mResultDate = mResultDate;
	}

	public String getResultMsg() {
		return mResultMsg;
	}

	public void setResultMsg(String mResultMsg) {
		this.mResultMsg = mResultMsg;
	}

	public String getReg_dt() {
		return mReg_dt;
	}

	public void setReg_dt(String mReg_dt) {
		this.mReg_dt = mReg_dt;
	}

	public String getAdd1() {
		return mAdd1;
	}

	public void setAdd1(String mAdd1) {
		this.mAdd1 = mAdd1;
	}

	public String getAdd2() {
		return mAdd2;
	}

	public void setAdd2(String mAdd2) {
		this.mAdd2 = mAdd2;
	}

	public String getAdd3() {
		return mAdd3;
	}

	public void setAdd3(String mAdd3) {
		this.mAdd3 = mAdd3;
	}

	public String getAdd4() {
		return mAdd4;
	}

	public void setAdd4(String mAdd4) {
		this.mAdd4 = mAdd4;
	}

	public String getAdd5() {
		return mAdd5;
	}

	public void setAdd5(String mAdd5) {
		this.mAdd5 = mAdd5;
	}

	public StringBuffer getPhnList() {
		return mPhnList;
	}

	public void setPhnList(StringBuffer mPhnList) {
		this.mPhnList = mPhnList;
	}
    
}
