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
    private Logger log = Logger.getLogger(getClass());
    Properties p = new Properties();
    
    
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
        	p.load(new FileInputStream("D:\\bizalimtalk\\BizAgent\\BizAgent\\conf\\log4j.properties"));
        	PropertyConfigurator.configure(p);
        	log.info("Log Property Load !!");
            status = "INITED";
            this.thread = new Thread(this);
            System.out.println("init OK.");
            System.out.println();
        } catch(IOException e) {
        	System.out.println("../conf/log4j.properties 파일 없어");
        }

    }
 
    @Override
    public void start() {
        status = "STARTED";
        this.thread.start();
        log.info("Biz Agent start OK. ");
    }
 
    @Override
    public void stop() throws Exception {
        status = "STOPED";
        this.thread.join(10);
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
    	
        while(true) {

			Date month = new Date();
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
			String monthStr = transFormat.format(month);

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -5);
			String PremonthStr = transFormat.format(cal.getTime());

			if(!monthStr.equals(PreMonth))
			{
	        	// 매월 1일에는 Log Table 생성
	        	Create_LOG_Table clt = new Create_LOG_Table();
	        	clt.log = log;
	        	clt.monthStr = monthStr;
	        	Thread clt_proc = new Thread(clt);
	        	clt_proc.start();
	        	PreMonth = monthStr;
			}
			
        	// 2차 발신 분류 처리
        	TBLReqProcess trp = new TBLReqProcess();
        	trp.log = log;
        	Thread trp_proc = new Thread(trp);
        	trp_proc.start();
        	
        	// 나노 아이티 동보 전송 처리
        	Nano_it_summary nano = new Nano_it_summary();
        	nano.log = log;
        	Thread nano_sum_proc = new Thread(nano);
        	nano_sum_proc.start();

			// Nano 폰문자 처리
        	Nano_PMS_Proc nanoPMS = new Nano_PMS_Proc();
			nanoPMS.log = log;
			nanoPMS.monthStr = monthStr;
			Thread nano_PMS_proc = new Thread(nanoPMS);
			nano_PMS_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
	        	Nano_PMS_Proc PrenanoPMS = new Nano_PMS_Proc();
	        	PrenanoPMS.log = log;
	        	PrenanoPMS.monthStr = PremonthStr;
				Thread Prenano_PMS_proc = new Thread(PrenanoPMS);
				Prenano_PMS_proc.start();
			}
        	
			// Nano FUN SMS 처리 ( GRS SMS )
        	Nano_FUNSMS_Proc nanoFunsms = new Nano_FUNSMS_Proc();
        	nanoFunsms.log = log;
        	nanoFunsms.monthStr = monthStr;
			Thread nanoFunsms_proc = new Thread(nanoFunsms);
			nanoFunsms_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
				Nano_FUNSMS_Proc PrenanoFunsms = new Nano_FUNSMS_Proc();
	        	PrenanoFunsms.log = log;
	        	PrenanoFunsms.monthStr = PremonthStr;
				Thread PrenanoFunsms_proc = new Thread(PrenanoFunsms);
				PrenanoFunsms_proc.start();
			}

			// Nano BKG LMS/MMS 처리
        	Nano_BKGMMS_Proc nanoBkgmms = new Nano_BKGMMS_Proc();
        	nanoBkgmms.log = log;
        	nanoBkgmms.monthStr = monthStr;
			Thread nanoBkgmms_proc = new Thread(nanoBkgmms);
			nanoBkgmms_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
				Nano_BKGMMS_Proc PrenanoBkgmms = new Nano_BKGMMS_Proc();
	        	PrenanoBkgmms.log = log;
	        	PrenanoBkgmms.monthStr = PremonthStr;
				Thread PrenanoBkgmms_proc = new Thread(PrenanoBkgmms);
				PrenanoBkgmms_proc.start();
			}

			// Nano GRS 처리
			Nano_GRS_Proc nanogrs = new Nano_GRS_Proc();
			nanogrs.log = log;
			nanogrs.monthStr = monthStr;
			Thread nanogrs_proc = new Thread(nanogrs);
			nanogrs_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
				Nano_GRS_Proc Prenanogrs = new Nano_GRS_Proc();
				Prenanogrs.log = log;
				Prenanogrs.monthStr = PremonthStr;
				Thread Prenanogrs_proc = new Thread(Prenanogrs);
				Prenanogrs_proc.start();
			}

			// Naself SMS 처리
			NAS_SMS_Proc nassms = new NAS_SMS_Proc();
			nassms.log = log;
			nassms.monthStr = monthStr;
			Thread nassms_proc = new Thread(nassms);
			nassms_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
				NAS_SMS_Proc Prenassms = new NAS_SMS_Proc();
				Prenassms.log = log;
				Prenassms.monthStr = PremonthStr;
				Thread Prenassms_proc = new Thread(Prenassms);
				Prenassms_proc.start();
			}
			
			// Naself MMS 처리
			NAS_MMS_Proc nasmms = new NAS_MMS_Proc();
			nasmms.log = log;
			nasmms.monthStr = monthStr;
			Thread nasmms_proc = new Thread(nasmms);
			nasmms_proc.start();
        	
			if(!monthStr.equals(PremonthStr)) {
				NAS_MMS_Proc Prenasmms = new NAS_MMS_Proc();
				Prenasmms.log = log;
				Prenasmms.monthStr = PremonthStr;
				Thread Prenasmms_proc = new Thread(Prenasmms);
				Prenasmms_proc.start();
			}
			
            try {
                //log.info("Biz Agent Call OK.");
                Thread.sleep(5000);
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
