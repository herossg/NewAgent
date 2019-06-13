package com.agent;

public class ServerMessage {
    private static final long serialVersionUID = 1L;
    private String mUserid;
    private int mMsgid;
    private int mDeatilMsgid;
    private String mResultCode;
    private String mResultMsg;
    private String m1stMessage_type;
    private String m2ndMessage_type;
    private String mSender;

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

	public String getmSender() {
		return mSender;
	}

	public void setmSender(String mSender) {
		this.mSender = mSender;
	}

	public int getmDeatilMsgid() {
		return mDeatilMsgid;
	}

	public void setmDeatilMsgid(int mDeatilMsgid) {
		this.mDeatilMsgid = mDeatilMsgid;
	}

	public String getmResultCode() {
		return mResultCode;
	}

	public void setmResultCode(String mResultCode) {
		this.mResultCode = mResultCode;
	}

	public String getmResultMsg() {
		return mResultMsg;
	}

	public void setmResultMsg(String mResultMsg) {
		this.mResultMsg = mResultMsg;
	}
    
}
