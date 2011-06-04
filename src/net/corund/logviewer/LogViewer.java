package net.corund.logviewer;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LogViewer {
    public static void main(String[] args) throws Exception {
        LogViewer logViewer = new LogViewer();
        logViewer.init();
        
        Display display = new Display();
        Shell shell = logViewer.initUi(display);
        logViewer.load();
        
        shell.open();
        while (! shell.isDisposed()) {
            if (! display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
    
    private Button prevButton;
    private Button nextButton;
    private Button reloadButton;
    private Label label;
    private Text text;
    
    private Calendar calendar;
    private LogFetcher fetcher;
    
    public LogViewer() {
    }
    
    public void init() {
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream("config.properties");
        try {
            if (null != is) {
                props.load(is);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.fetcher = new LogFetcher();
        fetcher.setHost(props.getProperty("host"));
        
        CommonsHttpClient hClient = new CommonsHttpClient();
        hClient.setHost(props.getProperty("host"));
        hClient.setUsername(props.getProperty("username"));
        hClient.setPassword(props.getProperty("password"));
        hClient.init();
        
        fetcher.setHttpClientApi(hClient);
        
        this.calendar = Calendar.getInstance();
    }
    
    public Shell initUi(Display display) {
        Shell shell = new Shell(display);
        shell.setText("IRC LogViewer");
        shell.setLayout(new FormLayout());
        
        FormData formData;
        
        this.prevButton = new Button(shell, SWT.PUSH);
        prevButton.setText("Previous");
        formData = new FormData();
        formData.left = new FormAttachment(0, 5);
        formData.top = new FormAttachment(0, 5);
        prevButton.setLayoutData(formData);
        prevButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                calendar.add(Calendar.DATE, -1);
                try {
                    load();
                } catch (Exception e) {
                    //
                    e.printStackTrace();
                }
            }
        });
        
        this.nextButton = new Button(shell, SWT.PUSH);
        nextButton.setText("Next");
        formData = new FormData();
        formData.left = new FormAttachment(prevButton, 5);
        formData.top = new FormAttachment(0, 5);
        nextButton.setLayoutData(formData);
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                calendar.add(Calendar.DATE, 1);
                try {
                    load();
                } catch (Exception e) {
                    //
                    e.printStackTrace();
                }
            }
        });
        
        this.reloadButton = new Button(shell, SWT.PUSH);
        reloadButton.setText("Reload");
        formData = new FormData();
        formData.left = new FormAttachment(nextButton, 10);
        formData.top = new FormAttachment(0, 5);
        reloadButton.setLayoutData(formData);
        reloadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    load();
                } catch (Exception e) {
                    //
                    e.printStackTrace();
                }
            }
        });
        
        this.label = new Label(shell, SWT.LEFT);
        formData = new FormData();
        formData.left = new FormAttachment(reloadButton, 10);
        formData.top = new FormAttachment(0, 5);
        label.setLayoutData(formData);
        
        this.text = new Text(shell, SWT.MULTI | SWT.BORDER);
        formData = new FormData();
        formData.left = new FormAttachment(0, 5);
        formData.right = new FormAttachment(100, -5);
        formData.top = new FormAttachment(prevButton, 5);
        formData.bottom = new FormAttachment(100, -5);
        text.setLayoutData(formData);
        
        return shell;
    }
    
    public void load() throws Exception {
        int year = this.calendar.get(Calendar.YEAR);
        int month = this.calendar.get(Calendar.MONTH) + 1; // In java month index starts at 0
        int day = this.calendar.get(Calendar.DAY_OF_MONTH);
        
        load(year, month, day);
        this.label.setText(formatDate(this.calendar));
    }
    
    public void load(int year, int month, int day) throws Exception {
        List<LogEntry> entries = this.fetcher.logEntries(year, month, day);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sb.append("| nickname\t| time\t| message\n");
        sb.append("+---------------+-------+---------------\n");
        for (LogEntry entry : entries) {
            sb.append("| ");
            String nickname = entry.getNickname();
            sb.append(nickname);
            if (nickname.length() < 6) {
                sb.append("\t");
            }
            sb.append("\t| ");
            sb.append(sdf.format(entry.getDatetime()));
            sb.append("\t| ");
            String message = entry.getMessage();
            message = StringUtils.replace(message, "\n", "\n\t\t\t| ");
            sb.append(message);
            sb.append("\n");
        }
        
        this.text.setText(sb.toString());
    }
    
    private String formatDate(Calendar cal) {
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
