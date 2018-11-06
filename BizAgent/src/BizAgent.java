import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
        while(true) {
        	
        	TBLReqProcess trp = new TBLReqProcess();
        	trp.log = log;
        	Thread trp_proc = new Thread(trp);
        	trp_proc.start();
        	
        	Nano_it_summary nano = new Nano_it_summary();
        	nano.log = log;
        	Thread nano_sum_proc = new Thread(nano);
        	nano_sum_proc.start();
        	
        	Nano_PMS_Proc nanoPMS = new Nano_PMS_Proc();
			Date month = new Date();
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMM");
			String monthStr = transFormat.format(month);
			nanoPMS.log = log;
			nanoPMS.monthStr = monthStr;
			Thread nano_PMS_proc = new Thread(nanoPMS);
			nano_PMS_proc.start();
        	
            try {
                //log.info("Biz Agent Call OK.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           // if (no > 1) {
            	//log.info("Biz Agent 끝.");
                //break;
           // }
           // no++;
        }
    }
}
