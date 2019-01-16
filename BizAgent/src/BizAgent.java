import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
 
public class BizAgent implements Daemon, Runnable {
    private String status = "";
    private int no = 0;
    private Thread thread = null;
    public Logger log = Logger.getLogger(getClass());
    Properties p = new Properties();
    private final String DB_URL = "jdbc:mysql://210.114.225.53/dhn?characterEncoding=utf8";  
    //private final String DB_URL = "jdbc:mysql://222.122.203.68/dhn?characterEncoding=utf8";
    private boolean isStop = false;
    BizDBCPInit bizDBCP;
    
    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        System.out.println("init...");
        String[] args = context.getArguments();
        if(args != null) {
            for(String arg : args) {
                System.out.println(arg);
            }
        }
        
        try {
        	//p.load(new FileInputStream("E:\\Git\\BizAgent\\conf\\log4j.properties")); 
        	//p.load(new FileInputStream("D:\\BIZ\\BizAgent\\BizAgent\\conf\\log4j.properties")); 
        	p.load(new FileInputStream("/root/BizAgent/conf/log4j.properties"));
        	PropertyConfigurator.configure(p);
        	log.info("Log Property Load !!");
            status = "INITED";
            this.thread = new Thread(this);
            log.info("init OK.");
            //System.out.println(); 
            bizDBCP = BizDBCPInit.getInstance();
        } catch(IOException e) {
        	log.info("../conf/log4j.properties 파일 없어");
        }

    }
 
    @Override
    public void start() {
        status = "STARTED";
        this.thread.start();
        log.info("Biz Agent start OK. ");
        isStop = false;
    }
 
    @Override
    public void stop() throws Exception {
        status = "STOPED";
        //this.thread.join(10);
        isStop = true;
        log.info("Biz Agent stop OK.");
    }
 
    @Override
    public void destroy() {
        status = "DESTROIED";
        log.info("Biz Agent destory OK.");
    }
 
    @Override
    public void run() {
    	
    	String PreMonth = "";
    	boolean isRunning = true;
        while(isRunning) {

			Date month = new Date();
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
			String monthStr = transFormat.format(month);

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -5);
			String PremonthStr = transFormat.format(cal.getTime());

			isRunning = !isStop;
			
			if(!monthStr.equals(PreMonth))
			{
	        	// 매월 1일에는 Log Table 생성
	        	Create_LOG_Table clt = new Create_LOG_Table(DB_URL, log);
	        	clt.log = log;
	        	clt.monthStr = monthStr;
	        	Thread clt_proc = new Thread(clt);
	        	clt_proc.start();
	        	PreMonth = monthStr;
			}
			
        	// 2차 발신 분류 처리
			   
			for(int i=0; i<10; i++) 
			{
	        	TBLReqProcess trp = new TBLReqProcess(DB_URL, log, i);
	        	Thread trp_proc = new Thread(trp);
	        	if(!isStop)
	        		trp_proc.start();
	
	        	if(TBLReqProcess.isRunning[i])
	        		isRunning = true;
			}
			   
        	// 나노 아이티 동보 전송 처리
        	Nano_it_summary nano = new Nano_it_summary(DB_URL, log);
        	Thread nano_sum_proc = new Thread(nano);
        	if(!isStop)
        		nano_sum_proc.start();

        	if(Nano_it_summary.isRunning)
        		isRunning = true;

        	
			// Nano 폰문자 처리
        	Nano_PMS_Proc nanoPMS = new Nano_PMS_Proc(DB_URL, log);
			nanoPMS.monthStr = monthStr;
			Thread nano_PMS_proc = new Thread(nanoPMS);
			if(!isStop)
				nano_PMS_proc.start();
        	
        	if(Nano_PMS_Proc.isRunning)
        		isRunning = true;
			
			if(!monthStr.equals(PremonthStr)) {
	        	Nano_PMS_Proc PrenanoPMS = new Nano_PMS_Proc(DB_URL, log);
	        	PrenanoPMS.monthStr = PremonthStr;
	        	PrenanoPMS.isPremonth = true;
				Thread Prenano_PMS_proc = new Thread(PrenanoPMS);
				if(!isStop)
					Prenano_PMS_proc.start();
	        	
				if(Nano_PMS_Proc.isPreRunning)
	        		isRunning = true;
								
			}
        	
			// Nano FUN SMS 처리 ( GRS SMS )
        	Nano_FUNSMS_Proc nanoFunsms = new Nano_FUNSMS_Proc(DB_URL, log);
        	nanoFunsms.monthStr = monthStr;
			Thread nanoFunsms_proc = new Thread(nanoFunsms);
			if(!isStop)
				nanoFunsms_proc.start();
			if(Nano_FUNSMS_Proc.isRunning)
        		isRunning = true;
        	
			if(!monthStr.equals(PremonthStr)) {
				Nano_FUNSMS_Proc PrenanoFunsms = new Nano_FUNSMS_Proc(DB_URL, log);
	        	PrenanoFunsms.monthStr = PremonthStr;
	        	PrenanoFunsms.isPremonth = true;
				Thread PrenanoFunsms_proc = new Thread(PrenanoFunsms);
				if(!isStop)
					PrenanoFunsms_proc.start();
				if(Nano_FUNSMS_Proc.isPreRunning)
	        		isRunning = true;
			}

			// Nano BKG LMS/MMS 처리
        	Nano_BKGMMS_Proc nanoBkgmms = new Nano_BKGMMS_Proc(DB_URL, log);
        	nanoBkgmms.monthStr = monthStr;
			Thread nanoBkgmms_proc = new Thread(nanoBkgmms);
			if(!isStop)
				nanoBkgmms_proc.start();
			if(Nano_BKGMMS_Proc.isRunning)
        		isRunning = true;
			
			if(!monthStr.equals(PremonthStr)) {
				Nano_BKGMMS_Proc PrenanoBkgmms = new Nano_BKGMMS_Proc(DB_URL, log);
	        	PrenanoBkgmms.monthStr = PremonthStr;
	        	PrenanoBkgmms.isPremonth = true;
				Thread PrenanoBkgmms_proc = new Thread(PrenanoBkgmms);
				if(!isStop)
					PrenanoBkgmms_proc.start();
				if(Nano_BKGMMS_Proc.isPreRunning)
	        		isRunning = true;
			}

			// Nano GRS 처리
			for(int j=0; j<10; j++) 
			{
				Nano_GRS_Proc nanogrs = new Nano_GRS_Proc(DB_URL, log, j);
				nanogrs.monthStr = monthStr;
				Thread nanogrs_proc = new Thread(nanogrs);
				if(!isStop)
					nanogrs_proc.start();
				if(Nano_GRS_Proc.isRunning[j])
	        		isRunning = true;
			}
			
			if(!monthStr.equals(PremonthStr)) {
				Nano_PREGRS_Proc Prenanogrs = new Nano_PREGRS_Proc(DB_URL, log);
				Prenanogrs.monthStr = PremonthStr;
				Prenanogrs.isPremonth = true;
				Thread Prenanogrs_proc = new Thread(Prenanogrs);
				if(!isStop)
					Prenanogrs_proc.start();
				if(Nano_GRS_Proc.isPreRunning)
	        		isRunning = true;
			}

			// Naself SMS 처리
			NAS_SMS_Proc nassms = new NAS_SMS_Proc(DB_URL, log);
			nassms.monthStr = monthStr;
			Thread nassms_proc = new Thread(nassms);
			if(!isStop)
				nassms_proc.start();
			if(NAS_SMS_Proc.isRunning)
        		isRunning = true;
			
			if(!monthStr.equals(PremonthStr)) {
				NAS_SMS_Proc Prenassms = new NAS_SMS_Proc(DB_URL, log);
				Prenassms.monthStr = PremonthStr;
				Prenassms.isPremonth = true;
				Thread Prenassms_proc = new Thread(Prenassms);
				if(!isStop)
					Prenassms_proc.start();
				if(NAS_SMS_Proc.isPreRunning)
	        		isRunning = true;
			}
			
			// Naself MMS 처리
			NAS_MMS_Proc nasmms = new NAS_MMS_Proc(DB_URL, log);
			nasmms.monthStr = monthStr;
			Thread nasmms_proc = new Thread(nasmms);
			if(!isStop)
				nasmms_proc.start();
			if(NAS_MMS_Proc.isRunning)
        		isRunning = true;
			
			if(!monthStr.equals(PremonthStr)) {
				NAS_MMS_Proc Prenasmms = new NAS_MMS_Proc(DB_URL, log);
				Prenasmms.monthStr = PremonthStr;
				Prenasmms.isPremonth = true;
				Thread Prenasmms_proc = new Thread(Prenasmms);
				if(!isStop)
					Prenasmms_proc.start();
				if(NAS_MMS_Proc.isPreRunning)
	        		isRunning = true;
			}
			
            try {
                //log.info("Biz Agent Call OK.");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("메인 Thread 오류 : " + e.toString());
            }
                        
           // if (no > 1) {
            	//log.info("Biz Agent 끝.");
                //break;
           // }
           // no++;
        }
    }
}
