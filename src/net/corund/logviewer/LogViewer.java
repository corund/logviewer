package net.corund.logviewer;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

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
    
    private DateTime date;
    private Button reloadButton;
    private Table table;
    private Color gray;
    private Font italic;
    
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
    }
    
    private Pattern linkPattern = Pattern.compile("https?://\\S+");
    
    public Shell initUi(Display display) {
        InputStream is = getClass().getResourceAsStream("icon64.png");
        Image icon = new Image(display, is);
        
        final Shell shell = new Shell(display);
        shell.setText("IRC LogViewer");
        shell.setImage(icon);
        shell.setLayout(new FormLayout());
        
        this.gray = new Color(display, 0x60, 0x60, 0xa0);
        FontData[] fds = shell.getFont().getFontData();
        FontData[] ns = new FontData[fds.length];
        for (int i = 0; i < fds.length; i++) {
            ns[i] = new FontData(fds[i].getName(), fds[i].getHeight(), fds[i].getStyle() | SWT.ITALIC);
        }
        this.italic = new Font(display, ns);

        FormData formData;
        
        this.date = new DateTime(shell, SWT.DATE);
        formData = new FormData();
        formData.left = new FormAttachment(0, 5);
        formData.top = new FormAttachment(0, 5);
        date.setLayoutData(formData);
        Calendar cal = Calendar.getInstance();
        date.setDate(cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        date.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    load();
                } catch (Exception e) {
                    MessageBox box = new MessageBox(shell, SWT.OK);
                    box.setText("Error!\n" + e.getMessage());
                    box.open();
                }
            }
        });
        
        this.reloadButton = new Button(shell, SWT.PUSH);
        reloadButton.setText("Reload");
        formData = new FormData();
        formData.left = new FormAttachment(date, 5);
        formData.top = new FormAttachment(0, 5);
        reloadButton.setLayoutData(formData);
        reloadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    load();
                } catch (Exception e) {
                    MessageBox box = new MessageBox(shell, SWT.OK);
                    box.setText("Error!\n" + e.getMessage());
                    box.open();
                }
            }
        });
        
        this.table = new Table(shell, SWT.SINGLE | SWT.BORDER);
        formData = new FormData();
        formData.left = new FormAttachment(0, 5);
        formData.right = new FormAttachment(100, -5);
        formData.top = new FormAttachment(date, 5);
        formData.bottom = new FormAttachment(100, -5);
        table.setLayoutData(formData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        final TableColumn nickname = new TableColumn(table, SWT.NULL);
        nickname.setText("nickname");
        
        final TableColumn time = new TableColumn(table, SWT.CENTER);
        time.setText("time");
        
        final TableColumn message = new TableColumn(table, SWT.NULL);
        message.setText("message");
        
        table.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                int width = table.getClientArea().width;
                nickname.setWidth(96);
                time.setWidth(60);
                message.setWidth(width - 96 - 60);
            }
        });
        
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TableItem item = table.getSelection()[0];
                String text = item.getText(2);
                Matcher matcher = linkPattern.matcher(text);
                if (matcher.find()) {
                    String link = matcher.group();
                    Program.launch(link);
                }
            }
        });
        return shell;
    }
    
    public void load() throws Exception {
        int year = this.date.getYear();
        int month = this.date.getMonth() + 1; // In java month index starts at 0
        int day = this.date.getDay();

        this.table.removeAll();
        
        load(year, month, day);
    }
    
    public void load(int year, int month, int day) throws Exception {
        List<LogEntry> entries = this.fetcher.logEntries(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        
        for (LogEntry entry : entries) {
            String nickname = entry.getNickname();
            String time = sdf.format(entry.getDatetime());
            String message = entry.getMessage();
            
            /* 
             * swt TableItem 은 multiline 처리를 하지 않기 때문에 이처럼
             * 줄 단위로 메시지를 잘라 각각 TableItem 을 만든다. 
             */
            if (message.indexOf('\n') < 0) {
                TableItem item = new TableItem(this.table, SWT.NULL);
                item.setText(new String[] { nickname, time, message });
                if (null == nickname || "".equals(nickname)) {
                    item.setForeground(this.gray);
                    item.setFont(italic);
                }
            } else {
                String[] arrs = StringUtils.split(message, '\n');
                for (int i = 0; i < arrs.length; i++) {
                    TableItem item = new TableItem(this.table, SWT.NULL);
                    if (0 == i) {
                        item.setText(new String[] { nickname, time, arrs[i] });
                    } else {
                        item.setText(new String[] { "", "", arrs[i] });
                    }
                }
            }
        }
    }
}
